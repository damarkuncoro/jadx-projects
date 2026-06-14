package jadx.gui.ui.treenodes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jadx.api.ICodeInfo;
import jadx.api.ResourceFile;
import jadx.api.ResourcesLoader;
import jadx.api.impl.SimpleCodeInfo;
import jadx.core.dex.attributes.IAttributeNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.ProcessState;
import jadx.core.utils.ErrorsCounter;
import jadx.core.utils.Utils;
import jadx.gui.JadxWrapper;
import jadx.gui.treemodel.JClass;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.panel.ContentPanel;
import jadx.gui.ui.panel.HtmlPanel;
import jadx.gui.ui.tab.TabbedPane;
import jadx.gui.utils.UiUtils;

public class SummaryNode extends JNode {
	private static final long serialVersionUID = 4295299814582784805L;

	private static final ImageIcon ICON = UiUtils.openSvgIcon("nodes/detailView");

	private final MainWindow mainWindow;
	private final JadxWrapper wrapper;

	public SummaryNode(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		this.wrapper = mainWindow.getWrapper();
	}

	@Override
	public ICodeInfo getCodeInfo() {
		StringEscapeUtils.Builder builder = StringEscapeUtils.builder(StringEscapeUtils.ESCAPE_HTML4);
		try {
			builder.append("<html>");
			builder.append("<body>");
			writeInputSummary(builder);
			writeBuildStackSummary(builder);
			writeDecompilationSummary(builder);
			builder.append("</body>");
		} catch (Exception e) {
			builder.append("Error build summary: ");
			builder.append("<pre>");
			builder.append(Utils.getStackTrace(e));
			builder.append("</pre>");
		}
		return new SimpleCodeInfo(builder.toString());
	}

	private void writeInputSummary(StringEscapeUtils.Builder builder) throws IOException {
		builder.append("<h2>Input</h2>");
		builder.append("<h3>Files</h3>");
		builder.append("<ul>");
		for (File inputFile : wrapper.getArgs().getInputFiles()) {
			builder.append("<li>");
			builder.escape(inputFile.getCanonicalFile().getAbsolutePath());
			builder.append("</li>");
		}
		builder.append("</ul>");

		List<ClassNode> classes = wrapper.getRootNode().getClasses(true);
		List<String> codeSources = classes.stream()
				.map(ClassNode::getInputFileName)
				.distinct()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());
		codeSources.remove("synthetic");
		int codeSourcesCount = codeSources.size();
		builder.append("<h3>Code sources</h3>");
		builder.append("<ul>");
		if (codeSourcesCount != 1) {
			builder.append("<li>Count: " + codeSourcesCount + "</li>");
		}
		for (String input : codeSources) {
			builder.append("<li>");
			builder.escape(input);
			builder.append("</li>");
		}
		builder.append("</ul>");

		addNativeLibsInfo(builder);

