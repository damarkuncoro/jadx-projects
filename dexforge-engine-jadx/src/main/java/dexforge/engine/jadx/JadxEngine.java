package dexforge.engine.jadx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dexforge.api.engine.DexForgeEngine;
import dexforge.api.intelligence.IProjectIntelligence;
import dexforge.api.plugin.IDexForgePlugin;
import dexforge.api.ui.IUiEditor;
import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.service.intelligence.ProjectIntelligenceService;
import dexforge.core.service.ui.VisualUiEditorService;
import dexforge.engine.jadx.utils.JadxIntegrityUtils;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.JavaPackage;
import jadx.api.ResourceFile;
import jadx.api.ICodeInfo;
import jadx.api.metadata.ICodeAnnotation;

/**
 * Implementation of DexForgeEngine using JADX.
 * Integrated as a Pluggable component.
 */
public final class JadxEngine implements DexForgeEngine, IDexForgePlugin {
	private JadxDecompiler decompiler;
	private JadxArgs args;
	private IProjectIntelligence intelligence;

	@Override
	public String getName() { return "JADX Engine"; }

	@Override
	public String getDescription() { return "Provides industry-standard decompilation using JADX"; }

	@Override
	public String getVersion() { return "1.5.0"; }

	@Override
	public void onInitialize(Map<String, Object> context) {
		// Plugin-specific init
	}

	@Override
	public void onShutdown() {
		if (decompiler != null) decompiler.close();
	}

	@Override
	public String getEngineId() {
		return "jadx";
	}

	@Override
	public IProjectIntelligence getIntelligence() {
		return intelligence;
	}

	@Override
	public IUiEditor getUiEditor(Object rootNode) {
		if (rootNode instanceof AxmlNode) {
			// In real app, we need the resolver from ProjectIntelligenceService or similar
			return new VisualUiEditorService((AxmlNode) rootNode, null);
		}
		return null;
	}

	@Override
	public void init(List<File> inputFiles, Map<String, Object> settings) {
		this.args = new JadxArgs();
		this.args.getInputFiles().addAll(inputFiles);

		if (settings.containsKey("threadsCount")) {
			args.setThreadsCount((Integer) settings.get("threadsCount"));
		}
		if (settings.containsKey("commentsLevel")) {
			args.setCommentsLevel(jadx.api.CommentsLevel.valueOf(((String) settings.get("commentsLevel")).toUpperCase()));
		}
		if (settings.containsKey("decompilationMode")) {
			args.setDecompilationMode(jadx.api.DecompilationMode.valueOf(((String) settings.get("decompilationMode")).toUpperCase()));
		}
		if (settings.containsKey("deobfuscationOn")) {
			args.setDeobfuscationOn((Boolean) settings.get("deobfuscationOn"));
		}

		this.decompiler = new JadxDecompiler(args);

		// Initialize Intelligence from dexforge-core
		if (!inputFiles.isEmpty()) {
			File first = inputFiles.get(0);
			if (first.getName().endsWith(".apk")) {
				try {
					ApkLoader loader = new ApkLoader();
					loader.load(first);
					this.intelligence = new ProjectIntelligenceService(loader);
				} catch (Exception ignored) {}
			}
		}
	}

	@Override
	public void setCodeCache(Object codeCache) {
		if (codeCache instanceof dexforge.api.model.IDexForgeCodeCache) {
			dexforge.api.model.IDexForgeCodeCache dfCache = (dexforge.api.model.IDexForgeCodeCache) codeCache;
			args.setCodeCache(new jadx.api.ICodeCache() {
				@Override
				public void add(String clsFullName, jadx.api.ICodeInfo codeInfo) {
					dfCache.add(clsFullName, new dexforge.api.model.DexForgeCodeInfo(codeInfo, JadxEngine.this));
				}

				@Override
				public void remove(String clsFullName) {
					dfCache.remove(clsFullName);
				}

				@Override
				public jadx.api.ICodeInfo get(String clsFullName) {
					return dfCache.get(clsFullName)
							.map(info -> (jadx.api.ICodeInfo) info.unwrap())
							.orElse(jadx.api.ICodeInfo.EMPTY);
				}

				@Override
				public String getCode(String clsFullName) {
					return dfCache.get(clsFullName).map(dexforge.api.model.ICodeInfo::getCodeStr).orElse(null);
				}

				@Override
				public boolean contains(String clsFullName) {
					return dfCache.contains(clsFullName);
				}

				@Override
				public void close() throws java.io.IOException {
					dfCache.close();
				}
			});
		}
	}

	@Override
	public void load() {
		decompiler.load();
	}

	@Override
	public List<Object> getRawClasses() {
		return new ArrayList<>(decompiler.getClasses());
	}

