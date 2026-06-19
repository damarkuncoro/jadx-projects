package dexforge.cli;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import jadx.api.JadxArgs;

public class DexforgeCLI {

	public static void main(String[] args) {
		int result = 1;
		try {
			result = execute(args);
		} finally {
			System.exit(result);
		}
	}

	public static int execute(String[] args) {
		return execute(args, null);
	}

	public static int execute(String[] args, @Nullable Consumer<JadxArgs> argsMod) {
		CliCommandParser parser = new CliCommandParser();
		CliApplication app = new CliApplication(parser);
		return app.run(args, argsMod);
	}
}
