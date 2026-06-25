package dexforge.cli.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import dexforge.cli.JCommanderWrapper;
import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.analysis.patterns.ByteArrayDecoder;
import dexforge.core.parser.analysis.patterns.SecurityTrapDetector;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "scan APK for security checks, traps, and hardcoded keys")
public class CommandScan implements ICommand {

	@Parameter(description = "<apk file>")
	public List<String> apkPaths = new ArrayList<>();

	@Parameter(names = {"-h", "--help"}, description = "print this help", help = true)
	private boolean printHelp = false;

	@Override
	public String name() {
		return "scan";
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

			System.out.println("Initiating Security Analysis...");
			int dexIndex = 0;
			int totalKeys = 0;
			int totalTraps = 0;

			for (DexFastIndexer indexer : loader.getIndexers()) {
				System.out.println("\nAnalyzing DEX #" + dexIndex + "...");

				// 1. Scan for Byte Arrays (ASCII Keys)
				ByteArrayDecoder arrayDecoder = new ByteArrayDecoder(indexer);
				List<ByteArrayDecoder.DecodedArray> arrays = arrayDecoder.decodeStaticArrays();
				if (!arrays.isEmpty()) {
					System.out.println("  [+] Found " + arrays.size() + " potential hardcoded keys/seeds:");
					for (ByteArrayDecoder.DecodedArray arr : arrays) {
						System.out.printf("      - Method: %s\n", arr.getMethodSignature());
						System.out.printf("        Size: %d bytes\n", arr.getSize());
						System.out.printf("        Decoded ASCII: %s\n", arr.getAscii());
						System.out.print("        Hex bytes: ");
						for (byte b : arr.getData()) {
							System.out.printf("%02x ", b);
						}
						System.out.println("\n");
					}
					totalKeys += arrays.size();
				}

				// 2. Scan for Security Traps
				SecurityTrapDetector trapDetector = new SecurityTrapDetector(indexer);
				List<SecurityTrapDetector.TrapWarning> traps = trapDetector.scanForTraps();
				if (!traps.isEmpty()) {
					System.out.println("  [!] Found " + traps.size() + " potential environment/security checks crash traps:");
					for (SecurityTrapDetector.TrapWarning warning : traps) {
						System.out.printf("      - Method: %s\n", warning.getMethodSignature());
						System.out.printf("        Offset: %d\n", warning.getOffset());
						System.out.printf("        Description: %s\n", warning.getDescription());
						System.out.printf("        Suggestion: %s\n\n", warning.getSuggestion());
					}
					totalTraps += traps.size();
				}

				dexIndex++;
			}

			System.out.println("====================================================");
			System.out.println("Scan complete.");
			System.out.println("Total hardcoded keys found: " + totalKeys);
			System.out.println("Total crash traps found: " + totalTraps);
			System.out.println("====================================================");

		} catch (Exception e) {
			System.err.println("Error during scanning: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
