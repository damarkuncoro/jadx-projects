package jadx.gui.frida;

import jadx.gui.device.adb.ADBDevice;
import jadx.gui.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles APK patching, frida-gadget injection, and signing.
 */
public class FridaApkPatcher {
    private static final Logger LOG = LoggerFactory.getLogger(FridaApkPatcher.class);

    private final MainWindow mainWindow;
    private final Consumer<String> logAppender;
    private final FridaDownloader downloader;

    public FridaApkPatcher(MainWindow mainWindow, Consumer<String> logAppender, FridaDownloader downloader) {
        this.mainWindow = mainWindow;
        this.logAppender = logAppender;
        this.downloader = downloader;
    }

    public void autoPatchAndInstallApk(ADBDevice device, String arch) {
        new Thread(() -> {
            File tempDir = null;
            List<File> signedApksToInstall = new ArrayList<>();
            try {
                logAppender.accept("[INFO] Starting automatic APK patching process...");
                List<java.nio.file.Path> filePaths = mainWindow.getProject().getFilePaths();
                if (filePaths.isEmpty()) {
                    logAppender.accept("[ERROR] No APK file is loaded in the project.");
                    return;
                }

                java.nio.file.Path baseApkPath = FridaUtils.findBaseApkPath(filePaths);
                logAppender.accept("[INFO] Base APK identified: " + baseApkPath.getFileName());

                tempDir = Files.createTempDirectory("jadx_patch_").toFile();
                logAppender.accept("[INFO] Decompiling base APK using apktool...");

                String apktoolPath = FridaUtils.findApktoolPath();
                int decExit = FridaUtils.runCommand(new String[]{
                        apktoolPath, "d", "-r", "-f", "-o", tempDir.getAbsolutePath(), baseApkPath.toAbsolutePath().toString()
                }, "apktool-d", logAppender);
                if (decExit != 0) {
                    throw new IOException("Apktool decompilation failed with exit code " + decExit);
                }

                String mainActivity = FridaUtils.findMainActivityFromJadx(mainWindow);
                if (mainActivity == null) {
                    throw new IOException("MainActivity not found in AndroidManifest.xml");
                }
                logAppender.accept("[INFO] Found MainActivity: " + mainActivity);

                File smaliFile = FridaUtils.findSmaliFile(tempDir, mainActivity);
                if (smaliFile == null) {
                    throw new IOException("MainActivity smali file not found");
                }
                injectFridaGadgetLoad(smaliFile);
                copyFridaGadgetLib(tempDir, arch);

                logAppender.accept("[INFO] Rebuilding base APK using apktool...");
                File unsignedBaseApk = new File(tempDir.getParentFile(), "patched_base_unsigned.apk");
                int compExit = FridaUtils.runCommand(new String[]{
                        apktoolPath, "b", tempDir.getAbsolutePath(), "-o", unsignedBaseApk.getAbsolutePath()
                }, "apktool-b", logAppender);
                if (compExit != 0) {
                    throw new IOException("Apktool build failed with exit code " + compExit);
                }

                File keystoreFile = new File(tempDir.getParentFile(), "debug.keystore");
                if (!keystoreFile.exists()) {
                    logAppender.accept("[INFO] Generating debug keystore...");
                    generateDebugKeystore(keystoreFile);
                }

                logAppender.accept("[INFO] Signing base APK...");
                File signedBaseApk = new File(tempDir.getParentFile(), "patched_base_signed.apk");
                signApk(unsignedBaseApk, signedBaseApk, keystoreFile);
                unsignedBaseApk.delete();
                signedApksToInstall.add(signedBaseApk);

                // Process other split APKs (resign them with the same keystore)
                for (java.nio.file.Path path : filePaths) {
                    if (path.equals(baseApkPath)) {
                        continue;
                    }
                    String splitName = path.getFileName().toString();
                    logAppender.accept("[INFO] Resigning split APK: " + splitName + "...");
                    File signedSplit = new File(tempDir.getParentFile(), "signed_" + splitName);
                    signApk(path.toFile(), signedSplit, keystoreFile);
                    signedApksToInstall.add(signedSplit);
                }

                // Uninstall the original package to avoid signature mismatch
                String adbPath = jadx.gui.device.adb.AdbService.detectAdbPath();
                String packageName = FridaUtils.resolveTargetPackage(mainWindow);
                if (packageName != null && !packageName.isEmpty()) {
                    logAppender.accept("[INFO] Uninstalling existing app to prevent signature mismatch: " + packageName + "...");
                    FridaUtils.runCommand(new String[]{
                            adbPath, "-s", device.getSerial(), "uninstall", packageName
                    }, "adb-uninstall", logAppender);
                }

                // Install all signed APKs (base + splits)
                logAppender.accept("[INFO] Installing patched APK(s) to device " + device.getSerial() + "...");
                List<String> installCmd = new ArrayList<>();
                installCmd.add(adbPath);
                installCmd.add("-s");
                installCmd.add(device.getSerial());
                if (signedApksToInstall.size() > 1) {
                    installCmd.add("install-multiple");
                    installCmd.add("-r");
                    for (File f : signedApksToInstall) {
                        installCmd.add(f.getAbsolutePath());
                    }
                } else {
                    installCmd.add("install");
                    installCmd.add("-r");
                    installCmd.add(signedApksToInstall.get(0).getAbsolutePath());
                }

                int instExit = FridaUtils.runCommand(installCmd.toArray(new String[0]), "adb-install", logAppender);
                if (instExit != 0) {
                    throw new IOException("Installation failed with exit code " + instExit);
                }

                logAppender.accept("[INFO] APK(s) successfully patched, signed, and installed!");
                logAppender.accept("[INFO] Please open the application on your device to launch the Frida Gadget and run the script again.");
            } catch (Exception ex) {
                LOG.error("Failed to auto patch APK", ex);
                logAppender.accept("[ERROR] Failed to auto patch APK: " + ex.getMessage());
            } finally {
                if (tempDir != null) {
                    FridaUtils.deleteDirectory(tempDir);
                }
                for (File f : signedApksToInstall) {
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
        }).start();
    }

    public void injectFridaGadgetLoad(File smaliFile) throws IOException {
        List<String> lines = Files.readAllLines(smaliFile.toPath(), StandardCharsets.UTF_8);
        List<String> newLines = new ArrayList<>();
        boolean hasClinit = false;

        for (String line : lines) {
            if (line.trim().startsWith(".method static constructor <clinit>()V")) {
                hasClinit = true;
                break;
            }
        }

        if (hasClinit) {
            boolean injected = false;
            boolean inClinit = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                newLines.add(line);
                if (line.trim().startsWith(".method static constructor <clinit>()V")) {
                    inClinit = true;
                } else if (inClinit && !injected) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith(".registers") || trimmed.startsWith(".locals")) {
                        // Ensure we have at least 1 register/local
                        String[] parts = trimmed.split("\\s+");
                        if (parts.length >= 2) {
                            try {
                                int val = Integer.parseInt(parts[1]);
                                if (val < 1) {
                                    newLines.remove(newLines.size() - 1);
                                    newLines.add(parts[0] + " 1");
                                }
                            } catch (NumberFormatException e) {
                                // ignore, keep as is
                            }
                        }
                        newLines.add("");
                        newLines.add("    const-string v0, \"frida-gadget\"");
                        newLines.add("    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V");
                        newLines.add("");
                        injected = true;
                        inClinit = false;
                    } else if (trimmed.startsWith(".end method") || (!trimmed.isEmpty() && !trimmed.startsWith("."))) {
                        // We found an instruction or the end of the method before registers/locals declaration
                        // This means they are missing. Let's inject registers and load call before this line.
                        newLines.remove(newLines.size() - 1); // remove the current line
                        newLines.add("    .registers 1");
                        newLines.add("");
                        newLines.add("    const-string v0, \"frida-gadget\"");
                        newLines.add("    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V");
                        newLines.add("");
                        newLines.add(line); // re-add the current line
                        injected = true;
                        inClinit = false;
                    }
                }
            }
        } else {
            for (String line : lines) {
                newLines.add(line);
            }
            newLines.add("");
            newLines.add(".method static constructor <clinit>()V");
            newLines.add("    .registers 1");
            newLines.add("    const-string v0, \"frida-gadget\"");
            newLines.add("    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V");
            newLines.add("    return-void");
            newLines.add(".end method");
        }

