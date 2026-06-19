class BinaryDiffUseCase {
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

        // Select two files to compare
        const files = await vscode.window.showOpenDialog({
            canSelectMany: true,
            openLabel: 'Select two files to compare',
            filters: {
                'Android/Java Packages': ['apk', 'jar', 'dex', 'aar', 'zip']
            }
        });

        if (!files || files.length !== 2) {
            vscode.window.showWarningMessage('Please select exactly two files to compare.');
            return;
        }

        this.outputChannel.show(true);
        this.outputChannel.appendLine(`[DexForge] Comparing: ${files[0].fsPath} vs ${files[1].fsPath}`);

        try {
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: "DexForge: Comparing binaries...",
                cancellable: false
            }, async () => {
                const response = await client.sendRequest('dexforge/binary-diff', {
                    file1: files[0].fsPath,
                    file2: files[1].fsPath
                });

                if (response && response.diff) {
                    const diffUri = vscode.Uri.parse(`dexforge:diff-${Date.now()}.txt`);
                    const diffContent = this.formatDiff(response.diff);
                    
                    const doc = await vscode.workspace.openTextDocument(diffUri);
                    const edit = new vscode.WorkspaceEdit();
                    edit.insert(diffUri, new vscode.Position(0, 0), diffContent);
                    await vscode.workspace.applyEdit(edit);
                    await vscode.window.showTextDocument(doc, { preview: false });
                    
                    this.outputChannel.appendLine(`[DexForge] Binary diff completed.`);
                } else {
                    throw new Error(response ? JSON.stringify(response) : 'No diff returned');
                }
            });
        } catch (error) {
            this.outputChannel.appendLine(`[Error] Binary diff failed: ${error.message}`);
            vscode.window.showErrorMessage(`Binary diff failed: ${error.message}`);
        }
    }

    formatDiff(diff) {
        let output = '# Binary Diff Report\n\n';
        output += `## Summary\n`;
        output += `- Added classes: ${diff.addedClasses || 0}\n`;
        output += `- Removed classes: ${diff.removedClasses || 0}\n`;
        output += `- Modified classes: ${diff.modifiedClasses || 0}\n\n`;
        
        if (diff.added && diff.added.length > 0) {
            output += `## Added Classes\n`;
            diff.added.forEach(cls => {
                output += `+ ${cls}\n`;
            });
            output += '\n';
        }

        if (diff.removed && diff.removed.length > 0) {
            output += `## Removed Classes\n`;
            diff.removed.forEach(cls => {
                output += `- ${cls}\n`;
            });
            output += '\n';
        }

        if (diff.modified && diff.modified.length > 0) {
            output += `## Modified Classes\n`;
            diff.modified.forEach(cls => {
                output += `~ ${cls}\n`;
            });
            output += '\n';
        }

        return output;
    }
}

module.exports = BinaryDiffUseCase;
