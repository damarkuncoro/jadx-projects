package jadx.frida;

import java.util.List;

import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;

public interface IFridaScriptGenerator {
	String generateMethodHook(JavaMethod method);

	String generateMethodSnippet(JavaMethod method, JavaClass javaClass);

	String generateClassSnippet(JavaClass javaClass);

	String generateFieldSnippet(JavaField javaField, JavaClass javaClass);

	String generateClassAllMethodSnippet(JavaClass javaClass, List<JavaMethod> methodList);
}
