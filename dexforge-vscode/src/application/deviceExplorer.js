class DeviceExplorerUseCase {
    constructor(deviceExplorerService, lspClientManager, outputChannel) {
        this.deviceExplorerService = deviceExplorerService;
        this.lspClientManager = lspClientManager;
        this.outputChannel = outputChannel;
    }

    async execute(vscode) {
        this.outputChannel.show(true);
        this.outputChannel.appendLine('[DexForge] Querying ADB for connected devices...');

        try {
            // 1. Fetch connected devices
            const devices = await this.deviceExplorerService.listDevices();
            if (devices.length === 0) {
                this.outputChannel.appendLine('[DexForge] No Android devices found via ADB.');
                vscode.window.showInformationMessage('No connected Android devices detected.');
                return;
            }

            this.outputChannel.appendLine(`[DexForge] Detected ${devices.length} device(s):`);
            devices.forEach((device) => {
                this.outputChannel.appendLine(`  - Serial: ${device.serial}, State: ${device.state}, Model: ${device.model}`);
            });

            const deviceItems = devices.map(d => ({
                label: d.serial,
                description: d.model,
                detail: `State: ${d.state}`
            }));

            const selectedDevice = await vscode.window.showQuickPick(deviceItems, { placeHolder: 'Select connected Android device' });
            if (!selectedDevice) {
                return;
            }
            const serial = selectedDevice.label;
            this.outputChannel.appendLine(`[DexForge] Selected device: ${serial}`);

            // 2. Fetch users for the selected device
            let users = [];
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: "DexForge: Fetching users...",
                cancellable: false
            }, async () => {
                users = await this.deviceExplorerService.listUsers(serial);
            });

            let userId = 0;
            if (users.length > 1) {
                const userItems = users.map(u => ({
                    label: String(u.id),
                    description: u.name
                }));
                const selectedUser = await vscode.window.showQuickPick(userItems, { placeHolder: 'Select Android User Profile' });
                if (!selectedUser) {
                    return;
                }
                userId = parseInt(selectedUser.label, 10);
            } else if (users.length === 1) {
                userId = users[0].id;
            }
            this.outputChannel.appendLine(`[DexForge] Using User ID: ${userId}`);

            // 3. Fetch packages for the user
            let packages = [];
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: "DexForge: Fetching installed packages...",
                cancellable: false
            }, async () => {
                packages = await this.deviceExplorerService.listPackages(serial, userId);
            });

            if (packages.length === 0) {
                vscode.window.showWarningMessage('No installed third-party/user packages found.');
                return;
            }

            const packageItems = packages.map(p => ({
                label: p.appName || p.packageName,
                description: p.packageName,
                detail: p.path ? `Path: ${p.path}` : undefined
            }));

            const selectedPackage = await vscode.window.showQuickPick(packageItems, {
                placeHolder: 'Select package to pull & decompile',
                matchOnDescription: true
            });
            if (!selectedPackage) {
                return;
            }
            const packageName = selectedPackage.description;
            this.outputChannel.appendLine(`[DexForge] Selected package: ${packageName}`);

            // 4. Select output directory
            const folderUris = await vscode.window.showOpenDialog({
                canSelectFiles: false,
                canSelectFolders: true,
                canSelectMany: false,
                openLabel: 'Select Output Workspace Folder'
            });

            if (!folderUris || folderUris.length === 0) {
                return;
            }
            const outDir = folderUris[0].fsPath;
            this.outputChannel.appendLine(`[DexForge] Target extraction folder: ${outDir}`);

            // 5. Run pull-and-decompile
            let pullResult = null;
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: `DexForge: Pulling & decompiling ${packageName}...`,
                cancellable: false
            }, async () => {
                pullResult = await this.deviceExplorerService.pullAndDecompile(serial, packageName, outDir, userId);
            });

            if (pullResult && pullResult.decompiledPath) {
                this.outputChannel.appendLine(`[DexForge] Decompilation successful!`);
                this.outputChannel.appendLine(`  - Local Path: ${pullResult.decompiledPath}`);
                vscode.window.showInformationMessage(`Successfully pulled and decompiled package: ${packageName}!`);

                // 6. Find base.apk and load it into LSP daemon if possible
                const baseApk = (pullResult.apkPaths || []).find(p => p.type === 'base' || p.localName === 'base.apk');
                if (baseApk) {
                    const client = this.lspClientManager.getClient();
                    if (client) {
                        const pathModule = require('path');
                        // Determine base.apk absolute path
                        const baseApkFullPath = pathModule.join(outDir, 'apks', baseApk.localName);
                        this.outputChannel.appendLine(`[DexForge] Loading main base.apk into LSP decompiler: ${baseApkFullPath}`);
                        try {
                            const loadResponse = await client.sendRequest('load', { path: baseApkFullPath });
                            if (loadResponse && loadResponse.classesCount !== undefined) {
                                this.outputChannel.appendLine(`[DexForge] LSP Decompiler loaded base.apk successfully.`);
                                vscode.window.showInformationMessage(`LSP engine loaded base.apk with ${loadResponse.classesCount} classes.`);
                            }
                        } catch (e) {
                            this.outputChannel.appendLine(`[LSP Error] Failed to load pulled APK to active daemon: ${e.message}`);
                        }
                    }
                }

                // 7. Prompt user to open folder in a new workspace window
                const openChoice = await vscode.window.showInformationMessage(
                    `Would you like to open the decompiled workspace directory?`,
                    'Open Folder',
                    'Cancel'
                );

                if (openChoice === 'Open Folder') {
                    const folderUri = vscode.Uri.file(pullResult.decompiledPath);
                    vscode.commands.executeCommand('vscode.openFolder', folderUri, true);
                }
            } else {
                throw new Error('Decompiled path not returned by CLI.');
            }

        } catch (error) {
            this.outputChannel.appendLine(`[Error] Device Explorer operation failed: ${error.message}`);
            vscode.window.showErrorMessage(`Device Explorer operation failed: ${error.message}`);
        }
    }
}

module.exports = DeviceExplorerUseCase;
