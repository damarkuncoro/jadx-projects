package jadx.gui.device.adb.parsers;

import java.util.ArrayList;
import java.util.List;

import jadx.gui.device.adb.AdbPackage;

public final class PackageListParser {
	private PackageListParser() {
	}

	public static List<AdbPackage> parse(String output, String filterType) {
		List<AdbPackage> packages = new ArrayList<>();
		if (output == null) {
			return packages;
		}
		for (String line : output.split("\n")) {
			line = line.trim();
			if (line.isEmpty() || !line.startsWith("package:")) {
				continue;
			}
			int lastEq = line.lastIndexOf('=');
			if (lastEq > 0) {
				String rawPath = line.substring("package:".length(), lastEq);
				String pkgName = line.substring(lastEq + 1);

				AdbPackage pkg = new AdbPackage(pkgName, rawPath);
				boolean include = false;
				if ("all".equalsIgnoreCase(filterType)) {
					include = true;
				} else if ("system".equalsIgnoreCase(filterType)) {
					include = pkg.isSystem();
				} else if ("user".equalsIgnoreCase(filterType)) {
					include = !pkg.isSystem();
				}
				if (include) {
					packages.add(pkg);
				}
			}
		}
		return packages;
	}
}
