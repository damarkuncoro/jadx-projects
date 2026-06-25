package dexforge.core.parser.smali.parser;

import java.util.List;
import dexforge.core.parser.smali.model.SmaliClass;
import dexforge.core.parser.smali.lexer.SmaliToken;
import dexforge.core.parser.smali.lexer.SmaliTokenType;
import dexforge.core.parser.smali.lexer.SmaliTokenizer;

/**
 * Parser that understands the arrangement of Smali tokens and uses ASTBuilder to create the model.
 */
public final class SmaliParser {
	private List<SmaliToken> tokens;
	private int pos = 0;
	private SmaliASTBuilder astBuilder;

	public SmaliClass parse(String smaliText) {
		SmaliTokenizer tokenizer = new SmaliTokenizer(smaliText);
		this.tokens = tokenizer.tokenize();
		this.pos = 0;
		this.astBuilder = new SmaliASTBuilder();

		while (!isAtEnd()) {
			SmaliToken token = peek();
			if (token.getType() == SmaliTokenType.DIRECTIVE) {
				parseDirective();
			} else if (token.getType() == SmaliTokenType.INSTRUCTION) {
				astBuilder.addInstruction(consumeInstructionLine());
			} else {
				advance();
			}
		}
		return astBuilder.getResult();
	}

	private void parseDirective() {
		SmaliToken token = advance();
		String directive = token.getText();

		switch (directive) {
			case ".class":
				parseClassDirective();
				break;
			case ".super":
				astBuilder.setSuperName(consume(SmaliTokenType.IDENTIFIER, "Expected super class name").getText());
				break;
			case ".source":
				String source = consume(SmaliTokenType.STRING_LITERAL, "Expected source file string").getText();
				if (source.startsWith("\"") && source.endsWith("\"")) {
					source = source.substring(1, source.length() - 1);
				}
				astBuilder.setSourceFile(source);
				break;
			case ".implements":
				astBuilder.addInterface(consume(SmaliTokenType.IDENTIFIER, "Expected interface name").getText());
				break;
			case ".method":
				parseMethodDirective();
				break;
			case ".end":
				if (peek().getText().equals("method")) {
					advance(); // method
					astBuilder.endMethod();
				}
				break;
			case ".registers":
				astBuilder.setMethodRegisters(Integer.parseInt(consume(SmaliTokenType.NUMBER_LITERAL, "Expected number of registers").getText()));
				break;
			default:
				// Skip unknown directives
				while (!isAtEnd() && peek().getType() != SmaliTokenType.NEWLINE) {
					advance();
				}
				break;
		}
	}

	private void parseClassDirective() {
		while (peek().getType() == SmaliTokenType.ACCESS_FLAG) {
			advance(); // Skip for now
		}
		astBuilder.setClassName(consume(SmaliTokenType.IDENTIFIER, "Expected class name").getText());
	}

	private void parseMethodDirective() {
		while (peek().getType() == SmaliTokenType.ACCESS_FLAG) {
			advance(); // Skip for now
		}
		String name = consume(SmaliTokenType.IDENTIFIER, "Expected method name").getText();

		StringBuilder sig = new StringBuilder(name);
		while (!isAtEnd() && peek().getType() != SmaliTokenType.NEWLINE) {
			sig.append(advance().getText());
		}
		astBuilder.startMethod(name, sig.toString(), 0);
	}

	private String consumeInstructionLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(tokens.get(pos - 1).getText()).append(" "); // Add the already consumed instruction token
		while (!isAtEnd() && peek().getType() != SmaliTokenType.NEWLINE) {
			sb.append(advance().getText()).append(" ");
		}
		return sb.toString().trim();
	}

	private SmaliToken peek() {
		return tokens.get(pos);
	}

	private SmaliToken advance() {
		if (!isAtEnd()) {
			pos++;
		}
		return tokens.get(pos - 1);
	}

	private boolean isAtEnd() {
		return pos >= tokens.size() || peek().getType() == SmaliTokenType.EOF;
	}

	private SmaliToken consume(SmaliTokenType type, String message) {
		if (peek().getType() == type) {
			return advance();
		}
		return advance();
	}
}
