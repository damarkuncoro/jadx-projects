package jadx.frida;

import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.metadata.annotations.VarNode;
import jadx.core.codegen.TypeGen;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper class to generate Frida scripts from Jadx decompiled methods, classes, and fields
 */
public class FridaScriptGenerator {

	/**
	 * Generate full Frida script to hook a single Java method
	 */
	public static String generateMethodHook(JavaMethod method) {
		MethodNode methodNode = method.getMethodNode();
		MethodInfo methodInfo = methodNode.getMethodInfo();
		String className = methodInfo.getDeclClass().getFullName();
		String methodName = methodInfo.getName();

		StringBuilder script = new StringBuilder();
		script.append("Java.perform(function() {\n");
		script.append("  var TargetClass = Java.use(\"").append(className).append("\");\n");
		script.append("\n");

		// Check if method is overloaded
		boolean isOverloaded = isOverloaded(methodNode);
		String methodAccess;
		if (isOverloaded) {
			List<String> overloadArgs = methodInfo.getArgumentsTypes().stream()
					.map(FridaScriptGenerator::parseArgType)
					.collect(Collectors.toList());
			methodAccess = "TargetClass[\"" + methodName + "\"].overload(" + String.join(", ", overloadArgs) + ")";
		} else {
			methodAccess = "TargetClass[\"" + methodName + "\"]";
		}

		// Generate overload handling
		script.append("  ").append(methodAccess).append(".implementation = function(");

		// Add arguments
		List<String> argNames = new ArrayList<>();
		List<ArgType> argTypes = methodInfo.getArgumentsTypes();
		for (int i = 0; i < argTypes.size(); i++) {
			argNames.add("arg" + i);
		}
		script.append(String.join(", ", argNames));
		script.append(") {\n");

		// Log arguments
		script.append("    console.log(\"[*] ").append(className).append(".").append(methodName).append(" called!\");\n");
		for (int i = 0; i < argNames.size(); i++) {
			script.append("    console.log(\"  Arg").append(i).append(": \" + ").append(argNames.get(i)).append(");\n");
		}

		// Call original method and log return value
		script.append("    var result = this[\"").append(methodName).append("\"].apply(this, arguments);\n");
		script.append("    console.log(\"  Return: \" + result);\n");
		script.append("    return result;\n");
		script.append("  };\n");
		script.append("});\n");

		return script.toString();
	}

	/**
	 * Generate just the method hook snippet (without Java.perform wrapper)
	 */
	public static String generateMethodSnippet(JavaMethod method, JavaClass javaClass) {
		MethodNode mth = method.getMethodNode();
		MethodInfo methodInfo = mth.getMethodInfo();
		String methodName;
		String newMethodName;
		if (methodInfo.isConstructor()) {
			methodName = "$init";
			newMethodName = methodName;
		} else {
			methodName = StringEscapeUtils.escapeEcmaScript(methodInfo.getName());
			newMethodName = StringEscapeUtils.escapeEcmaScript(methodInfo.getAlias());
		}
		String overload;
		if (isOverloaded(mth)) {
			String overloadArgs = methodInfo.getArgumentsTypes().stream()
					.map(FridaScriptGenerator::parseArgType).collect(Collectors.joining(", "));
			overload = ".overload(" + overloadArgs + ")";
		} else {
			overload = "";
		}
		List<String> argNames = mth.collectArgNodes().stream()
				.map(VarNode::getName).collect(Collectors.toList());
		String args = String.join(", ", argNames);
		String logArgs;
		if (argNames.isEmpty()) {
			logArgs = "";
		} else {
			logArgs = ": " + argNames.stream().map(arg -> arg + "=${" + arg + "}").collect(Collectors.joining(", "));
		}
		String shortClassName = mth.getParentClass().getAlias();
		if (methodInfo.isConstructor() || methodInfo.getReturnType() == ArgType.VOID) {
			// no return value
			return shortClassName + "[\"" + methodName + "\"]" + overload + ".implementation = function (" + args + ") {\n"
					+ "    console.log(`" + shortClassName + "." + newMethodName + " is called" + logArgs + "`);\n"
					+ "    this[\"" + methodName + "\"](" + args + ");\n"
					+ "};";
		}
		return shortClassName + "[\"" + methodName + "\"]" + overload + ".implementation = function (" + args + ") {\n"
				+ "    console.log(`" + shortClassName + "." + newMethodName + " is called" + logArgs + "`);\n"
				+ "    let result = this[\"" + methodName + "\"](" + args + ");\n"
				+ "    console.log(`" + shortClassName + "." + newMethodName + " result=${result}`);\n"
				+ "    return result;\n"
				+ "};";
	}

	/**
	 * Generate a class snippet (Java.use statement)
	 */
	public static String generateClassSnippet(JavaClass javaClass) {
		String rawClassName = StringEscapeUtils.escapeEcmaScript(javaClass.getRawName());
		String shortClassName = javaClass.getName();
		return String.format("var %s = Java.use(\"%s\");", shortClassName, rawClassName);
	}

	/**
	 * Generate a field access snippet
	 */
	public static String generateFieldSnippet(JavaField javaField, JavaClass javaClass) {
		FieldNode fieldNode = javaField.getFieldNode();
		String rawFieldName = StringEscapeUtils.escapeEcmaScript(javaField.getRawName());
		String fieldName = javaField.getName();

		List<MethodNode> methodNodes = fieldNode.getParentClass().getMethods();
		for (MethodNode methodNode : methodNodes) {
			if (methodNode.getName().equals(rawFieldName)) {
				rawFieldName = "_" + rawFieldName;
				break;
			}
		}
		String shortClassName = fieldNode.getParentClass().getAlias();
		return String.format("%s\n%s = %s.%s.value;", generateClassSnippet(javaClass), fieldName, shortClassName, rawFieldName);
	}

	/**
	 * Generate snippets for all methods in a class
	 */
	public static String generateClassAllMethodSnippet(JavaClass javaClass, List<JavaMethod> methodList) {
		StringBuilder result = new StringBuilder();
		String classSnippet = generateClassSnippet(javaClass);
		result.append(classSnippet).append("\n");
		for (JavaMethod javaMethod : methodList) {
			result.append(generateMethodSnippet(javaMethod, javaClass)).append("\n");
		}
		return result.toString();
	}

	/**
	 * Check if a method is overloaded in its class
	 */
	public static boolean isOverloaded(MethodNode methodNode) {
		return methodNode.getParentClass().getMethods().stream()
				.anyMatch(m -> m.getName().equals(methodNode.getName())
						&& !Objects.equals(methodNode.getMethodInfo().getShortId(), m.getMethodInfo().getShortId()));
	}

	/**
	 * Parse an argument type to a Frida-compatible string
	 */
	public static String parseArgType(ArgType x) {
		String typeStr;
		if (x.isArray()) {
			typeStr = TypeGen.signature(x).replace("/", ".");
		} else {
			typeStr = x.toString();
		}
		return "'" + typeStr + "'";
	}
}
