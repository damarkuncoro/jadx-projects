package dexforge.api.plugin;

import java.io.Closeable;
import java.util.List;

public interface DexForgePluginLoader extends Closeable {
	List<DexForgePlugin> load();
}
