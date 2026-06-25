package dexforge.cli.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import dexforge.cli.JCommanderWrapper;
import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.analysis.deobf.DeobfuscationPatcher;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Parameters(commandDescription = "deobfuscate APK strings and generate patched smali")
public class CommandDeobfuscate implements ICommand {

	@Parameter(description = "<apk file>")
	public List<String> apkPaths = new ArrayList<>();

	@Parameter(names = {"-o", "--output"}, description = "output directory for patched smali files")
	public String outputDir = "out_deobf";

	@Parameter(names = {"-h", "--help"}, description = "print this help", help = true)
	private boolean printHelp = false;

	@Override
	public String name() {
		return "deobf";
	}

	@Override
	public void process(JCommanderWrapper jcw, JCommander subCommander) {
		if (printHelp || apkPaths.isEmpty()) {
			jcw.printUsage(subCommander);
			return;
		}

		String apkPath = apkPaths.get(0);
		File apkFile = new File(apkPath);
		if (!apkFile.exists()) {
			System.err.println("Error: APK file not found: " + apkPath);
			return;
		}

		try {
			System.out.println("Loading APK: " + apkPath);
			ApkLoader loader = new ApkLoader();
			loader.load(apkFile);

			int totalPatched = 0;
			int dexIndex = 0;
			for (DexFastIndexer indexer : loader.getIndexers()) {
				System.out.println("Analyzing DEX #" + (dexIndex++));
				DeobfuscationPatcher patcher = new DeobfuscationPatcher(indexer, loader.getIndexers(), apkFile);
				Map<String, String> patchedClasses = patcher.patchAll();

				if (patchedClasses.isEmpty()) {
					System.out.println("  No obfuscated strings found in this DEX.");
					continue;
				}

				System.out.println("  Found " + patchedClasses.size() + " classes to patch.");
				savePatchedSmali(patchedClasses);
				totalPatched += patchedClasses.size();
			}

			if (totalPatched > 0) {
				System.out.println("Deobfuscation complete. Total patched classes: " + totalPatched);
				System.out.println("Patched Smali files saved to: " + outputDir);
			} else {
				System.out.println("Finished. No obfuscation patterns detected.");
			}

		} catch (Exception e) {
			System.err.println("Error during deobfuscation: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void savePatchedSmali(Map<String, String> patchedClasses) {
		File outBase = new File(outputDir);
		if (!outBase.exists()) {
			outBase.mkdirs();
		}

		for (Map.Entry<String, String> entry : patchedClasses.entrySet()) {
			String className = entry.getKey();
			String smali = entry.getValue();

			// Convert Lcom/example/Test; to com/example/Test.smali
			String pathPart = className.substring(1, className.length() - 1);
			File outFile = new File(outBase, pathPart + ".smali");

			try {
				File parent = outFile.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}
				Files.write(outFile.toPath(), smali.getBytes(StandardCharsets.UTF_8));
			} catch (Exception e) {
				System.err.println("Failed to save " + outFile.getAbsolutePath() + ": " + e.getMessage());
			}
		}
	}
}
