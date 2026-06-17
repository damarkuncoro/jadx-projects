package jadx.gui.buildstack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data class yang merepresentasikan hasil deteksi framework.
 */
public class FrameworkDetection {
	private final String name;
	private final String status;
	private final String confidence;
	private final List<String> evidence;

	public FrameworkDetection(String name, String status, String confidence, List<String> evidence) {
		this.name = name;
		this.status = status;
		this.confidence = confidence;
		this.evidence = evidence;
	}

	/**
	 * Menambahkan prefix ke setiap bukti evidence.
	 */
	public FrameworkDetection withEvidencePrefix(String prefix) {
		List<String> prefixedEvidence = evidence.stream()
				.map(item -> EvidenceUtils.toExportedEvidence(item, prefix))
				.collect(Collectors.toList());
		return new FrameworkDetection(name, status, confidence, prefixedEvidence);
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public String getConfidence() {
		return confidence;
	}

	public List<String> getEvidence() {
		return evidence;
	}

	public boolean isDetected() {
		return "DETECTED".equals(status);
	}

	/**
	 * Mengkonversi objek ke Map untuk keperluan serialisasi.
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", name);
		map.put("status", status);
		map.put("confidence", confidence);
		map.put("evidence", evidence);
		return map;
	}
}
