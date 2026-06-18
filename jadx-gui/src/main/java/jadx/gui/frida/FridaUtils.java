package jadx.gui.frida;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JavaClass;
import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import jadx.gui.ui.MainWindow;

/**
 * Utility helper methods for Frida integration.
 */
public class FridaUtils {
	private static final Logger LOG = LoggerFactory.getLogger(FridaUtils.class);

	private FridaUtils() {
	}

	public static String getLocalFridaVersion() {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "frida", "--version" });
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				if (line != null) {
					return line.trim();
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to detect local frida version", e);
		}
		return "17.12.0"; // default fallback
	}

	public static String mapAbiToFridaArch(String abi) {
		if (abi == null) {
			return "arm64";
		}
		abi = abi.toLowerCase();
		if (abi.contains("arm64") || abi.contains("aarch64")) {
			return "arm64";
		} else if (abi.contains("arm") || abi.contains("abi")) {
			return "arm";
		} else if (abi.contains("x86_64") || abi.contains("amd64")) {
			return "x86_64";
		} else if (abi.contains("x86") || abi.contains("i386") || abi.contains("i686")) {
			return "x86";
		}
		return "arm64"; // fallback
	}

	public static String resolveTargetPackage(MainWindow mainWindow) {
		try {
			String pkg = mainWindow.getWrapper().getDecompiler().getRoot().getAppPackage();
			if (pkg != null && !pkg.trim().isEmpty()) {
				LOG.info("[FridaPanel] Resolved target package from root appPackage: {}", pkg);
				return pkg.trim();
			}
		} catch (Exception ex) {
			LOG.warn("[FridaPanel] Failed to get appPackage from root", ex);
		}

		try {
			List<ResourceFile> resources = mainWindow.getWrapper().getDecompiler().getResources();
			for (ResourceFile rf : resources) {
				if (rf.getType() == ResourceType.MANIFEST) {
					String content = rf.loadContent().getText().getCodeStr();
					Pattern pattern = Pattern.compile("package\\s*=\\s*\"([^\"]+)\"");
					Matcher matcher = pattern.matcher(content);
					if (matcher.find()) {
						String pkg = matcher.group(1).trim();
						LOG.info("[FridaPanel] Resolved target package from AndroidManifest: {}", pkg);
						return pkg;
					}
				}
			}
		} catch (Exception ex) {
			LOG.warn("[FridaPanel] Failed to parse AndroidManifest.xml for package name", ex);
		}

		try {
			List<JavaClass> classes = mainWindow.getWrapper().getClasses();
			if (!classes.isEmpty()) {
				String firstClassPkg = classes.get(0).getFullName();
				int lastDot = firstClassPkg.lastIndexOf('.');
				if (lastDot != -1) {
					String pkg = firstClassPkg.substring(0, lastDot);
					LOG.info("[FridaPanel] Resolved target package fallback from first class: {}", pkg);
					return pkg;
				}
			}
		} catch (Exception ex) {
			LOG.warn("[FridaPanel] Failed to get fallback package from classes", ex);
		}

		return "";
	}

	public static String findApktoolPath() {
		File optHb = new File("/opt/homebrew/bin/apktool");
		if (optHb.exists()) {
			return optHb.getAbsolutePath();
		}
		File usrLoc = new File("/usr/local/bin/apktool");
		if (usrLoc.exists()) {
			return usrLoc.getAbsolutePath();
		}
		return "apktool";
	}

	public static String findBuildToolsBinary(String binaryName) {
		File buildToolsDir = new File(System.getProperty("user.home"), "Library/Android/sdk/build-tools");
		if (buildToolsDir.exists()) {
			File[] versions = buildToolsDir.listFiles(File::isDirectory);
			if (versions != null && versions.length > 0) {
				java.util.Arrays.sort(versions, (a, b) -> b.getName().compareTo(a.getName()));
				File binary = new File(versions[0], binaryName);
				if (binary.exists()) {
					return binary.getAbsolutePath();
				}
			}
		}
		return binaryName;
	}

	public static void deleteDirectory(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDirectory(f);
				} else {
					f.delete();
				}
			}
		}
		dir.delete();
	}

	public static int runCommand(String[] command, String taskName, Consumer<String> logAppender) throws Exception {
		LOG.info("Running command for {}: {}", taskName, String.join(" ", command));
		logAppender.accept("[INFO] Running " + taskName + "...");
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String finalLine = line;
				logAppender.accept("[" + taskName + "] " + finalLine);
				LOG.info("[{}] {}", taskName, line);
			}
		}
		return process.waitFor();
	}

	public static boolean isLauncherActivity(org.w3c.dom.Element element) {
		org.w3c.dom.NodeList intentFilters = element.getElementsByTagName("intent-filter");
		for (int j = 0; j < intentFilters.getLength(); j++) {
			org.w3c.dom.Element filter = (org.w3c.dom.Element) intentFilters.item(j);
			boolean hasMain = false;
			boolean hasLauncher = false;
			org.w3c.dom.NodeList actions = filter.getElementsByTagName("action");
			for (int k = 0; k < actions.getLength(); k++) {
				if ("android.intent.action.MAIN".equals(((org.w3c.dom.Element) actions.item(k)).getAttribute("android:name"))) {
					hasMain = true;
				}
			}
			org.w3c.dom.NodeList categories = filter.getElementsByTagName("category");
			for (int k = 0; k < categories.getLength(); k++) {
				if ("android.intent.category.LAUNCHER".equals(((org.w3c.dom.Element) categories.item(k)).getAttribute("android:name"))) {
					hasLauncher = true;
				}
			}
			if (hasMain && hasLauncher) {
				return true;
			}
		}
		return false;
	}

	public static String getFullyQualifiedClassName(org.w3c.dom.Document doc, String className) {
		if (className == null || className.isEmpty()) {
			return className;
		}
		if (className.contains(".") && !className.startsWith(".")) {
			return className;
		}
		String pkg = doc.getDocumentElement().getAttribute("package");
		if (pkg == null || pkg.isEmpty()) {
			return className;
		}
		if (className.startsWith(".")) {
			return pkg + className;
		}
		return pkg + "." + className;
	}

	public static String findMainActivityFromJadx(MainWindow mainWindow) {
		try {
			List<ResourceFile> resources = mainWindow.getWrapper().getDecompiler().getResources();
			for (ResourceFile rf : resources) {
				if (rf.getType() == ResourceType.MANIFEST) {
					String content = rf.loadContent().getText().getCodeStr();
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
					org.w3c.dom.Document doc = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(content)));

					// Try <activity> nodes first
					org.w3c.dom.NodeList activityNodes = doc.getElementsByTagName("activity");
					for (int i = 0; i < activityNodes.getLength(); i++) {
						org.w3c.dom.Element activity = (org.w3c.dom.Element) activityNodes.item(i);
						if (isLauncherActivity(activity)) {
							String name = activity.getAttribute("android:name");
							return getFullyQualifiedClassName(doc, name);
						}
					}

					// Try <activity-alias> nodes if not found in <activity>
					org.w3c.dom.NodeList aliasNodes = doc.getElementsByTagName("activity-alias");
					for (int i = 0; i < aliasNodes.getLength(); i++) {
						org.w3c.dom.Element alias = (org.w3c.dom.Element) aliasNodes.item(i);
						if (isLauncherActivity(alias)) {
							String target = alias.getAttribute("android:targetActivity");
							return getFullyQualifiedClassName(doc, target);
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to parse JADX manifest for main activity", e);
		}
		return null;
	}

	public static File findSmaliFile(File decompiledDir, String className) {
		String relativePath = className.replace('.', '/') + ".smali";
		File[] subDirs = decompiledDir.listFiles(f -> f.isDirectory() && f.getName().startsWith("smali"));
		if (subDirs != null) {
			for (File dir : subDirs) {
				File smaliFile = new File(dir, relativePath);
				if (smaliFile.exists()) {
					return smaliFile;
				}
			}
		}
		return null;
	}

	public static java.nio.file.Path findBaseApkPath(List<java.nio.file.Path> filePaths) {
		if (filePaths.size() == 1) {
			return filePaths.get(0);
		}
		// Look for exact "base.apk"
		for (java.nio.file.Path p : filePaths) {
			if (p.getFileName().toString().equalsIgnoreCase("base.apk")) {
				return p;
			}
		}
		// Look for filename containing "base"
		for (java.nio.file.Path p : filePaths) {
			if (p.getFileName().toString().toLowerCase().contains("base")) {
				return p;
			}
		}
		// Look for filename NOT containing "split"
		for (java.nio.file.Path p : filePaths) {
			if (!p.getFileName().toString().toLowerCase().contains("split")) {
				return p;
			}
		}
		// Fallback to the first one
		return filePaths.get(0);
	}
}
