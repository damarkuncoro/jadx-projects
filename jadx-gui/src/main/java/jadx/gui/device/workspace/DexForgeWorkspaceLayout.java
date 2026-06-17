package jadx.gui.device.workspace;

import java.io.File;

public final class DexForgeWorkspaceLayout {
	private static final String APKS_DIR = "apks";
	private static final String DECOMPILED_DIR = "decompiled";
	private static final String REPORTS_DIR = "reports";
	private static final String PULL_REPORT = "pull-report.json";
	private static final String DECOMPILE_REPORT = "decompile-report.json";
	private static final String MANIFEST_REPORT = "manifest.json";
	private static final String SECURITY_REPORT = "security.json";

	private final File rootDir;

	public DexForgeWorkspaceLayout(String outDir) {
		this(new File(outDir));
	}

	public DexForgeWorkspaceLayout(File rootDir) {
		this.rootDir = rootDir;
	}

	public File getRootDir() {
		return rootDir;
	}

	public File getApksDir() {
		return new File(rootDir, APKS_DIR);
	}

	public File getDecompiledDir() {
		return new File(rootDir, DECOMPILED_DIR);
	}

	public File getReportsDir() {
		return new File(rootDir, REPORTS_DIR);
	}

	public File getPullReportFile() {
		return new File(getReportsDir(), PULL_REPORT);
	}

	public File getDecompileReportFile() {
		return new File(getReportsDir(), DECOMPILE_REPORT);
	}

	public File getManifestReportFile() {
		return new File(getReportsDir(), MANIFEST_REPORT);
	}

	public File getSecurityReportFile() {
		return new File(getReportsDir(), SECURITY_REPORT);
	}

	public String getDecompiledDirName() {
		return DECOMPILED_DIR;
	}

	public String getPullReportPath() {
		return reportPath(PULL_REPORT);
	}

	public String getDecompileReportPath() {
		return reportPath(DECOMPILE_REPORT);
	}

	public String getManifestReportPath() {
		return reportPath(MANIFEST_REPORT);
	}

	public String getSecurityReportPath() {
		return reportPath(SECURITY_REPORT);
	}

	public String apkPath(String fileName) {
		return APKS_DIR + "/" + fileName;
	}

	private String reportPath(String fileName) {
		return REPORTS_DIR + "/" + fileName;
	}
}
