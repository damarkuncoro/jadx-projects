package jadx.gui.frida;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * Handles downloading and pushing Frida server and gadget.
 */
public class FridaDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(FridaDownloader.class);

    private final Consumer<String> logAppender;

    public FridaDownloader(Consumer<String> logAppender) {
        this.logAppender = logAppender;
    }

    public void downloadFridaGadget(String arch, String version) throws IOException {
        try {
            File gadgetDir = new File(System.getProperty("user.home"), ".cache/frida");
            if (!gadgetDir.exists()) {
                gadgetDir.mkdirs();
            }
            File gadgetFile = new File(gadgetDir, "gadget-android-" + arch + ".so");
            if (gadgetFile.exists()) {
                return;
            }

            logAppender.accept("[INFO] Downloading frida-gadget " + version + " for " + arch + "...");
            String downloadUrl = "https://github.com/frida/frida/releases/download/" + version + "/frida-gadget-" + version + "-android-" + arch + ".so.xz";
            File tempXz = File.createTempFile("frida_gadget_", ".xz");

            String pythonBin = new File("/usr/bin/python3").exists() ? "/usr/bin/python3" : "python3";

            String pyCmd = "import urllib.request, lzma; urllib.request.urlretrieve('" + downloadUrl + "', '" + tempXz.getAbsolutePath() + "'); " +
                    "open('" + gadgetFile.getAbsolutePath() + "', 'wb').write(lzma.open('" + tempXz.getAbsolutePath() + "').read())";

            Process process = Runtime.getRuntime().exec(new String[]{pythonBin, "-c", pyCmd});
            int exitCode = process.waitFor();
            tempXz.delete();

            if (exitCode != 0) {
                throw new IOException("Python download process exited with code " + exitCode);
            }
            logAppender.accept("[INFO] Successfully downloaded frida-gadget to: " + gadgetFile.getAbsolutePath());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }
    }

    public String downloadAndPushFridaServer(jadx.gui.device.adb.ADBDevice device) {
        try {
            String abi = jadx.gui.device.adb.AdbService.execShell(device, "getprop ro.product.cpu.abi").trim();
            String arch = FridaUtils.mapAbiToFridaArch(abi);
            String version = FridaUtils.getLocalFridaVersion();

            String binaryName = "frida-server-" + version;
            File localDir = new File(System.getProperty("user.home"), ".jadx/frida");
            if (!localDir.exists()) {
                localDir.mkdirs();
            }
            File localBinary = new File(localDir, binaryName + "-android-" + arch);

            if (!localBinary.exists()) {
                logAppender.accept("[INFO] Downloading frida-server " + version + " for " + arch + "...");
                String downloadUrl = "https://github.com/frida/frida/releases/download/" + version + "/frida-server-" + version + "-android-" + arch + ".xz";
                File tempXz = File.createTempFile("frida_server_", ".xz");

                String pythonBin = new File("/usr/bin/python3").exists() ? "/usr/bin/python3" : "python3";

                String pyCmd = "import urllib.request, lzma; urllib.request.urlretrieve('" + downloadUrl + "', '" + tempXz.getAbsolutePath() + "'); " +
                        "open('" + localBinary.getAbsolutePath() + "', 'wb').write(lzma.open('" + tempXz.getAbsolutePath() + "').read())";

                Process process = Runtime.getRuntime().exec(new String[]{pythonBin, "-c", pyCmd});
                int exitCode = process.waitFor();
                tempXz.delete();

                if (exitCode != 0) {
                    throw new IOException("Python download process exited with code " + exitCode);
                }
                logAppender.accept("[INFO] Successfully downloaded frida-server binary to host: " + localBinary.getAbsolutePath());
            }

            // Push to device
            logAppender.accept("[INFO] Pushing frida-server to /data/local/tmp/" + binaryName + "...");
            String adbPath = jadx.gui.device.adb.AdbService.detectAdbPath();
            Process pushProc = Runtime.getRuntime().exec(new String[]{
                    adbPath, "-s", device.getSerial(), "push", localBinary.getAbsolutePath(), "/data/local/tmp/" + binaryName
            });
            int pushExit = pushProc.waitFor();
            if (pushExit != 0) {
                throw new IOException("ADB push failed with exit code " + pushExit);
            }
            logAppender.accept("[INFO] Successfully pushed frida-server to device.");
            return binaryName;
        } catch (Exception e) {
            LOG.error("Failed to download and push frida-server", e);
            logAppender.accept("[ERROR] Failed to download and push frida-server: " + e.getMessage());
            return null;
        }
    }
}
