package dexforge.core.parser.dex.decompiler.model.expressions;

public final class FieldAccessExpression implements JavaExpression {
	private final String object;
	private final String fieldName;

	public FieldAccessExpression(String object, String fieldName) {
		this.object = object;
		this.fieldName = fieldName;
	}

	@Override
	public String toCode() {
		if (object == null || object.isEmpty()) {
			return fieldName;
		}
		return object + "." + fieldName;
	}
}
