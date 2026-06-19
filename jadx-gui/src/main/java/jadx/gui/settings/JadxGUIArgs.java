package jadx.gui.settings;

import com.beust.jcommander.Parameter;

import dexforge.cli.DexforgeCLIArgs;
import dexforge.cli.config.DexforgeConfigExclude;

public class JadxGUIArgs extends DexforgeCLIArgs {

	@DexforgeConfigExclude
	@Parameter(
			names = { "-sc", "--select-class" },
			description = "GUI: Open the selected class and show the decompiled code"
	)
	private String cmdSelectClass = null;

	public String getCmdSelectClass() {
		return cmdSelectClass;
	}

	public void setCmdSelectClass(String cmdSelectClass) {
		this.cmdSelectClass = cmdSelectClass;
	}
}
