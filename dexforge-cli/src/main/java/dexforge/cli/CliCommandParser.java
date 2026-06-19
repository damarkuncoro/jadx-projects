package dexforge.cli;

import dexforge.cli.config.DexforgeConfigAdapter;

public class CliCommandParser {

	public ParsedCommand parse(String[] args) {
		if (isDeviceExplorerCommand(args)) {
			return new ParsedCommand(CliCommandType.DEVICE_EXPLORER, args, null);
		}
		if (isDecompilerDaemonCommand(args)) {
			return new ParsedCommand(CliCommandType.DECOMPILER_DAEMON, args, null);
		}
		DexforgeCLIArgs cliArgs = DexforgeCLIArgs.processArgs(args,
				new DexforgeCLIArgs(),
				new DexforgeConfigAdapter<>(DexforgeCLIArgs.class, "cli"));
		if (cliArgs == null) {
			return new ParsedCommand(CliCommandType.EXIT_SUCCESS, args, null);
		}
		return new ParsedCommand(CliCommandType.DECOMPILE, args, cliArgs);
	}

	private boolean isDeviceExplorerCommand(String[] args) {
		return args.length > 0 && "device-explorer".equals(args[0]);
	}

	private boolean isDecompilerDaemonCommand(String[] args) {
		return args.length > 0 && ("decompiler-daemon".equals(args[0]) || "lsp".equals(args[0]));
	}
}
