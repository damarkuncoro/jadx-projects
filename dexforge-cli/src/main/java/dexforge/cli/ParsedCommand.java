package dexforge.cli;

import org.jetbrains.annotations.Nullable;

public class ParsedCommand {
	private final CliCommandType type;
	private final String[] args;
	private final @Nullable DexforgeCLIArgs cliArgs;

	public ParsedCommand(CliCommandType type, String[] args, @Nullable DexforgeCLIArgs cliArgs) {
		this.type = type;
		this.args = args;
		this.cliArgs = cliArgs;
	}

	public CliCommandType getType() {
		return type;
	}

	public String[] getArgs() {
		return args;
	}

	public @Nullable DexforgeCLIArgs getCliArgs() {
		return cliArgs;
	}
}
