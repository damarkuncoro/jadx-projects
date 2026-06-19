class YaraScanUseCase {
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

        // Select file to scan
        const fileUris = await vscode.window.showOpenDialog({
            canSelectMany: false,
            openLabel: 'Select file to scan with YARA',
            filters: {
                'All Files': ['*']
            }
        });

        if (!fileUris || fileUris.length === 0) {
            return;
        }

        const fileUri = fileUris[0];
        
        // Select YARA rule file
        const ruleUris = await vscode.window.showOpenDialog({
            canSelectMany: false,
            openLabel: 'Select YARA rule file',
            filters: {
                'YARA Rules': ['yar', 'yara', 'rules']
            }
        });

        if (!ruleUris || ruleUris.length === 0) {
            vscode.window.showWarningMessage('No YARA rule file selected.');
            return;
        }

        const ruleUri = ruleUris[0];
        this.outputChannel.show(true);
        this.outputChannel.appendLine(`[DexForge] Scanning ${fileUri.fsPath} with YARA rules: ${ruleUri.fsPath}`);

        try {
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: "DexForge: Running YARA scan...",
                cancellable: false
            }, async () => {
                const response = await client.sendRequest('dexforge/yara-scan', {
                    file: fileUri.fsPath,
                    rules: ruleUri.fsPath
                });

                if (response && response.matches) {
                    const scanUri = vscode.Uri.parse(`dexforge:yara-scan-${Date.now()}.txt`);
                    const scanContent = this.formatYaraResults(response.matches, fileUri.fsPath);
                    
                    const doc = await vscode.workspace.openTextDocument(scanUri);
                    const edit = new vscode.WorkspaceEdit();
                    edit.insert(scanUri, new vscode.Position(0, 0), scanContent);
                    await vscode.workspace.applyEdit(edit);
                    await vscode.window.showTextDocument(doc, { preview: false });
                    
                    this.outputChannel.appendLine(`[DexForge] YARA scan completed. Found ${response.matches.length} matches.`);
                } else {
                    const scanUri = vscode.Uri.parse(`dexforge:yara-scan-${Date.now()}.txt`);
                    const scanContent = '# YARA Scan Results\n\nNo matches found.\n';
                    
                    const doc = await vscode.workspace.openTextDocument(scanUri);
                    const edit = new vscode.WorkspaceEdit();
                    edit.insert(scanUri, new vscode.Position(0, 0), scanContent);
                    await vscode.workspace.applyEdit(edit);
                    await vscode.window.showTextDocument(doc, { preview: false });
                    
                    this.outputChannel.appendLine(`[DexForge] YARA scan completed. No matches found.`);
                }
            });
        } catch (error) {
            this.outputChannel.appendLine(`[Error] YARA scan failed: ${error.message}`);
            vscode.window.showErrorMessage(`YARA scan failed: ${error.message}`);
        }
    }

    formatYaraResults(matches, filePath) {
        let output = '# YARA Scan Results\n\n';
        output += `## File: ${filePath}\n\n`;
        output += `## Matches: ${matches.length}\n\n`;

        matches.forEach((match, index) => {
            output += `### Match ${index + 1}: ${match.ruleName}\n`;
            output += `- **Namespace:** ${match.namespace}\n`;
            output += `- **Tags:** ${match.tags ? match.tags.join(', ') : 'None'}\n`;
            output += `- **Offset:** ${match.offset}\n`;
            output += `- **Matched String:** ${match.matchedString}\n\n`;
        });

        return output;
    }
}

module.exports = YaraScanUseCase;
