package dexforge.api.plugins.input;

import java.io.Closeable;
import java.util.function.Consumer;

import dexforge.api.plugins.input.data.IClassData;

public interface ICodeLoader extends Closeable {

	void visitClasses(Consumer<IClassData> consumer);

	boolean isEmpty();
}
