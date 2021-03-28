package py.una.pol.analizadorlexico.utils;

import java.util.List;
import java.util.Map;

import py.una.pol.analizadorlexico.tokenizer.Token;
import py.una.pol.analizadorlexico.tokenizer.TokenList;
import py.una.pol.analizadorlexico.tokenizer.TokenType;

public class JSONBeautify {

	private static final char ESPACIO = ' ';

	private static final int INDENTAR = 2;

	private static int callDepth = 0;

	public String beautify(TokenList tokens) {
		StringBuilder sb = new StringBuilder();

		while (tokens.hasMore()) {
			Token token = tokens.next();
			TokenType tokenType = token.getTokenType();

			if (tokenType.equals(TokenType.BEGIN_OBJECT)) {
				sb.append(token.getValue());
				sb.append("\n");
				callDepth++;
				sb.append(getIndentString());
			} else if (tokenType.equals(TokenType.BEGIN_ARRAY)) {
				sb.append(token.getValue());
				sb.append("\n");
				callDepth++;
				sb.append(getIndentString());
			} else if (tokenType.equals(TokenType.END_OBJECT)) {
				sb.append("\n");
				callDepth--;
				sb.append(getIndentString());
				sb.append(token.getValue());
				sb.append(ESPACIO);
			} else if (tokenType.equals(TokenType.END_ARRAY)) {
				sb.append("\n");
				callDepth--;
				sb.append(getIndentString());
				sb.append(token.getValue());
			} else if (tokenType.equals(TokenType.SEP_COMMA)) {
				sb.append(token.getValue());
				sb.append("\n");
				sb.append(getIndentString());
			} else {
				if (!tokenType.equals(TokenType.END_DOCUMENT)) {
					sb.append(token.getValue());
					sb.append(ESPACIO);
				}
			}
		}
		return sb.toString();

	}

	private static String getIndentString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < callDepth * INDENTAR; i++) {
			sb.append(ESPACIO);
		}

		return sb.toString();
	}
}
