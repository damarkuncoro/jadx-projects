package dexforge.api.plugins.loader;

import java.io.Closeable;
import java.util.List;

import dexforge.api.plugins.JadxPlugin;

public interface JadxPluginLoader extends Closeable {

	List<JadxPlugin> load();
}
