package dexforge.core.diagnostic;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.model.*;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.smali.service.SmaliWriter;
import java.io.File;

/**
 * REUSEABLE tool to dump Smali code for a specific class/method from any APK.
 */
public class MethodDumper {
	public static void main(String[] args) throws Exception {
		if (args.length < 2 || args[0].isEmpty() || args[1].isEmpty()) {
			System.out.println("Usage: java MethodDumper <apk_path> <class_name> [method_name]");
			return;
		}

		String apkPath = args[0];
		String targetClass = args[1];
		String targetMethod = (args.length > 2 && !args[2].isEmpty()) ? args[2] : null;

		ApkLoader loader = new ApkLoader();
		loader.load(new File(apkPath));

		for (DexFastIndexer indexer : loader.getIndexers()) {
			for (DexClass clazz : indexer.getClasses()) {
				if (clazz.getName().equals(targetClass)) {
					indexer.fillClassData(clazz);
					SmaliWriter writer = new SmaliWriter(indexer);
					String smali = writer.writeClass(clazz);

					if (targetMethod == null) {
						System.out.println(smali);
					} else {
						dumpSpecificMethod(smali, targetMethod);
					}
					return;
				}
			}
		}
	}

	private static void dumpSpecificMethod(String classSmali, String methodName) {
		String[] lines = classSmali.split("\n");
		boolean printing = false;
		for (String line : lines) {
			if (line.startsWith(".method") && line.contains(" " + methodName + "(")) {
				printing = true;
			}
			if (printing) {
				System.out.println(line);
			}
			if (line.equals(".end method")) {
				printing = false;
			}
		}
	}
}
