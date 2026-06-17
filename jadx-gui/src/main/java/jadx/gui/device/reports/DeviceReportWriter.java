package jadx.gui.device.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class DeviceReportWriter {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private DeviceReportWriter() {}

	public static void writeJson(File file, Object value) throws IOException {
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(value, writer);
		}
	}

	public static void writeManifestReport(File decompiledDir, File manifestReportFile) throws IOException {
		File manifestFile = findFile(decompiledDir, "AndroidManifest.xml");
		Map<String, Object> manifestReport = new LinkedHashMap<>();
		manifestReport.put("status", manifestFile == null ? "MISSING" : "FOUND");
		if (manifestFile != null) {
			manifestReport.put("path", decompiledDir.toPath().relativize(manifestFile.toPath()).toString());
			manifestReport.put("absolutePath", manifestFile.getAbsolutePath());
		}
		writeJson(manifestReportFile, manifestReport);
	}

	public static void writeDecompileReport(List<File> apkFiles, File decompiledDir, File reportFile, long duration) throws IOException {
		Map<String, Object> report = new LinkedHashMap<>();
		report.put("outputPath", decompiledDir.getAbsolutePath());
		report.put("durationMs", duration);
		report.put("status", "COMPLETE");
		report.put("jobsSkipped", 0);

		List<String> inputs = new ArrayList<>();
		for (File f : apkFiles) {
			inputs.add(f.getName());
		}
		report.put("inputApkFiles", inputs);
		report.put("generatedAt", Instant.now().toString());

		writeJson(reportFile, report);
	}

	private static File findFile(File dir, String fileName) {
		if (dir == null || !dir.exists()) {
			return null;
		}
		File[] files = dir.listFiles();
		if (files == null) {
			return null;
		}
		for (File file : files) {
			if (file.isFile() && fileName.equals(file.getName())) {
				return file;
			}
			if (file.isDirectory()) {
				File found = findFile(file, fileName);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
