package jadx.gui.device.adb.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jadx.gui.device.adb.AdbService.AdbUser;

public final class AdbUserParser {
	private static final Pattern USER_PATTERN = Pattern.compile("UserInfo\\{(\\d+):([^:]+):\\d+\\}");

	private AdbUserParser() {
	}

	public static List<AdbUser> parse(String output) {
		List<AdbUser> users = new ArrayList<>();
		if (output == null) {
			users.add(new AdbUser(0, "Owner"));
			return users;
		}
		for (String line : output.split("\n")) {
			line = line.trim();
			Matcher m = USER_PATTERN.matcher(line);
			if (m.find()) {
				int id = Integer.parseInt(m.group(1));
				String name = m.group(2);
				users.add(new AdbUser(id, name));
			}
		}
		if (users.isEmpty()) {
			users.add(new AdbUser(0, "Owner"));
		}
		return users;
	}
}