	@Override
	public List<Object> getRawPackages() {
		// Jadx has hierarchical packages, but we start from root
		return decompiler.getPackages().stream()
				.filter(JavaPackage::isRoot)
				.map(p -> (Object) p)
				.collect(Collectors.toList());
	}

	@Override
	public List<Object> getClassesInPackage(Object rawPackage) {
		if (rawPackage instanceof JavaPackage) {
			return new ArrayList<>(((JavaPackage) rawPackage).getClasses());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public List<Object> getSubPackages(Object rawPackage) {
		if (rawPackage instanceof JavaPackage) {
			return new ArrayList<>(((JavaPackage) rawPackage).getSubPackages());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public List<Object> getRawResources() {
		return new ArrayList<>(decompiler.getResources());
	}

	@Override
	public String getName(Object rawNode) {
		if (rawNode instanceof JavaNode) {
			return ((JavaNode) rawNode).getName();
		}
		if (rawNode instanceof JavaPackage) {
			return ((JavaPackage) rawNode).getName();
		}
		if (rawNode instanceof ResourceFile) {
			return ((ResourceFile) rawNode).getDeobfName();
		}
		return rawNode.toString();
	}

	@Override
	public String getFullName(Object rawNode) {
		if (rawNode instanceof JavaNode) {
			return ((JavaNode) rawNode).getFullName();
		}
		if (rawNode instanceof JavaPackage) {
			return ((JavaPackage) rawNode).getFullName();
		}
		return getName(rawNode);
	}

	@Override
	public String getCode(Object rawNode) {
		if (rawNode instanceof JavaClass) {
			return ((JavaClass) rawNode).getCode();
		}
		if (rawNode instanceof JavaMethod) {
			return ((JavaMethod) rawNode).getCodeStr();
		}
		return "";
	}

	@Override
	public String getSmali(Object rawNode) {
		if (rawNode instanceof JavaClass) {
			return ((JavaClass) rawNode).getClassNode().getDisassembledCode();
		}
		return "";
	}

	@Override
	public String getResourceText(Object rawResource) {
		if (rawResource instanceof ResourceFile) {
			ResourceFile res = (ResourceFile) rawResource;
			return res.loadContent().getText().getCodeStr();
		}
		return "";
	}

	@Override
	public String decodeBinaryXml(byte[] bytes) {
		try {
			return new jadx.core.xmlgen.BinaryXMLParser(decompiler.getRoot())
					.parse(new java.io.ByteArrayInputStream(bytes))
					.getCodeStr();
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public List<Object> getMethods(Object rawClass) {
		if (rawClass instanceof JavaClass) {
			return new ArrayList<>(((JavaClass) rawClass).getMethods());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public List<Object> getFields(Object rawClass) {
		if (rawClass instanceof JavaClass) {
			return new ArrayList<>(((JavaClass) rawClass).getFields());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public List<Object> getInnerClasses(Object rawClass) {
		if (rawClass instanceof JavaClass) {
			return new ArrayList<>(((JavaClass) rawClass).getInnerClasses());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public String getSuperClass(Object rawClass) {
		if (rawClass instanceof JavaClass) {
			JavaClass cls = (JavaClass) rawClass;
			// Jadx doesn't have a direct getSuperClass returning String,
			// it uses getSuperClass returning JavaClass or access through ClassNode
			return cls.getClassNode().getSuperClass().toString();
		}
		return "java.lang.Object";
	}

	@Override
	public List<String> getInterfaces(Object rawClass) {
		if (rawClass instanceof JavaClass) {
			return ((JavaClass) rawClass).getClassNode().getInterfaces().stream()
					.map(Object::toString)
					.collect(Collectors.toList());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public String getReturnType(Object rawMethod) {
		if (rawMethod instanceof JavaMethod) {
			return ((JavaMethod) rawMethod).getReturnType().toString();
		}
		return "void";
	}

	@Override
	public List<String> getArgumentTypes(Object rawMethod) {
		if (rawMethod instanceof JavaMethod) {
			return ((JavaMethod) rawMethod).getArguments().stream()
					.map(Object::toString)
					.collect(Collectors.toList());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public boolean isConstructor(Object rawMethod) {
		if (rawMethod instanceof JavaMethod) {
			return ((JavaMethod) rawMethod).isConstructor();
		}
		return false;
	}

	@Override
	public String getFieldType(Object rawField) {
		if (rawField instanceof JavaField) {
			return ((JavaField) rawField).getType().toString();
		}
		return "Object";
	}

	@Override
	public int getModifiers(Object rawNode) {
		if (rawNode instanceof JavaClass) {
			return ((JavaClass) rawNode).getAccessInfo().rawValue();
		}
		if (rawNode instanceof JavaMethod) {
			return ((JavaMethod) rawNode).getAccessFlags().rawValue();
		}
		if (rawNode instanceof JavaField) {
			return ((JavaField) rawNode).getAccessFlags().rawValue();
		}
		return 0;
	}

	@Override
	public void rename(Object rawNode, String newName) {
		if (rawNode instanceof JavaClass) {
			((JavaClass) rawNode).getClassNode().rename(newName);
		} else if (rawNode instanceof JavaMethod) {
			((JavaMethod) rawNode).getMethodNode().rename(newName);
		} else if (rawNode instanceof JavaField) {
			((JavaField) rawNode).getFieldNode().rename(newName);
		} else if (rawNode instanceof JavaPackage) {
			((JavaPackage) rawNode).rename(newName);
		}
	}

	@Override
	public void removeAlias(Object rawNode) {
		if (rawNode instanceof JavaNode) {
			((JavaNode) rawNode).removeAlias();
		} else if (rawNode instanceof JavaPackage) {
			((JavaPackage) rawNode).removeAlias();
		}
	}

	@Override
	public List<Object> getUseIn(Object rawNode) {
		if (rawNode instanceof JavaClass) {
			return new ArrayList<>(((JavaClass) rawNode).getUseIn());
		}
		if (rawNode instanceof JavaMethod) {
			return new ArrayList<>(((JavaMethod) rawNode).getUseIn());
		}
		if (rawNode instanceof JavaField) {
			return new ArrayList<>(((JavaField) rawNode).getUseIn());
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public Object getNodeAt(Object rawClass, int position) {
		if (!(rawClass instanceof JavaClass)) {
			return null;
		}
		JavaClass cls = (JavaClass) rawClass;
		// JadxDecompiler has method to find node at position
		return decompiler.getJavaNodeAtPosition(cls.getCodeInfo(), position);
	}

	@Override
	public Object getAnnotationAt(Object rawCodeInfo, int position) {
		if (rawCodeInfo instanceof ICodeInfo) {
			return ((ICodeInfo) rawCodeInfo).getCodeMetadata().getAt(position);
		}
		return null;
	}

	@Override
	public Map<Integer, Object> getAnnotations(Object rawCodeInfo) {
		if (rawCodeInfo instanceof ICodeInfo) {
			return ((ICodeInfo) rawCodeInfo).getCodeMetadata().getAsMap().entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue()));
		}
		return java.util.Collections.emptyMap();
	}

	@Override
	public String getAnnotationType(Object rawAnnotation) {
		if (rawAnnotation instanceof ICodeAnnotation) {
			return ((ICodeAnnotation) rawAnnotation).getAnnType().name();
		}
		return "UNKNOWN";
	}

	@Override
	public String getAnnotationData(Object rawAnnotation) {
		return rawAnnotation != null ? rawAnnotation.toString() : null;
	}

	@Override
	public int getDefinitionPosition(Object rawNode) {
		if (rawNode instanceof JavaNode) {
			return ((JavaNode) rawNode).getDefPos();
		}
		return 0;
	}

	@Override
	public Object searchClass(String fullName) {
		return decompiler.searchJavaClassByAliasFullName(fullName);
	}

	@Override
	public dexforge.api.model.DexForgeApkMetadata getApkMetadata() {
		jadx.core.dex.nodes.RootNode root = decompiler.getRoot();
		String pkg = root.getAppPackage();

		// In a real implementation, we would extract these from the manifest attributes
		// For now, providing a solid structure with available data
		return new dexforge.api.model.DexForgeApkMetadata(
				pkg != null ? pkg : "unknown",
				"1.0", // Version Name placeholder
				1,     // Version Code placeholder
				21,    // Min SDK placeholder
				33,    // Target SDK placeholder
				java.util.Collections.emptyList(), // Permissions placeholder
				java.util.Collections.emptyMap()   // Attributes placeholder
		);
	}

	@Override
	public Map<String, String> calculateFingerprint() {
		return JadxIntegrityUtils.calculateFingerprint(decompiler);
	}

	@Override
	public void close() {
		if (decompiler != null) {
			decompiler.close();
		}
	}

	public dexforge.engine.DexForgeProjectSession getProjectSession() {
		if (decompiler == null) {
			return null;
		}
		dexforge.domain.model.project.Project proj = dexforge.domain.model.project.Project.create(
				dexforge.domain.model.project.ProjectId.generate(),
				dexforge.domain.model.project.ProjectConfig.create("JADX Project", ""),
				args.getInputFiles().get(0).toPath()
		);
		return new dexforge.engine.jadx.session.JadxProjectSession(decompiler, proj);
	}
}
