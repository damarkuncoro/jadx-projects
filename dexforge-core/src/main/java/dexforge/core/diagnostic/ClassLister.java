package dexforge.core.diagnostic;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.io.File;

public class ClassLister {
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			return;
		}
		ApkLoader loader = new ApkLoader();
		loader.load(new File(args[0]));
		for (DexFastIndexer indexer : loader.getIndexers()) {
			for (DexClass clazz : indexer.getClasses()) {
				System.out.println(clazz.getName());
			}
		}
	}
}
