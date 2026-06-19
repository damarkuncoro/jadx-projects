package dexforge.cli.commands;

import com.beust.jcommander.JCommander;

import dexforge.cli.JCommanderWrapper;

public interface ICommand {
	String name();

	void process(JCommanderWrapper jcw, JCommander subCommander);
}
