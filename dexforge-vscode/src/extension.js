const vscode = require('vscode');
const LspClientManager = require('./infrastructure/lspClient');
const DeviceExplorerService = require('./infrastructure/deviceService');
const LoadApkUseCase = require('./application/loadApk');
const DecompileClassUseCase = require('./application/decompileClass');
const DeviceExplorerUseCase = require('./application/deviceExplorer');
const BinaryDiffUseCase = require('./application/binaryDiff');
const YaraScanUseCase = require('./application/yaraScan');
const { decompilerProvider } = require('./presentation/contentProvider');

/** @type {LspClientManager | null} */
let lspClientManager = null;
/** @type {vscode.OutputChannel} */
let outputChannel;

/**
 * @param {vscode.ExtensionContext} context
 */
function activate(context) {
    outputChannel = vscode.window.createOutputChannel('DexForge');
    context.subscriptions.push(outputChannel);

    outputChannel.appendLine('[DexForge] VS Code extension activated.');

    const config = vscode.workspace.getConfiguration('dexforge');
    const dexforgeBinary = config.get('path') || 'dexforge';

    // 1. Initialize Infrastructure Services
    lspClientManager = new LspClientManager(outputChannel, dexforgeBinary);
    const deviceService = new DeviceExplorerService(dexforgeBinary);

    // 2. Initialize Application Use Cases
    const loadApkUseCase = new LoadApkUseCase(lspClientManager, outputChannel);
    const decompileClassUseCase = new DecompileClassUseCase(lspClientManager, decompilerProvider, outputChannel);
    const deviceExplorerUseCase = new DeviceExplorerUseCase(deviceService, lspClientManager, outputChannel);
    const binaryDiffUseCase = new BinaryDiffUseCase(lspClientManager, outputChannel);
    const yaraScanUseCase = new YaraScanUseCase(lspClientManager, outputChannel);

    // 3. Register Presentation / Content Providers
    let providerDisposable = vscode.workspace.registerTextDocumentContentProvider('dexforge', decompilerProvider);
    context.subscriptions.push(providerDisposable);

    // 4. Register Command Handlers
    let deviceExplorerDisposable = vscode.commands.registerCommand('dexforge.deviceExplorer', async () => {
        await deviceExplorerUseCase.execute(vscode);
    });
    context.subscriptions.push(deviceExplorerDisposable);

    let startDaemonDisposable = vscode.commands.registerCommand('dexforge.startDaemon', () => {
        lspClientManager.start(vscode);
    });
    context.subscriptions.push(startDaemonDisposable);

    let loadApkDisposable = vscode.commands.registerCommand('dexforge.loadApk', async () => {
        await loadApkUseCase.execute(vscode);
    });
    context.subscriptions.push(loadApkDisposable);

    let decompileClassDisposable = vscode.commands.registerCommand('dexforge.decompileClass', async () => {
        await decompileClassUseCase.execute(vscode);
    });
    context.subscriptions.push(decompileClassDisposable);

    let binaryDiffDisposable = vscode.commands.registerCommand('dexforge.binaryDiff', async () => {
        await binaryDiffUseCase.execute(vscode);
    });
    context.subscriptions.push(binaryDiffDisposable);

    let yaraScanDisposable = vscode.commands.registerCommand('dexforge.yaraScan', async () => {
        await yaraScanUseCase.execute(vscode);
    });
    context.subscriptions.push(yaraScanDisposable);

    // Automatically start decompiler daemon
    lspClientManager.start(vscode);
}

function deactivate() {
    if (!lspClientManager) {
        return undefined;
    }
    const stopPromise = lspClientManager.stop();
    lspClientManager = null;
    return stopPromise;
}

module.exports = {
    activate,
    deactivate
};
