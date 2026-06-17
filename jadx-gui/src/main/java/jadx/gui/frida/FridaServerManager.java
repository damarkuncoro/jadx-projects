package jadx.gui.frida;

import jadx.gui.device.adb.ADBDevice;
import jadx.gui.device.adb.AdbService;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Manages Frida server operations on connected Android devices.
 */
public class FridaServerManager {
    private static final Logger LOG = LoggerFactory.getLogger(FridaServerManager.class);

    private final MainWindow mainWindow;
    private final JadxSettings settings;
    private final FridaDownloader downloader;
    private final Consumer<String> logAppender;

    public FridaServerManager(
            MainWindow mainWindow,
            JadxSettings settings,
            FridaDownloader downloader,
            Consumer<String> logAppender
    ) {
        this.mainWindow = mainWindow;
        this.settings = settings;
        this.downloader = downloader;
        this.logAppender = logAppender;
    }

    public boolean autoStartFridaServer() {
        try {
            logAppender.accept("[INFO] Checking connected devices for running frida-server...");
            String host = settings.getAdbDialogHost();
            if (host == null || host.isEmpty()) {
                host = "localhost";
            }
            int port = 5037;
            try {
                port = Integer.parseInt(settings.getAdbDialogPort());
            } catch (Exception ex) {
                // use default
            }

            List<ADBDevice> devices = AdbService.listDevices(host, port);
            if (devices.isEmpty()) {
                logAppender.accept("[WARN] No connected Android devices detected via ADB.");
                return false;
            }

            boolean isReady = false;
            for (ADBDevice device : devices) {
                String serial = device.getSerial();
                logAppender.accept("[INFO] Inspecting device: " + serial);

                // Determine device architecture & local Frida version
                String abi = AdbService.execShell(device, "getprop ro.product.cpu.abi").trim();
                String arch = FridaUtils.mapAbiToFridaArch(abi);

                // Check if the device is rooted
                boolean isRooted = false;
                try {
                    String suTest = AdbService.execShell(device, "which su");
                    if (suTest != null && suTest.contains("su")) {
                        isRooted = true;
                    }
                } catch (Exception ex) {
                    // su not found or exec fails
                }

                if (!isRooted) {
                    logAppender.accept("[WARN] Device " + serial + " is not rooted. Automated frida-server startup is not supported on jailed devices.");

                    // Setup port forwarding
                    String adbPath = AdbService.detectAdbPath();
                    try {
                        Process forwardProc = Runtime.getRuntime().exec(new String[]{
                                adbPath, "-s", serial, "forward", "tcp:27042", "tcp:27042"
                        });
                    } catch (Exception e) {
                        LOG.error("Failed to set up port forwarding", e);
                    }
                    continue;
                }

                // Check if frida-server is already running
                boolean isRunning = false;
                try {
                    String psOutput = AdbService.execShell(device, "ps");
                    if (psOutput != null && psOutput.contains("frida-server")) {
                        isRunning = true;
                    }
                } catch (Exception e) {
                    // ignore
                }

                if (isRunning) {
                    logAppender.accept("[INFO] Frida server is already running on " + serial + ".");
                    isReady = true;
                    continue;
                }

                // Try to download and push frida-server
                String binaryName = downloader.downloadAndPushFridaServer(device);
                if (binaryName == null) {
                    continue;
                }

                // Start frida-server on device
                logAppender.accept("[INFO] Starting frida-server on " + serial + "...");
                try {
                    String adbPath = AdbService.detectAdbPath();
                    // We'll start frida-server in background with nohup
                    Process startProc = Runtime.getRuntime().exec(new String[]{
                            adbPath, "-s", serial, "shell", "su", "-c",
                            "cd /data/local/tmp && chmod 755 " + binaryName + " && nohup ./" + binaryName + " >/dev/null 2>&1 &"
                    });
                    int exitCode = startProc.waitFor();
                    if (exitCode != 0) {
                        logAppender.accept("[WARN] Could not start frida-server automatically on " + serial + ". You may need to start it manually.");
                    } else {
                        // Wait a second to let server start
                        Thread.sleep(1000);
                        logAppender.accept("[INFO] Frida server started successfully on " + serial + "!");
                        isReady = true;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logAppender.accept("[ERROR] Frida server startup interrupted on " + serial);
                } catch (Exception e) {
                    LOG.error("Failed to start frida-server on " + serial, e);
                    logAppender.accept("[ERROR] Failed to start frida-server on " + serial + ": " + e.getMessage());
                }
            }

            if (!isReady) {
                logAppender.accept("[WARN] No devices with running frida-server found. You may need to start it manually.");
            }

            return isReady;
        } catch (Exception ex) {
            LOG.error("Failed to auto-start frida-server", ex);
            logAppender.accept("[ERROR] Failed to auto-start frida-server: " + ex.getMessage());
            return false;
        }
    }
}