		int methodsCount = classes.stream().mapToInt(cls -> cls.getMethods().size()).sum();
		int fieldsCount = classes.stream().mapToInt(cls -> cls.getFields().size()).sum();
		int insnCount = classes.stream().flatMap(cls -> cls.getMethods().stream()).mapToInt(MethodNode::getInsnsCount).sum();
		builder.append("<h3>Counts</h3>");
		builder.append("<ul>");
		builder.append("<li>Classes: " + classes.size() + "</li>");
		builder.append("<li>Methods: " + methodsCount + "</li>");
		builder.append("<li>Fields: " + fieldsCount + "</li>");
		builder.append("<li>Instructions: " + insnCount + " (units)</li>");
		builder.append("</ul>");
	}

	private void addNativeLibsInfo(StringEscapeUtils.Builder builder) {
		List<String> nativeLibs = wrapper.getResources().stream()
				.map(ResourceFile::getOriginalName)
				.filter(f -> f.endsWith(".so"))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());
		builder.append("<h3>Native libs</h3>");
		builder.append("<ul>");
		if (nativeLibs.isEmpty()) {
			builder.append("<li>Total count: 0</li>");
		} else {
			Map<String, Set<String>> libsByArch = new HashMap<>();
			for (String libFile : nativeLibs) {
				String[] parts = StringUtils.split(libFile, '/');
				int count = parts.length;
				if (count >= 2) {
					String arch = parts[count - 2];
					String name = parts[count - 1];
					libsByArch.computeIfAbsent(arch, (a) -> new HashSet<>())
							.add(name);
				}
			}
			String arches = libsByArch.keySet().stream()
					.sorted(Comparator.naturalOrder())
					.collect(Collectors.joining(", "));
			builder.append("<li>Arch list: " + arches + "</li>");

			String perArchCount = libsByArch.entrySet().stream()
					.map(entry -> entry.getKey() + ":" + entry.getValue().size())
					.sorted(Comparator.naturalOrder())
					.collect(Collectors.joining(", "));
			builder.append("<li>Per arch count: " + perArchCount + "</li>");

			builder.append("<br>");
			builder.append("<li>Total count: " + nativeLibs.size() + "</li>");
			for (String lib : nativeLibs) {
				builder.append("<li>");
				builder.escape(lib);
				builder.append("</li>");
			}
		}
		builder.append("</ul>");
	}

	private void writeBuildStackSummary(StringEscapeUtils.Builder builder) {
		Map<String, String> manifest = parseManifest();
		Map<String, String> buildMetadata = parseKotlinToolingMetadata();
		Map<String, String> libraryVersions = collectLibraryVersions();
		List<String> frameworks = detectFrameworks();

		if (manifest.isEmpty() && buildMetadata.isEmpty() && libraryVersions.isEmpty() && frameworks.isEmpty()) {
			return;
		}

		builder.append("<h2>Build Stack</h2>");
		builder.append("<ul>");
		if (!frameworks.isEmpty()) {
			builder.append("<li>Detected: ");
			builder.escape(String.join(", ", frameworks));
			builder.append("</li>");
		}
		if (!buildMetadata.isEmpty()) {
			builder.append("<li>Build system: ");
			builder.escape(buildMetadata.getOrDefault("buildSystem", "Unknown"));
			if (buildMetadata.containsKey("buildSystemVersion")) {
				builder.append(" ");
				builder.escape(buildMetadata.get("buildSystemVersion"));
			}
			builder.append("</li>");
			if (buildMetadata.containsKey("buildPlugin")) {
				builder.append("<li>Build plugin: ");
				builder.escape(buildMetadata.get("buildPlugin"));
				if (buildMetadata.containsKey("buildPluginVersion")) {
					builder.append(" ");
					builder.escape(buildMetadata.get("buildPluginVersion"));
				}
				builder.append("</li>");
			}
			if (buildMetadata.containsKey("platformType")) {
				builder.append("<li>Platform: ");
				builder.escape(buildMetadata.get("platformType"));
				builder.append("</li>");
			}
			if (buildMetadata.containsKey("sourceCompatibility") || buildMetadata.containsKey("targetCompatibility")) {
				builder.append("<li>Java compatibility: source ");
				builder.escape(buildMetadata.getOrDefault("sourceCompatibility", "unknown"));
				builder.append(", target ");
				builder.escape(buildMetadata.getOrDefault("targetCompatibility", "unknown"));
				builder.append("</li>");
			}
		}
		if (!manifest.isEmpty()) {
			addManifestItem(builder, manifest, "Package", "package");
			addManifestItem(builder, manifest, "Version", "versionName");
			addManifestItem(builder, manifest, "Version code", "versionCode");
			addManifestItem(builder, manifest, "Compile SDK", "compileSdkVersion");
			addManifestItem(builder, manifest, "Min SDK", "minSdkVersion");
			addManifestItem(builder, manifest, "Target SDK", "targetSdkVersion");
			addManifestItem(builder, manifest, "Application", "applicationName");
		}
		builder.append("</ul>");

		if (!libraryVersions.isEmpty()) {
			builder.append("<h3>Library versions</h3>");
			builder.append("<ul>");
			libraryVersions.entrySet().stream()
					.filter(entry -> isImportantLibrary(entry.getKey()))
					.forEach(entry -> {
						builder.append("<li>");
						builder.escape(entry.getKey());
						builder.append(": ");
						builder.escape(entry.getValue());
						builder.append("</li>");
					});
			builder.append("<li>Total metadata entries: " + libraryVersions.size() + "</li>");
			builder.append("</ul>");
		}
	}

	private static void addManifestItem(StringEscapeUtils.Builder builder, Map<String, String> manifest, String title, String key) {
		String value = manifest.get(key);
		if (value == null) {
			return;
		}
		builder.append("<li>");
		builder.escape(title);
		builder.append(": ");
		builder.escape(value);
		builder.append("</li>");
	}

	private Map<String, String> parseKotlinToolingMetadata() {
		Map<String, String> metadata = new LinkedHashMap<>();
		String content = readResourceAsString("kotlin-tooling-metadata.json");
		if (content == null) {
			return metadata;
		}
		try {
			JsonObject root = JsonParser.parseString(content).getAsJsonObject();
			copyJsonString(root, metadata, "buildSystem");
			copyJsonString(root, metadata, "buildSystemVersion");
			copyJsonString(root, metadata, "buildPlugin");
			copyJsonString(root, metadata, "buildPluginVersion");
			if (root.has("projectTargets") && root.get("projectTargets").isJsonArray()
					&& root.getAsJsonArray("projectTargets").size() > 0) {
				JsonObject target = root.getAsJsonArray("projectTargets").get(0).getAsJsonObject();
				copyJsonString(target, metadata, "target");
				copyJsonString(target, metadata, "platformType");
				if (target.has("extras") && target.getAsJsonObject("extras").has("android")) {
					JsonObject android = target.getAsJsonObject("extras").getAsJsonObject("android");
					copyJsonString(android, metadata, "sourceCompatibility");
					copyJsonString(android, metadata, "targetCompatibility");
				}
			}
		} catch (Exception ignored) {
			// Keep summary rendering best-effort.
		}
		return metadata;
	}

	private static void copyJsonString(JsonObject root, Map<String, String> target, String key) {
		if (root.has(key) && root.get(key).isJsonPrimitive()) {
			target.put(key, root.get(key).getAsString());
		}
	}

	private Map<String, String> parseManifest() {
		Map<String, String> manifest = new LinkedHashMap<>();
		String content = readResourceAsString("AndroidManifest.xml");
		if (content == null) {
			return manifest;
		}
		putXmlAttr(content, manifest, "package", "package");
		putXmlAttr(content, manifest, "versionName", "android:versionName");
		putXmlAttr(content, manifest, "versionCode", "android:versionCode");
		putXmlAttr(content, manifest, "compileSdkVersion", "android:compileSdkVersion");
		putXmlAttr(content, manifest, "minSdkVersion", "android:minSdkVersion");
		putXmlAttr(content, manifest, "targetSdkVersion", "android:targetSdkVersion");
		putApplicationName(content, manifest);
		return manifest;
	}

	private static void putXmlAttr(String content, Map<String, String> target, String key, String attrName) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(java.util.regex.Pattern.quote(attrName) + "=\"([^\"]+)\"");
		java.util.regex.Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			target.put(key, matcher.group(1));
		}
	}

	private static void putApplicationName(String content, Map<String, String> manifest) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<application\\b[^>]*\\bandroid:name=\"([^\"]+)\"",
				java.util.regex.Pattern.DOTALL);
		java.util.regex.Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			manifest.put("applicationName", matcher.group(1));
		}
	}

	private Map<String, String> collectLibraryVersions() {
		Map<String, String> versions = new LinkedHashMap<>();
		wrapper.getResources().stream()
				.filter(res -> getResourceName(res).startsWith("META-INF/"))
				.filter(res -> getResourceName(res).endsWith(".version"))
				.sorted(Comparator.comparing(this::getResourceName))
				.forEach(res -> {
					String name = getResourceName(res);
					String library = name.substring("META-INF/".length(), name.length() - ".version".length());
					String version = readResourceAsString(res);
					if (version != null) {
						versions.put(library, version.trim());
					}
				});
		return versions;
	}

	private List<String> detectFrameworks() {
		Set<String> resourceNames = wrapper.getResources().stream()
				.map(this::getResourceName)
				.collect(Collectors.toSet());
		Set<String> classNames = wrapper.getRootNode().getClasses(true).stream()
				.map(ClassNode::getClassInfo)
				.map(clsInfo -> clsInfo.getRawName().replace('.', '/'))
				.collect(Collectors.toSet());

		List<String> frameworks = new ArrayList<>();
		if (resourceNames.contains("AndroidManifest.xml")) {
			frameworks.add("Native Android");
		}
		if (resourceNames.contains("flutter_assets/NOTICES.Z") || containsResource(resourceNames, "libflutter.so")) {
			frameworks.add("Flutter");
		}
		if (resourceNames.contains("assets/index.android.bundle")
				|| classNames.contains("com/facebook/react/ReactActivity")
				|| classNames.contains("com/facebook/react/ReactNativeHost")) {
			frameworks.add("React Native");
		}
		if (containsResource(resourceNames, "libunity.so") || classNames.contains("com/unity3d/player/UnityPlayerActivity")) {
			frameworks.add("Unity");
		}
		if (resourceNames.contains("assets/www/cordova.js") || classNames.contains("org/apache/cordova/CordovaActivity")) {
			frameworks.add("Cordova");
		}
		if (resourceNames.contains("assets/capacitor.config.json") || classNames.contains("com/getcapacitor/BridgeActivity")) {
			frameworks.add("Capacitor");
		}
		if (classNames.contains("mono/android/Runtime") || containsResource(resourceNames, "libmonodroid.so")) {
			frameworks.add("Xamarin");
		}
		if (resourceNames.contains("kotlin-tooling-metadata.json") || classNames.stream().anyMatch(cls -> cls.startsWith("kotlin/"))) {
			frameworks.add("Kotlin");
		}
		if (resourceNames.contains("META-INF/androidx.core_core-ktx.version")
				|| classNames.stream().anyMatch(cls -> cls.startsWith("androidx/"))) {
			frameworks.add("AndroidX / Jetpack");
		}
		return frameworks;
	}

	private static boolean containsResource(Set<String> resourceNames, String fileName) {
		return resourceNames.stream().anyMatch(name -> name.endsWith('/' + fileName));
	}

	private static boolean isImportantLibrary(String name) {
		return name.startsWith("androidx.core")
				|| name.startsWith("androidx.appcompat")
				|| name.startsWith("androidx.databinding")
				|| name.startsWith("androidx.navigation")
				|| name.startsWith("androidx.room")
				|| name.startsWith("androidx.camera")
				|| name.startsWith("kotlinx_coroutines")
				|| name.startsWith("com.google.android.material");
	}

	private String readResourceAsString(String resourceName) {
		ResourceFile resource = findResource(resourceName);
		return resource == null ? null : readResourceAsString(resource);
	}

	private String readResourceAsString(ResourceFile resource) {
		try {
			return ResourcesLoader.decodeStream(resource, (size, is) -> new String(is.readAllBytes(), StandardCharsets.UTF_8));
		} catch (Exception e) {
			return null;
		}
	}

	private ResourceFile findResource(String resourceName) {
		return wrapper.getResources().stream()
				.filter(resource -> getResourceName(resource).equals(resourceName))
				.findFirst()
				.orElse(null);
	}

	private String getResourceName(ResourceFile resource) {
		String name = resource.getOriginalName();
		int zipSeparator = name.indexOf(':');
		if (zipSeparator != -1 && zipSeparator + 1 < name.length()) {
			return name.substring(zipSeparator + 1);
		}
		return name;
	}

	private void writeDecompilationSummary(StringEscapeUtils.Builder builder) {
		builder.append("<h2>Decompilation</h2>");
		List<ClassNode> classes = wrapper.getRootNode().getClassesWithoutInner();
		int classesCount = classes.size();
		long notLoadedClasses = classes.stream().filter(c -> c.getState() == ProcessState.NOT_LOADED).count();
		long loadedClasses = classes.stream().filter(c -> c.getState() == ProcessState.LOADED).count();
		long processedClasses = classes.stream().filter(c -> c.getState() == ProcessState.PROCESS_COMPLETE).count();
		long generatedClasses = classes.stream().filter(c -> c.getState() == ProcessState.GENERATED_AND_UNLOADED).count();
		builder.append("<ul>");
		builder.append("<li>Top level classes: " + classesCount + "</li>");
		builder.append("<li>Not loaded: " + valueAndPercent(notLoadedClasses, classesCount) + "</li>");
		builder.append("<li>Loaded: " + valueAndPercent(loadedClasses, classesCount) + "</li>");
		builder.append("<li>Processed: " + valueAndPercent(processedClasses, classesCount) + "</li>");
		builder.append("<li>Code generated: " + valueAndPercent(generatedClasses, classesCount) + "</li>");
		builder.append("</ul>");

		ErrorsCounter counter = wrapper.getRootNode().getErrorsCounter();
		Set<IAttributeNode> problemNodes = new HashSet<>();
		problemNodes.addAll(counter.getErrorNodes());
		problemNodes.addAll(counter.getWarnNodes());
		long problemMethods = problemNodes.stream().filter(MethodNode.class::isInstance).count();
		int methodsCount = classes.stream().mapToInt(cls -> cls.getMethods().size()).sum();
		double methodSuccessRate = (methodsCount - problemMethods) * 100.0 / (double) methodsCount;

		builder.append("<h3>Issues</h3>");
		builder.append("<ul>");
		builder.append("<li>Errors: " + counter.getErrorCount() + "</li>");
		builder.append("<li>Warnings: " + counter.getWarnsCount() + "</li>");
		builder.append("<li>Nodes with errors: " + counter.getErrorNodes().size() + "</li>");
		builder.append("<li>Nodes with warnings: " + counter.getWarnNodes().size() + "</li>");
		builder.append("<li>Total nodes with issues: " + problemNodes.size() + "</li>");
		builder.append("<li>Methods with issues: " + problemMethods + "</li>");
		builder.append("<li>Methods success rate: " + String.format("%.2f", methodSuccessRate) + "%</li>");
		builder.append("</ul>");
	}

	private String valueAndPercent(long value, int total) {
		return String.format("%d (%.2f%%)", value, value * 100 / ((double) total));
	}

	@Override
	public boolean hasContent() {
		return true;
	}

	@Override
	public ContentPanel getContentPanel(TabbedPane tabbedPane) {
		return new HtmlPanel(tabbedPane, this);
	}

	@Override
	public String makeString() {
		return "Summary";
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}

	@Override
	public JClass getJParent() {
		return null;
	}
}
