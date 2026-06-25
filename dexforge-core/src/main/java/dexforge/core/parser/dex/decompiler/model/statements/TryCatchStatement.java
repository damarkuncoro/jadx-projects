package dexforge.core.parser.dex.decompiler.model.statements;

import java.util.ArrayList;
import java.util.List;

public final class TryCatchStatement implements JavaStatement {
	private final List<JavaStatement> tryBody = new ArrayList<>();
	private final List<CatchBlock> catchBlocks = new ArrayList<>();

	public List<JavaStatement> getTryBody() {
		return tryBody;
	}

	public List<CatchBlock> getCatchBlocks() {
		return catchBlocks;
	}

	public static final class CatchBlock {
		private final String exceptionType;
		private final String variableName;
		private final List<JavaStatement> body = new ArrayList<>();

		public CatchBlock(String exceptionType, String variableName) {
			this.exceptionType = exceptionType;
			this.variableName = variableName;
		}

		public String getExceptionType() {
			return exceptionType;
		}

		public String getVariableName() {
			return variableName;
		}

		public List<JavaStatement> getBody() {
			return body;
		}
	}

	@Override
	public String toCode(int indent) {
		String space = "\t".repeat(indent);
		StringBuilder sb = new StringBuilder();
		sb.append(space).append("try {\n");
		for (JavaStatement stmt : tryBody) {
			sb.append(stmt.toCode(indent + 1)).append("\n");
		}

		for (CatchBlock cb : catchBlocks) {
			sb.append(space).append("} catch (").append(cb.getExceptionType())
					.append(" ").append(cb.getVariableName()).append(") {\n");
			for (JavaStatement stmt : cb.getBody()) {
				sb.append(stmt.toCode(indent + 1)).append("\n");
			}
		}

		sb.append(space).append("}");
		return sb.toString();
	}
}
