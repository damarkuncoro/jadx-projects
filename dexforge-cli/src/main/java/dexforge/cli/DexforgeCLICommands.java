package dexforge.cli;

import java.util.LinkedHashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;

import dexforge.cli.commands.CommandDeobfuscate;
import dexforge.cli.commands.CommandPlugins;
import dexforge.cli.commands.CommandScan;
import dexforge.cli.commands.ICommand;

import jadx.core.utils.exceptions.JadxArgsValidateException;

public class DexforgeCLICommands {
	private static final Map<String, ICommand> COMMANDS_MAP = new LinkedHashMap<>();

	static {
		DexforgeCLICommands.register(new CommandPlugins());
		DexforgeCLICommands.register(new CommandDeobfuscate());
		DexforgeCLICommands.register(new CommandScan());
	}

	public static void register(ICommand command) {
		COMMANDS_MAP.put(command.name(), command);
	}

	public static void append(JCommander.Builder builder) {
		COMMANDS_MAP.forEach(builder::addCommand);
	}

	public static boolean process(JCommanderWrapper jcw, JCommander jc, String parsedCommand) {
		ICommand command = COMMANDS_MAP.get(parsedCommand);
		if (command == null) {
			throw new JadxArgsValidateException("Unknown command: " + parsedCommand
					+ ". Expected one of: " + COMMANDS_MAP.keySet());
		}
		JCommander subCommander = jc.getCommands().get(parsedCommand);
		command.process(jcw, subCommander);
		return true;
	}
}
