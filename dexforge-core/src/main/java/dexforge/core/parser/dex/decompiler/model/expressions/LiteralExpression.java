package dexforge.core.parser.dex.decompiler.model.expressions;

public final class LiteralExpression implements JavaExpression {
	private final String value;

	public LiteralExpression(Object value) {
		if (value instanceof String) {
			this.value = "\"" + ((String) value).replace("\"", "\\\"") + "\"";
		} else {
			this.value = String.valueOf(value);
		}
	}

	@Override
	public String toCode() {
		return value;
	}
}
