package jadx.gui.device.adb.parsers;

import java.util.ArrayList;
import java.util.List;

import jadx.gui.device.adb.ApkPath;

public final class ApkPathParser {
	private ApkPathParser() {
	}

	public static List<ApkPath> parse(String output) {
		List<ApkPath> paths = new ArrayList<>();
		if (output == null) {
			return paths;
		}
		for (String line : output.split("\n")) {
			line = line.trim();
			if (line.startsWith("package:")) {
				paths.add(new ApkPath(line.substring("package:".length())));
			}
		}
		return paths;
	}
}
