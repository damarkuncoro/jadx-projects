package dexforge.core.parser.dex.decompiler.model.expressions;

public final class ArrayAccessExpression implements JavaExpression {
	private final JavaExpression array;
	private final JavaExpression index;

	public ArrayAccessExpression(JavaExpression array, JavaExpression index) {
		this.array = array;
		this.index = index;
	}

	@Override
	public String toCode() {
		return array.toCode() + "[" + index.toCode() + "]";
	}
}
