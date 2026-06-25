package dexforge.core.parser.dex.decompiler.model.expressions;

public final class BinaryExpression implements JavaExpression {
	private final JavaExpression left;
	private final String operator;
	private final JavaExpression right;

	public BinaryExpression(JavaExpression left, String operator, JavaExpression right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	@Override
	public String toCode() {
		return "(" + left.toCode() + " " + operator + " " + right.toCode() + ")";
	}
}
