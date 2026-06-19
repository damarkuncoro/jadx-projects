class LoadApkUseCase {
    constructor(lspClientManager, outputChannel) {
        this.lspClientManager = lspClientManager;
        this.outputChannel = outputChannel;
    }

    async execute(vscode) {
        const client = this.lspClientManager.getClient();
        if (!client) {
            vscode.window.showErrorMessage('DexForge Daemon is not running. Please start the daemon first.');
            return;
        }

        const fileUris = await vscode.window.showOpenDialog({
            canSelectMany: false,
            openLabel: 'Load APK/JAR',
            filters: {
                'Android/Java Packages': ['apk', 'jar', 'dex', 'aar', 'zip']
            }
        });

        if (!fileUris || fileUris.length === 0) {
            return;
        }

        const fileUri = fileUris[0];
        this.outputChannel.show(true);
        this.outputChannel.appendLine(`[DexForge] Loading file: ${fileUri.fsPath}`);

        try {
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: "DexForge: Decompiling and loading...",
                cancellable: false
            }, async () => {
                const response = await client.sendRequest('load', { path: fileUri.fsPath });
                if (response && response.classesCount !== undefined) {
                    this.outputChannel.appendLine(`[DexForge] File loaded successfully.`);
                    this.outputChannel.appendLine(`  - Classes: ${response.classesCount}`);
                    this.outputChannel.appendLine(`  - Resources: ${response.resourcesCount}`);
                    vscode.window.showInformationMessage(`Successfully loaded! Found ${response.classesCount} classes and ${response.resourcesCount} resources.`);
                } else {
                    throw new Error(response ? JSON.stringify(response) : 'Unknown response');
                }
            });
        } catch (error) {
            this.outputChannel.appendLine(`[Error] Failed to load file: ${error.message}`);
            vscode.window.showErrorMessage(`Failed to load file: ${error.message}`);
        }
    }
}

module.exports = LoadApkUseCase;
