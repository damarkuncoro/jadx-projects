package dexforge.core.parser.smali.lexer;

import java.util.ArrayList;
import java.util.List;

public final class SmaliTokenizer {
	private final String input;
	private int pos = 0;
	private int line = 1;
	private int column = 1;

	public SmaliTokenizer(String input) {
		this.input = input;
	}

	public List<SmaliToken> tokenize() {
		List<SmaliToken> tokens = new ArrayList<>();
		while (pos < input.length()) {
			char c = peek();
			if (Character.isWhitespace(c)) {
				if (c == '\n') {
					tokens.add(new SmaliToken(SmaliTokenType.NEWLINE, "\n", line, column));
					advance();
					line++;
					column = 1;
				} else {
					advance();
				}
			} else if (c == '#') {
				tokens.add(consumeComment());
			} else if (c == '.') {
				tokens.add(consumeDirective());
			} else if (c == ':') {
				tokens.add(consumeLabel());
			} else if (c == '"') {
				tokens.add(consumeString());
			} else if (isSymbol(c)) {
				tokens.add(consumeSymbol());
			} else if (Character.isDigit(c) || (c == '-' && Character.isDigit(peek(1)))) {
				tokens.add(consumeNumber());
			} else {
				tokens.add(consumeIdentifierOrOther());
			}
		}
		tokens.add(new SmaliToken(SmaliTokenType.EOF, "", line, column));
		return tokens;
	}

	private char peek() {
		return pos < input.length() ? input.charAt(pos) : '\0';
	}

	private char peek(int offset) {
		return pos + offset < input.length() ? input.charAt(pos + offset) : '\0';
	}

	private char advance() {
		char c = peek();
		pos++;
		column++;
		return c;
	}

	private SmaliToken consumeComment() {
		int startCol = column;
		StringBuilder sb = new StringBuilder();
		while (pos < input.length() && peek() != '\n') {
			sb.append(advance());
		}
		return new SmaliToken(SmaliTokenType.COMMENT, sb.toString(), line, startCol);
	}

	private SmaliToken consumeDirective() {
		int startCol = column;
		StringBuilder sb = new StringBuilder();
		sb.append(advance()); // .
		while (pos < input.length() && isIdentifierPart(peek())) {
			sb.append(advance());
		}
		return new SmaliToken(SmaliTokenType.DIRECTIVE, sb.toString(), line, startCol);
	}

	private SmaliToken consumeLabel() {
		int startCol = column;
		StringBuilder sb = new StringBuilder();
		sb.append(advance()); // :
		while (pos < input.length() && isIdentifierPart(peek())) {
			sb.append(advance());
		}
		return new SmaliToken(SmaliTokenType.LABEL, sb.toString(), line, startCol);
	}

	private SmaliToken consumeString() {
		int startCol = column;
		StringBuilder sb = new StringBuilder();
		sb.append(advance()); // "
		while (pos < input.length() && peek() != '"') {
			if (peek() == '\\') {
				sb.append(advance());
			}
			sb.append(advance());
		}
		if (peek() == '"') {
			sb.append(advance());
		}
		return new SmaliToken(SmaliTokenType.STRING_LITERAL, sb.toString(), line, startCol);
	}

	private SmaliToken consumeSymbol() {
		int startCol = column;
		StringBuilder sb = new StringBuilder();
		char c = advance();
		sb.append(c);
		if (c == '-' && peek() == '>') {
			sb.append(advance());
		}
		return new SmaliToken(SmaliTokenType.SYMBOL, sb.toString(), line, startCol);
	}

	private SmaliToken consumeNumber() {
		int startCol = column;
		StringBuilder sb = new StringBuilder();
		while (pos < input.length() && (Character.isDigit(peek()) || "xXabcdefABCDEF.fL-".indexOf(peek()) != -1)) {
			sb.append(advance());
		}
		return new SmaliToken(SmaliTokenType.NUMBER_LITERAL, sb.toString(), line, startCol);
	}

	private SmaliToken consumeIdentifierOrOther() {
		int startCol = column;
		StringBuilder sb = new StringBuilder();
		while (pos < input.length() && !Character.isWhitespace(peek()) && !isSymbol(peek()) && peek() != '#' && peek() != '"') {
			sb.append(advance());
		}
		String text = sb.toString();
		SmaliTokenType type = SmaliTokenType.IDENTIFIER;

		if (text.matches("[vp]\\d+")) {
			type = SmaliTokenType.REGISTER;
		} else if (isAccessFlag(text)) {
			type = SmaliTokenType.ACCESS_FLAG;
		} else if (isInstruction(text)) {
			type = SmaliTokenType.INSTRUCTION;
		}

		return new SmaliToken(type, text, line, startCol);
	}

	private boolean isIdentifierPart(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '$';
	}

	private boolean isSymbol(char c) {
		return "(){}=,:->".indexOf(c) != -1;
	}

	private boolean isAccessFlag(String text) {
		return text.equals("public") || text.equals("private") || text.equals("protected") ||
				text.equals("static") || text.equals("final") || text.equals("synchronized") ||
				text.equals("native") || text.equals("abstract") || text.equals("strictfp") ||
				text.equals("synthetic") || text.equals("constructor") || text.equals("declared-synchronized");
	}

	private boolean isInstruction(String text) {
		// Very simplified check, real instructions often have dashes
		return text.contains("-") || text.equals("nop") || text.equals("return-void") || text.startsWith("move");
	}
}
