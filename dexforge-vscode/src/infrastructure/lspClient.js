const cp = require('child_process');
const { LanguageClient } = require('vscode-languageclient/node');
const LspStreamBridge = require('./lspBridge');

class LspClientManager {
    constructor(outputChannel, dexforgeBinary) {
        this.outputChannel = outputChannel;
        this.dexforgeBinary = dexforgeBinary;
        /** @type {LanguageClient | null} */
        this.client = null;
    }

    start(vscode) {
        if (this.client) {
            this.outputChannel.appendLine('[DexForge] LSP Daemon is already running.');
            return;
        }

        this.outputChannel.appendLine(`[DexForge] Starting Language Server via: ${this.dexforgeBinary} lsp`);

        const serverOptions = () => {
            const proc = cp.spawn(this.dexforgeBinary, ['lsp'], {
                env: process.env,
                shell: true
            });

            proc.stderr.on('data', (chunk) => {
                this.outputChannel.appendLine(`[Daemon stderr] ${chunk.toString('utf8')}`);
            });
            proc.on('close', (code) => {
                this.outputChannel.appendLine(`[Daemon] Process exited with code ${code}`);
                this.client = null;
            });

            const reader = LspStreamBridge.createReaderStream(proc, (err) => this.outputChannel.appendLine(`[Adapter Error] ${err}`));
            const writer = LspStreamBridge.createWriterStream(proc, (err) => this.outputChannel.appendLine(`[Adapter Error] ${err}`));

            return Promise.resolve({ reader, writer });
        };

        const clientOptions = {
            documentSelector: [
                { scheme: 'file', language: 'java' },
                { scheme: 'file', language: 'smali' }
            ],
            synchronize: {
                fileEvents: vscode.workspace.createFileSystemWatcher('**/*.{java,smali}')
            },
            outputChannel: this.outputChannel
        };

        try {
            this.client = new LanguageClient(
                'dexforgeDecompilerLSP',
                'DexForge Decompiler Language Server',
                serverOptions,
                clientOptions
            );

            this.client.start().then(() => {
                this.outputChannel.appendLine('[DexForge] Decompiler Language Server successfully started.');
            }).catch((err) => {
                this.outputChannel.appendLine(`[DexForge] Failed to start decompiler daemon: ${err.message}`);
                this.client = null;
            });
        } catch (e) {
            this.outputChannel.appendLine(`[DexForge] Error initializing LanguageClient: ${e.message}`);
            this.client = null;
        }
    }

    stop() {
        if (!this.client) {
            return Promise.resolve();
        }
        this.outputChannel.appendLine('[DexForge] Stopping Language Server...');
        const result = this.client.stop();
        this.client = null;
        return result;
    }

    getClient() {
        return this.client;
    }
}

module.exports = LspClientManager;
