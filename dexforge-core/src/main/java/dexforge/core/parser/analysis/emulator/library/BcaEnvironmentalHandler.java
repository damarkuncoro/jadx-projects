package dexforge.core.parser.analysis.emulator.library;

import java.util.List;
import java.io.File;

/**
 * Hooks Android-specific environmental calls that BCA obfuscation checks.
 */
public final class BcaEnvironmentalHandler implements VirtualMethodHandler {
	private final File apkFile;

	public BcaEnvironmentalHandler(File apkFile) {
		this.apkFile = apkFile;
	}

	@Override
	public boolean canHandle(String sig) {
		return sig.contains("Landroid/os/Process;->") ||
			   sig.contains("Landroid/os/SystemClock;->") ||
			   sig.contains("Landroid/view/ViewConfiguration;->") ||
			   sig.contains("Ljava/util/zip/ZipFile;->") ||
			   sig.contains("Landroid/content/Context;->") ||
			   sig.contains("Landroid/content/pm/ApplicationInfo;->") ||
			   sig.contains("Landroid/os/Build;->") ||
			   sig.contains("Landroid/provider/Settings$Secure;->getString");
	}

	@Override
	public Object execute(String sig, List<Object> args) throws Exception {
		if (sig.contains("myPid")) return 1234;
		if (sig.contains("myUid")) return 10001;
		if (sig.contains("myTid")) return 1234;
		if (sig.contains("uptimeMillis")) return 5000L;
		if (sig.contains("elapsedRealtime")) return 6000L;
		if (sig.contains("elapsedRealtimeNanos")) return 6000000000L;
		if (sig.contains("ViewConfiguration;->getTapTimeout()I")) return 500;
		if (sig.contains("getJumpTapTimeout")) return 500;
		if (sig.contains("getGlobalActionKeyTimeout")) return 500L;
		if (sig.contains("getMaximumFlingVelocity")) return 8000;

		if (sig.contains("ZipFile;-><init>(Ljava/io/File;)V")) {
			return new java.util.zip.ZipFile(apkFile);
		}

		if (sig.contains("ZipFile;-><init>(Ljava/lang/String;)V")) {
			return new java.util.zip.ZipFile(apkFile);
		}

		if (sig.contains("getEntry")) {
			return ((java.util.zip.ZipFile) args.get(0)).getEntry((String) args.get(1));
		}

		if (sig.contains("getInputStream")) {
			return ((java.util.zip.ZipFile) args.get(0)).getInputStream((java.util.zip.ZipEntry) args.get(1));
		}

		if (sig.contains("getApplicationInfo")) {
			return "com.bca.mobile";
		}

		if (sig.contains("Landroid/os/Build;->MODEL:Ljava/lang/String;")) return "Pixel 7";
		if (sig.contains("Landroid/os/Build;->MANUFACTURER:Ljava/lang/String;")) return "Google";

		if (sig.contains("Ljava/lang/System;->getProperty(Ljava/lang/String;)Ljava/lang/String;")) {
			String prop = (String) args.get(0);
			if (prop.equals("java.vm.version")) return "2.1.0";
			if (prop.equals("os.arch")) return "arm64";
			return "";
		}

		if (sig.contains("Landroid/provider/Settings$Secure;->getString")) {
			// Often looks for "android_id"
			return "35f7a2210b64d12a";
		}

		return null;
	}
}
