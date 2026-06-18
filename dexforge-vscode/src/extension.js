const vscode = require('vscode');
const cp = require('child_process');
const { LanguageClient } = require('vscode-languageclient/node');

/** @type {LanguageClient | null} */
let client = null;
/** @type {vscode.OutputChannel} */
let outputChannel;

/**
 * @param {vscode.ExtensionContext} context
 */
function activate(context) {
    outputChannel = vscode.window.createOutputChannel('DexForge');
    context.subscriptions.push(outputChannel);

    outputChannel.appendLine('[DexForge] VS Code extension activated.');

    // Register Command: Open Device Explorer
    let deviceExplorerDisposable = vscode.commands.registerCommand('dexforge.deviceExplorer', async () => {
        await handleDeviceExplorer();
    });
    context.subscriptions.push(deviceExplorerDisposable);

    // Register Command: Start Daemon / LSP
    let startDaemonDisposable = vscode.commands.registerCommand('dexforge.startDaemon', () => {
        startLanguageServer();
    });
    context.subscriptions.push(startDaemonDisposable);

    // Automatically start LSP client
    startLanguageServer();
}

function startLanguageServer() {
    if (client) {
        outputChannel.appendLine('[DexForge] LSP Daemon is already running.');
        return;
    }

    const config = vscode.workspace.getConfiguration('dexforge');
    const dexforgeBinary = config.get('path') || 'dexforge';

    outputChannel.appendLine(`[DexForge] Starting Language Server via: ${dexforgeBinary} lsp`);

    // Server options to spawn daemon
    const serverOptions = {
        command: dexforgeBinary,
        args: ['lsp'],
        options: {
            env: process.env,
            shell: true
        }
    };

    // Client options
    const clientOptions = {
        documentSelector: [
            { scheme: 'file', language: 'java' },
            { scheme: 'file', language: 'smali' }
        ],
        synchronize: {
            fileEvents: vscode.workspace.createFileSystemWatcher('**/*.{java,smali}')
        },
        outputChannel: outputChannel
    };

    try {
        client = new LanguageClient(
            'dexforgeDecompilerLSP',
            'DexForge Decompiler Language Server',
            serverOptions,
            clientOptions
        );

        client.start().then(() => {
            outputChannel.appendLine('[DexForge] Decompiler Language Server successfully started.');
        }).catch((err) => {
            outputChannel.appendLine(`[DexForge] Failed to start decompiler daemon: ${err.message}`);
            client = null;
        });
    } catch (e) {
        outputChannel.appendLine(`[DexForge] Error initializing LanguageClient: ${e.message}`);
        client = null;
    }
}

async function handleDeviceExplorer() {
    const config = vscode.workspace.getConfiguration('dexforge');
    const dexforgeBinary = config.get('path') || 'dexforge';

    outputChannel.show(true);
    outputChannel.appendLine('[DexForge] Querying ADB for connected devices...');

    cp.exec(`${dexforgeBinary} device-explorer list-devices --format json`, (error, stdout, stderr) => {
        if (error) {
            outputChannel.appendLine(`[Error] ADB device query failed: ${error.message}`);
            if (stderr) {
                outputChannel.appendLine(`[stderr] ${stderr}`);
            }
            vscode.window.showErrorMessage('Failed to list Android devices. Make sure adb is configured.');
            return;
        }

        try {
            const result = JSON.parse(stdout);
            if (result && result.status === 'ERROR') {
                outputChannel.appendLine(`[Error] Daemon returned error: ${JSON.stringify(result.error)}`);
                vscode.window.showErrorMessage(`Device Explorer error: ${result.error.message}`);
                return;
            }

            const devices = Array.isArray(result) ? result : (result.result || []);
            if (devices.length === 0) {
                outputChannel.appendLine('[DexForge] No Android devices found via ADB.');
                vscode.window.showInformationMessage('No connected Android devices detected.');
                return;
            }

            outputChannel.appendLine(`[DexForge] Detected ${devices.length} device(s):`);
            devices.forEach((device) => {
                outputChannel.appendLine(`  - Serial: ${device.serial}, State: ${device.state}, Model: ${device.model || 'Unknown'}`);
            });

            // Prompt user to select device
            const items = devices.map(d => `${d.serial} (${d.model || 'Generic'})`);
            vscode.window.showQuickPick(items, { placeHolder: 'Select connected Android device' }).then((selection) => {
                if (selection) {
                    const serial = selection.split(' ')[0];
                    outputChannel.appendLine(`[DexForge] Selected device: ${serial}`);
                    vscode.window.showInformationMessage(`Selected device: ${serial}. Querying packages next...`);
                    // We can proceed to query package listings in future milestones
                }
            });

        } catch (e) {
            outputChannel.appendLine(`[Error] Failed to parse daemon output: ${stdout}`);
            vscode.window.showErrorMessage('Failed to process Device Explorer output.');
        }
    });
}

function deactivate() {
    if (!client) {
        return undefined;
    }
    outputChannel.appendLine('[DexForge] Stopping Language Server...');
    const result = client.stop();
    client = null;
    return result;
}

module.exports = {
    activate,
    deactivate
};
