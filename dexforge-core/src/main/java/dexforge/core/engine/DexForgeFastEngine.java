package dexforge.core.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dexforge.api.engine.DexForgeEngine;
import dexforge.api.model.DexForgeApkMetadata;
import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.decompiler.JavaDecompiler;
import dexforge.core.parser.dex.decompiler.model.JavaClass;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.service.DexProject;
import dexforge.core.parser.dex.service.DexFastIndexer;

public final class DexForgeFastEngine implements DexForgeEngine {
    private DexProject project;
    private List<File> inputFiles;
    private JavaDecompiler decompiler;

    @Override
    public String getEngineId() {
        return "fast";
    }

    @Override
    public void init(List<File> inputFiles, Map<String, Object> settings) {
        this.inputFiles = inputFiles;
    }

    @Override
    public void load() {
        ApkLoader loader = new ApkLoader();
        try {
            if (!inputFiles.isEmpty()) {
                this.project = loader.load(inputFiles.get(0));
                if (!project.getIndexers().isEmpty()) {
                    this.decompiler = new JavaDecompiler(project.getIndexers().get(0));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load APK with Fast engine", e);
        }
    }

    @Override
    public List<Object> getRawClasses() {
        if (project == null) return Collections.emptyList();
        return project.getAllClasses().stream().map(c -> (Object) c).collect(Collectors.toList());
    }

    @Override
    public List<Object> getRawPackages() {
        // Return package names as strings for now
        if (project == null) return Collections.emptyList();
        return new ArrayList<>(project.getPackageTree().keySet());
    }

    @Override
    public List<Object> getClassesInPackage(Object rawPackage) {
        if (project == null || !(rawPackage instanceof String)) return Collections.emptyList();
        String pkg = (String) rawPackage;
        return project.getAllClasses().stream()
                .filter(c -> getPackageName(c.getName()).equals(pkg))
                .map(c -> (Object) c)
                .collect(Collectors.toList());
    }

    private String getPackageName(String fullName) {
        if (fullName.startsWith("L") && fullName.endsWith(";")) {
            fullName = fullName.substring(1, fullName.length() - 1);
        }
        int lastSlash = fullName.lastIndexOf('/');
        return (lastSlash == -1) ? "" : fullName.substring(0, lastSlash).replace('/', '.');
    }

    @Override
    public List<Object> getSubPackages(Object rawPackage) {
        return Collections.emptyList(); // Simplified
    }

    @Override
    public List<Object> getRawResources() {
        return Collections.emptyList(); // Simplified
    }

    @Override
    public String getName(Object rawNode) {
        if (rawNode instanceof DexClass) {
            String name = ((DexClass) rawNode).getName();
            int lastSlash = name.lastIndexOf('/');
            return (lastSlash == -1) ? name : name.substring(lastSlash + 1, name.length() - 1);
        }
        return String.valueOf(rawNode);
    }

    @Override
    public String getFullName(Object rawNode) {
        if (rawNode instanceof DexClass) {
            return ((DexClass) rawNode).getName();
        }
        return String.valueOf(rawNode);
    }

    @Override
    public String getCode(Object rawNode) {
        if (rawNode instanceof DexClass && decompiler != null) {
            JavaClass javaClass = decompiler.decompileClass((DexClass) rawNode);
            return javaClass.toCode();
        }
        return getSmali(rawNode);
    }

    @Override
    public String getSmali(Object rawNode) {
        if (rawNode instanceof DexClass && !project.getIndexers().isEmpty()) {
            DexFastIndexer indexer = project.getIndexers().get(0);
            return indexer.getSmali((DexClass) rawNode);
        }
        return "";
    }

    @Override
    public String getResourceText(Object rawResource) {
        return "";
    }

    @Override
    public String decodeBinaryXml(byte[] bytes) {
        return "";
    }

    @Override
    public List<Object> getMethods(Object rawClass) {
        return Collections.emptyList();
    }

    @Override
    public List<Object> getFields(Object rawClass) {
        return Collections.emptyList();
    }

    @Override
    public List<Object> getInnerClasses(Object rawClass) {
        return Collections.emptyList();
    }

    @Override
    public String getSuperClass(Object rawClass) {
        if (rawClass instanceof DexClass) return ((DexClass) rawClass).getSuperclass();
        return "java.lang.Object";
    }

    @Override
    public List<String> getInterfaces(Object rawClass) {
        if (rawClass instanceof DexClass) return ((DexClass) rawClass).getInterfaces();
        return Collections.emptyList();
    }

    @Override
    public String getReturnType(Object rawMethod) { return "void"; }
    @Override
    public List<String> getArgumentTypes(Object rawMethod) { return Collections.emptyList(); }
    @Override
    public boolean isConstructor(Object rawMethod) { return false; }
    @Override
    public String getFieldType(Object rawField) { return "Object"; }

    @Override
    public int getModifiers(Object rawNode) {
        if (rawNode instanceof DexClass) return ((DexClass) rawNode).getAccessFlags();
        return 0;
    }

    @Override
    public void rename(Object rawNode, String newName) {}
    @Override
    public void removeAlias(Object rawNode) {}

    @Override
    public List<Object> getUseIn(Object rawNode) {
        return Collections.emptyList();
    }

    @Override
    public Object getNodeAt(Object rawClass, int position) { return null; }
    @Override
    public Object getAnnotationAt(Object rawCodeInfo, int position) { return null; }
    @Override
    public Map<Integer, Object> getAnnotations(Object rawCodeInfo) { return Collections.emptyMap(); }
    @Override
    public String getAnnotationType(Object rawAnnotation) { return ""; }
    @Override
    public String getAnnotationData(Object rawAnnotation) { return ""; }
    @Override
    public int getDefinitionPosition(Object rawNode) { return 0; }

    @Override
    public Object searchClass(String fullName) {
        return project.findClass(fullName);
    }

    @Override
    public DexForgeApkMetadata getApkMetadata() {
        return project.getMetadata();
    }

	@Override
	public Map<String, String> calculateFingerprint() {
        return Collections.emptyMap();
    }

    @Override
    public void close() {}
}
