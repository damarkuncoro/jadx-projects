package jadx.gui.frida;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

public class FridaApiCompletionProvider {

	private FridaApiCompletionProvider() {
	}

	public static CompletionProvider create() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();

		// Core Frida API
		provider.addCompletion(new BasicCompletion(provider, "Java.perform"));
		provider.addCompletion(new BasicCompletion(provider, "Java.choose"));
		provider.addCompletion(new BasicCompletion(provider, "Java.enumerateClassLoaders"));
		provider.addCompletion(new BasicCompletion(provider, "Java.enumerateLoadedClasses"));
		provider.addCompletion(new BasicCompletion(provider, "Java.enumerateMethods"));
		provider.addCompletion(new BasicCompletion(provider, "Java.registerClass"));
		provider.addCompletion(new BasicCompletion(provider, "Java.vm"));
		provider.addCompletion(new BasicCompletion(provider, "Java.array"));
		provider.addCompletion(new BasicCompletion(provider, "Java.use"));
		provider.addCompletion(new BasicCompletion(provider, "Java.cast"));
		provider.addCompletion(new BasicCompletion(provider, "Java.isMainThread"));
		provider.addCompletion(new BasicCompletion(provider, "Java.scheduleOnMainThread"));

		// Java class API
		provider.addCompletion(new BasicCompletion(provider, ".classFactory"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getDeclaredField"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getDeclaredMethod"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getField"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getMethod"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getDeclaredFields"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getDeclaredMethods"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getFields"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getMethods"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getName"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getSuperclass"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getInterfaces"));
		provider.addCompletion(new BasicCompletion(provider, ".class.getClassLoader"));
		provider.addCompletion(new BasicCompletion(provider, ".class.newInstance"));
		provider.addCompletion(new BasicCompletion(provider, ".class.isInstance"));
		provider.addCompletion(new BasicCompletion(provider, ".class.isAssignableFrom"));

		// Method and field API
		provider.addCompletion(new BasicCompletion(provider, ".implementation"));
		provider.addCompletion(new BasicCompletion(provider, ".overload"));
		provider.addCompletion(new BasicCompletion(provider, ".value"));
		provider.addCompletion(new BasicCompletion(provider, ".call"));
		provider.addCompletion(new BasicCompletion(provider, ".apply"));

		// Memory API
		provider.addCompletion(new BasicCompletion(provider, "Memory.scan"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readByteArray"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeByteArray"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readU8"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readS8"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readU16"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readS16"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readU32"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readS32"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readU64"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readS64"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readFloat"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.readDouble"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeU8"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeS8"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeU16"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeS16"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeU32"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeS32"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeU64"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeS64"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeFloat"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.writeDouble"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.copy"));
		provider.addCompletion(new BasicCompletion(provider, "Memory.protect"));

		// Interceptor API
		provider.addCompletion(new BasicCompletion(provider, "Interceptor.attach"));
		provider.addCompletion(new BasicCompletion(provider, "Interceptor.replace"));
		provider.addCompletion(new BasicCompletion(provider, "Interceptor.flush"));
		provider.addCompletion(new BasicCompletion(provider, "Interceptor.alloc"));
		provider.addCompletion(new BasicCompletion(provider, "Interceptor.free"));

		// Native function API
		provider.addCompletion(new BasicCompletion(provider, "NativeFunction"));
		provider.addCompletion(new BasicCompletion(provider, "NativeCallback"));
		provider.addCompletion(new BasicCompletion(provider, "NativePointer"));
		provider.addCompletion(new BasicCompletion(provider, "Process.enumerateModules"));
		provider.addCompletion(new BasicCompletion(provider, "Process.enumerateThreads"));
		provider.addCompletion(new BasicCompletion(provider, "Process.enumerateMallocRanges"));
		provider.addCompletion(new BasicCompletion(provider, "Process.enumerateRanges"));
		provider.addCompletion(new BasicCompletion(provider, "Process.findModuleByName"));
		provider.addCompletion(new BasicCompletion(provider, "Process.findModuleByAddress"));
		provider.addCompletion(new BasicCompletion(provider, "Process.getCurrentThreadId"));
		provider.addCompletion(new BasicCompletion(provider, "Process.getCurrentThread"));
		provider.addCompletion(new BasicCompletion(provider, "Process.getCurrentId"));

		// Module API
		provider.addCompletion(new BasicCompletion(provider, ".name"));
		provider.addCompletion(new BasicCompletion(provider, ".base"));
		provider.addCompletion(new BasicCompletion(provider, ".size"));
		provider.addCompletion(new BasicCompletion(provider, ".path"));
		provider.addCompletion(new BasicCompletion(provider, ".enumerateExports"));
		provider.addCompletion(new BasicCompletion(provider, ".enumerateImports"));
		provider.addCompletion(new BasicCompletion(provider, ".enumerateSymbols"));
		provider.addCompletion(new BasicCompletion(provider, ".enumerateSections"));

		// Console and debug
		provider.addCompletion(new BasicCompletion(provider, "console.log"));
		provider.addCompletion(new BasicCompletion(provider, "console.warn"));
		provider.addCompletion(new BasicCompletion(provider, "console.error"));
		provider.addCompletion(new BasicCompletion(provider, "console.debug"));
		provider.addCompletion(new BasicCompletion(provider, "console.clear"));
		provider.addCompletion(new BasicCompletion(provider, "hexdump"));

		// Utility
		provider.addCompletion(new BasicCompletion(provider, "ptr"));
		provider.addCompletion(new BasicCompletion(provider, "uint64"));
		provider.addCompletion(new BasicCompletion(provider, "int64"));
		provider.addCompletion(new BasicCompletion(provider, "NULL"));

		return provider;
	}
}
