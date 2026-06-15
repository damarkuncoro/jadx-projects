package jadx.frida;

import jadx.api.JavaMethod;
import jadx.core.codegen.TypeGen;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper class to generate Frida scripts from Jadx decompiled methods
 */
public class FridaScriptGenerator {

	/**
	 * Generate Frida script to hook a single Java method
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

	private static boolean isOverloaded(MethodNode methodNode) {
		return methodNode.getParentClass().getMethods().stream()
				.anyMatch(m -> m.getName().equals(methodNode.getName())
						&& !Objects.equals(methodNode.getMethodInfo().getShortId(), m.getMethodInfo().getShortId()));
	}

	private static String parseArgType(ArgType x) {
		String typeStr;
		if (x.isArray()) {
			typeStr = TypeGen.signature(x).replace("/", ".");
		} else {
			typeStr = x.toString();
		}
		return "'" + typeStr + "'";
	}
}
