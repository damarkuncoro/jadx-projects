package jadx.gui.device.adb.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AndroidPropsParser {
	private AndroidPropsParser() {
	}

	public static List<String> parse(String output) {
		if (output == null || output.trim().isEmpty()) {
			return Collections.emptyList();
		}
		List<String> props = new ArrayList<>();
		String[] lines = output.split("\n");
		for (String line : lines) {
			line = line.trim();
			if (!line.isEmpty()) {
				props.add(line);
			}
		}
		return props;
	}
}
