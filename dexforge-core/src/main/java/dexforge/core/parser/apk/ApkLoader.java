package dexforge.core.parser.apk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.HashMap;

import dexforge.api.model.DexForgeApkMetadata;
import dexforge.core.parser.dex.service.DexProject;
import dexforge.core.parser.arsc.service.ArscFastIndexer;
import dexforge.core.parser.axml.service.AxmlFastIndexer;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.resolver.ResourceResolver;
import dexforge.core.parser.native_lib.ElfParser;
import dexforge.core.parser.native_lib.model.ElfSymbol;
import dexforge.core.parser.analysis.jni.JniBridgeMapper;
import dexforge.core.parser.analysis.strings.StringPatternAnalyzer;
import dexforge.core.parser.axml.service.manifest.ManifestAnalyzer;
import dexforge.core.parser.analysis.callgraph.CallGraphAnalyzer;
import dexforge.core.parser.analysis.callgraph.model.CallGraphNode;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Orchestrates the loading of an APK file.
 * Extracts DEX files, resources, and manifest for the project.
 */
public final class ApkLoader {
	private final ResourceResolver resourceResolver = new ResourceResolver();
	private final Map<String, List<String>> discoveredPatterns = new HashMap<>();
	private final List<String> activities = new ArrayList<>();
	private final List<String> permissions = new ArrayList<>();
	private final Map<String, String> jniBridges = new HashMap<>();
	private final JniBridgeMapper jniMapper = new JniBridgeMapper();
	private final List<AxmlNode> layouts = new ArrayList<>();
	private final Map<String, CallGraphNode> globalCallGraph = new HashMap<>();
	private final List<DexFastIndexer> indexers = new ArrayList<>();
	private ManifestAnalyzer manifestAnalyzer;

	public DexProject load(File apkFile) throws Exception {
		DexProject project = new DexProject();

		if (apkFile.getName().endsWith(".dex")) {
			byte[] data = java.nio.file.Files.readAllBytes(apkFile.toPath());
			project.addDex(data);

			DexFastIndexer indexer = new DexFastIndexer(data, resourceResolver);
			indexers.add(indexer);

			CallGraphAnalyzer cga = new CallGraphAnalyzer(indexer);
			globalCallGraph.putAll(cga.build());

			StringPatternAnalyzer spa = new StringPatternAnalyzer(indexer);
			spa.analyze().forEach((label, matches) ->
					discoveredPatterns.computeIfAbsent(label, k -> new java.util.ArrayList<>()).addAll(matches)
			);
			return project;
		}

		try (ZipFile zip = new ZipFile(apkFile)) {
			Enumeration<? extends ZipEntry> entries = zip.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();

				if (name.endsWith(".dex")) {
					byte[] data = readEntry(zip, entry);
					project.addDex(data);

					// Index with shared resource resolver
					DexFastIndexer indexer = new DexFastIndexer(data, resourceResolver);
					indexers.add(indexer);

					// Build global call graph
					CallGraphAnalyzer cga = new CallGraphAnalyzer(indexer);
					globalCallGraph.putAll(cga.build());

					// Perform pattern analysis
					StringPatternAnalyzer spa = new StringPatternAnalyzer(indexer);
					spa.analyze().forEach((label, matches) ->
							discoveredPatterns.computeIfAbsent(label, k -> new java.util.ArrayList<>()).addAll(matches)
					);
				} else if (name.equals("AndroidManifest.xml")) {
					byte[] data = readEntry(zip, entry);
					AxmlFastIndexer axml = new AxmlFastIndexer(data);
					axml.parse();

					if (axml.getRootNode() != null) {
						this.manifestAnalyzer = new ManifestAnalyzer(axml.getRootNode());
						activities.addAll(manifestAnalyzer.getActivities());
						permissions.addAll(axml.getPermissions());
					}

					project.setMetadata(new DexForgeApkMetadata(
							axml.getPackageName(),
							"Unknown", 0, // version
							0, 0, // sdk
							axml.getPermissions(),
							new HashMap<>()
					));
				} else if (name.equals("resources.arsc")) {
					byte[] data = readEntry(zip, entry);
					ArscFastIndexer arsc = new ArscFastIndexer(data);
					arsc.parse();
					resourceResolver.addMappings(arsc.getIdToNameMap());
				} else if (name.endsWith(".so")) {
					byte[] data = readEntry(zip, entry);
					ElfParser elf = new ElfParser(data);
					List<ElfSymbol> symbols = elf.parseSymbols();
					jniBridges.putAll(jniMapper.buildBridgeMap(symbols));
				} else if (name.startsWith("res/layout/") && name.endsWith(".xml")) {
					byte[] data = readEntry(zip, entry);
					AxmlFastIndexer axml = new AxmlFastIndexer(data);
					axml.parse();
					if (axml.getRootNode() != null) {
						layouts.add(axml.getRootNode());
					}
				}
			}
		}

		return project;
	}

	private byte[] readEntry(ZipFile zip, ZipEntry entry) throws Exception {
		try (InputStream is = zip.getInputStream(entry)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int len;
			while ((len = is.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			return bos.toByteArray();
		}
	}

	public ResourceResolver getResourceResolver() {
		return resourceResolver;
	}

	public Map<String, List<String>> getDiscoveredPatterns() {
		return discoveredPatterns;
	}

	public List<String> getActivities() {
		return activities;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public ManifestAnalyzer getManifestAnalyzer() {
		return manifestAnalyzer;
	}

	public Map<String, String> getJniBridges() {
		return jniBridges;
	}

	public Map<String, CallGraphNode> getGlobalCallGraph() {
		return globalCallGraph;
	}

	public List<AxmlNode> getLayouts() {
		return layouts;
	}

	public List<DexFastIndexer> getIndexers() {
		return indexers;
	}
}
