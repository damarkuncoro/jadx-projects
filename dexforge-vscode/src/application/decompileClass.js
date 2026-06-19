const ClassMetadata = require('../domain/class');

class DecompileClassUseCase {
    constructor(lspClientManager, decompilerProvider, outputChannel) {
        this.lspClientManager = lspClientManager;
        this.decompilerProvider = decompilerProvider;
        this.outputChannel = outputChannel;
    }

    async execute(vscode) {
        const client = this.lspClientManager.getClient();
        if (!client) {
            vscode.window.showErrorMessage('DexForge Daemon is not running. Please start the daemon first.');
            return;
        }

        this.outputChannel.show(true);
        this.outputChannel.appendLine('[DexForge] Fetching class list from decompiler...');

        try {
            let classesDto = [];
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: "DexForge: Fetching class list...",
                cancellable: false
            }, async () => {
                classesDto = await client.sendRequest('list-classes');
            });

            if (!classesDto || classesDto.length === 0) {
                vscode.window.showWarningMessage('No classes loaded. Please load an APK/JAR file first.');
                return;
            }

            const classes = classesDto.map(dto => ClassMetadata.fromDto(dto));

            const items = classes.map(cls => ({
                label: cls.alias || cls.name,
                description: cls.fullName,
                detail: cls.package ? `Package: ${cls.package}` : undefined
            }));

            const selected = await vscode.window.showQuickPick(items, {
                placeHolder: 'Select a class to decompile',
                matchOnDescription: true,
                matchOnDetail: true
            });

            if (!selected) {
                return;
            }

            const className = selected.description;
            this.outputChannel.appendLine(`[DexForge] Decompiling class: ${className}`);

            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: `DexForge: Decompiling ${className}...`,
                cancellable: false
            }, async () => {
                const response = await client.sendRequest('decompile', { className });
                if (response && response.code !== undefined) {
                    const docUri = vscode.Uri.parse(`dexforge:${className.replace(/\./g, '/')}.java`);
                    this.decompilerProvider.documents.set(docUri.toString(), response.code);
                    this.decompilerProvider.onDidChangeEmitter.fire(docUri);

                    const doc = await vscode.workspace.openTextDocument(docUri);
                    await vscode.window.showTextDocument(doc, { preview: false });
                    this.outputChannel.appendLine(`[DexForge] Class ${className} decompiled and displayed successfully.`);
                } else {
                    throw new Error(response ? JSON.stringify(response) : 'No code returned from decompiler.');
                }
            });

        } catch (error) {
            this.outputChannel.appendLine(`[Error] Decompilation failed: ${error.message}`);
            vscode.window.showErrorMessage(`Decompilation failed: ${error.message}`);
        }
    }
}

module.exports = DecompileClassUseCase;
