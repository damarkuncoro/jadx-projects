package dexforge.api.plugins;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import dexforge.api.plugins.data.IJadxFiles;
import dexforge.api.plugins.data.IJadxPlugins;
import dexforge.api.plugins.events.IJadxEvents;
import dexforge.api.plugins.gui.JadxGuiContext;
import dexforge.api.plugins.input.JadxCodeInput;
import dexforge.api.plugins.options.JadxPluginOptions;
import dexforge.api.plugins.pass.JadxPass;
import dexforge.api.plugins.resources.IResourcesLoader;
import dexforge.zip.ZipReader;

public interface JadxPluginContext {

	JadxArgs getArgs();

	JadxDecompiler getDecompiler();

	void addPass(JadxPass pass);

	void addCodeInput(JadxCodeInput codeInput);

	void registerOptions(JadxPluginOptions options);

	/**
	 * Function to calculate hash of all options which can change output code.
	 * Hash for input files ({@link JadxArgs#getInputFiles()}) and registered options
	 * calculated by default implementations.
	 */
	void registerInputsHashSupplier(Supplier<String> supplier);

	/**
	 * Customize resource loading
	 */
	IResourcesLoader getResourcesLoader();

	/**
	 * Access to jadx-gui specific methods
	 */
	@Nullable
	JadxGuiContext getGuiContext();

	/**
	 * Subscribe and send events
	 */
	IJadxEvents events();

	/**
	 * Access to registered plugins and runtime data
	 */
	IJadxPlugins plugins();

	/**
	 * Access to plugin specific files and directories
	 */
	IJadxFiles files();

	/**
	 * Custom jadx zip reader to fight tampering and provide additional security checks
	 */
	ZipReader getZipReader();
}
