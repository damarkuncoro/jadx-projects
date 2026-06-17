package jadx.gui.buildstack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data class yang menyimpan informasi lengkap tentang build stack.
 */
public class BuildStackInfo {
	private final String summary;
	private final Map<String, Object> buildMetadata;
	private final Map<String, String> manifest;
	private final List<FrameworkDetection> frameworks;
	private final Map<String, String> libraryVersions;
	private final List<String> evidence;

	public BuildStackInfo(
			String summary,
			Map<String, Object> buildMetadata,
			Map<String, String> manifest,
			List<FrameworkDetection> frameworks,
			Map<String, String> libraryVersions,
			List<String> evidence) {
		this.summary = summary;
		this.buildMetadata = buildMetadata;
		this.manifest = manifest;
		this.frameworks = frameworks;
		this.libraryVersions = libraryVersions;
		this.evidence = evidence;
	}

	/**
	 * Menambahkan prefix ke setiap bukti evidence.
	 */
	public BuildStackInfo withEvidencePrefix(String prefix) {
		List<FrameworkDetection> prefixedFrameworks = frameworks.stream()
				.map(framework -> framework.withEvidencePrefix(prefix))
				.collect(Collectors.toList());
		List<String> prefixedEvidence = evidence.stream()
				.map(item -> EvidenceUtils.toExportedEvidence(item, prefix))
				.collect(Collectors.toList());
		return new BuildStackInfo(summary, buildMetadata, manifest, prefixedFrameworks, libraryVersions, prefixedEvidence);
	}

	public String getSummary() {
		return summary;
	}

	public Map<String, Object> getBuildMetadata() {
		return buildMetadata;
	}

	public Map<String, String> getManifest() {
		return manifest;
	}

	public List<FrameworkDetection> getFrameworks() {
		return frameworks;
	}

	public List<FrameworkDetection> getDetectedFrameworks() {
		return frameworks.stream()
				.filter(FrameworkDetection::isDetected)
				.collect(Collectors.toList());
	}

	public List<FrameworkDetection> getNotDetectedFrameworks() {
		return frameworks.stream()
				.filter(framework -> !framework.isDetected())
				.collect(Collectors.toList());
	}

	public Map<String, String> getLibraryVersions() {
		return libraryVersions;
	}

	public boolean isEmpty() {
		return buildMetadata.isEmpty()
				&& manifest.isEmpty()
				&& libraryVersions.isEmpty()
				&& getDetectedFrameworks().isEmpty();
	}

	/**
	 * Mengkonversi objek ke Map untuk keperluan serialisasi.
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("summary", summary);
		map.put("buildMetadata", buildMetadata);
		map.put("manifest", manifest);
		map.put("frameworks", frameworks.stream().map(FrameworkDetection::toMap).collect(Collectors.toList()));
		map.put("libraryVersions", libraryVersions);
		map.put("evidence", evidence);
		return map;
	}
}
