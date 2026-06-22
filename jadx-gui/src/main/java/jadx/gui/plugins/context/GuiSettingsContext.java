package jadx.gui.plugins.context;

import java.util.List;

import dexforge.api.plugins.gui.ISettingsGroup;
import dexforge.api.plugins.gui.JadxGuiSettings;
import dexforge.api.plugins.options.OptionDescription;

import jadx.gui.settings.ui.SubSettingsGroup;
import jadx.gui.settings.ui.plugins.PluginSettings;
import jadx.gui.ui.MainWindow;

public class GuiSettingsContext implements JadxGuiSettings {
	private final GuiPluginContext guiPluginContext;

	public GuiSettingsContext(GuiPluginContext guiPluginContext) {
		this.guiPluginContext = guiPluginContext;
	}

	@Override
	public void setCustomSettingsGroup(ISettingsGroup group) {
		guiPluginContext.setCustomSettings(group);
	}

	@Override
	public ISettingsGroup buildSettingsGroupForOptions(String title, List<OptionDescription> options) {
		MainWindow mainWindow = guiPluginContext.getCommonContext().getMainWindow();
		PluginSettings pluginsSettings = new PluginSettings(mainWindow, mainWindow.getSettings());
		SubSettingsGroup settingsGroup = new SubSettingsGroup(title);
		pluginsSettings.addOptions(settingsGroup, options);
		return settingsGroup;
	}
}
