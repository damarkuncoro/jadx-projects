package dexforge.core.parser.dex.decompiler.model.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MethodCallExpression implements JavaExpression {
	private final String object;
	private final String methodName;
	private final List<JavaExpression> arguments = new ArrayList<>();

	public MethodCallExpression(String object, String methodName) {
		this.object = object;
		this.methodName = methodName;
	}

	public List<JavaExpression> getArguments() {
		return arguments;
	}

	@Override
	public String toCode() {
		String args = arguments.stream().map(JavaExpression::toCode).collect(Collectors.joining(", "));
		if (object == null || object.isEmpty()) {
			return methodName + "(" + args + ")";
		}
		return object + "." + methodName + "(" + args + ")";
	}
}