        Files.write(smaliFile.toPath(), newLines, StandardCharsets.UTF_8);
        LOG.info("[FridaPanel] Injected frida-gadget loader into smali file: {}", smaliFile.getAbsolutePath());
    }

    public void copyFridaGadgetLib(File decompiledDir, String arch) throws IOException {
        File libDir = new File(decompiledDir, "lib");
        if (!libDir.exists()) {
            libDir.mkdirs();
        }

        File[] existingDirs = libDir.listFiles(File::isDirectory);
        if (existingDirs != null && existingDirs.length > 0) {
            for (File dir : existingDirs) {
                String dirName = dir.getName();
                String targetArch = null;
                if (dirName.equals("arm64-v8a")) targetArch = "arm64";
                else if (dirName.equals("armeabi-v7a") || dirName.equals("armeabi")) targetArch = "arm";
                else if (dirName.equals("x86")) targetArch = "x86";
                else if (dirName.equals("x86_64")) targetArch = "x86_64";

                if (targetArch != null) {
                    String version = FridaUtils.getLocalFridaVersion();
                    downloader.downloadFridaGadget(targetArch, version);
                    File gadgetSrc = new File(System.getProperty("user.home"), ".cache/frida/gadget-android-" + targetArch + ".so");
                    if (gadgetSrc.exists()) {
                        File destFile = new File(dir, "libfrida-gadget.so");
                        Files.copy(gadgetSrc.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        LOG.info("[FridaPanel] Copied frida-gadget ({}) to: {}", targetArch, destFile.getAbsolutePath());
                    }
                }
            }
        } else {
            // No existing lib dirs, create one matching the connected device arch
            String abi = "arm64-v8a";
            if (arch.equals("arm")) abi = "armeabi-v7a";
            else if (arch.equals("x86")) abi = "x86";
            else if (arch.equals("x86_64")) abi = "x86_64";

            File dir = new File(libDir, abi);
            dir.mkdirs();
            String version = FridaUtils.getLocalFridaVersion();
            downloader.downloadFridaGadget(arch, version);
            File gadgetSrc = new File(System.getProperty("user.home"), ".cache/frida/gadget-android-" + arch + ".so");
            if (gadgetSrc.exists()) {
                File destFile = new File(dir, "libfrida-gadget.so");
                Files.copy(gadgetSrc.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                LOG.info("[FridaPanel] Created and copied frida-gadget ({}) to: {}", arch, destFile.getAbsolutePath());
            }
        }
    }

    public void generateDebugKeystore(File keystoreFile) throws Exception {
        String keytoolPath = "keytool";
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            File kt = new File(javaHome, "bin/keytool");
            if (kt.exists()) {
                keytoolPath = kt.getAbsolutePath();
            }
        }
        int exitCode = FridaUtils.runCommand(new String[]{
                keytoolPath, "-genkey", "-v", "-keystore", keystoreFile.getAbsolutePath(),
                "-alias", "jadx", "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000",
                "-storepass", "android", "-keypass", "android", "-dname", "CN=JadxPatch"
        }, "keytool", logAppender);
        if (exitCode != 0) {
            throw new IOException("Failed to generate debug keystore using keytool");
        }
    }

    public void signApk(File unsignedApk, File signedApk, File keystoreFile) throws Exception {
        String apksigner = FridaUtils.findBuildToolsBinary("apksigner");
        int exitCode = FridaUtils.runCommand(new String[]{
                apksigner, "sign", "--ks", keystoreFile.getAbsolutePath(),
                "--ks-pass", "pass:android", "--out", signedApk.getAbsolutePath(),
                unsignedApk.getAbsolutePath()
        }, "apksigner", logAppender);
        if (exitCode != 0) {
            throw new IOException("Apksigner failed with exit code " + exitCode);
        }
    }
}
