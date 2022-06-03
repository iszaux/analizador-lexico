package py.una.pol.analizadorlexicosintactico.tokenizer;

import java.io.IOException;

import py.una.pol.analizadorlexicosintactico.exception.JSONParseException;

public class Tokenizer {

	private static final String L_CORCHETE = "L_CORCHETE";
	private static final String R_CORCHETE = "R_CORCHETE";
	private static final String L_LLAVE = "L_LLAVE";
	private static final String R_LLAVE = "R_LLAVE";
	private static final String COMA = "COMA";
	private static final String DOS_PUNTOS = "DOS_PUNTOS";
	private static final String LITERAL_CADENA = "STRING";
	private static final String LITERAL_NUM = "NUMBER";
	private static final String PR_TRUE = "PR_TRUE";
	private static final String PR_FALSE = "PR_FALSE";
	private static final String PR_NULL = "PR_NULL";
	private static final String EOF = "EOF";

	private CharReader charReader;

	private TokenList tokens;

	public TokenList tokenize(CharReader charReader) throws IOException {
		this.charReader = charReader;
		tokens = new TokenList();
		tokenize();

		return tokens;
	}

	private void tokenize() throws IOException {
		Token token;
		do {
			token = start();
			tokens.add(token);
		} while (token.getTokenType() != TokenType.END_DOCUMENT);
	}

	private Token start() throws IOException {
		char ch;
		for (;;) {
			if (!charReader.hasMore()) {
				return new Token(TokenType.END_DOCUMENT, EOF);
			}

			ch = charReader.next();
			if (!isWhiteSpace(ch)) {
				break;
			}
		}
		if (isDigit(ch)) {
			return readNumber();
		}
		switch (ch) {
			case '{':
				return new Token(TokenType.BEGIN_OBJECT, L_LLAVE);
			case '}':
				return new Token(TokenType.END_OBJECT, R_LLAVE);
			case '[':
				return new Token(TokenType.BEGIN_ARRAY, L_CORCHETE);
			case ']':
				return new Token(TokenType.END_ARRAY, R_CORCHETE);
			case ',':
				return new Token(TokenType.SEP_COMMA, COMA);
			case ':':
				return new Token(TokenType.SEP_COLON, DOS_PUNTOS);
			case 'n':
				return readNull();
			case 't':
			case 'f':
				return readBoolean();
			case '"':
				return readString();
			case '-':
				return readNumber();
			default:
				return new Token(TokenType.ERROR, "Error léxico, caracter inválido: '" + ch + "'");
		}

	}

	private boolean isWhiteSpace(char ch) {
		return (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n');
	}

	private Token readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            char ch = charReader.next();
            if (ch == '\\') {
                if (!isEscape()) {
                    throw new JSONParseException("Carácter de escape inválido");
                }
                sb.append('\\');
                ch = charReader.peek();
                sb.append(ch);
                if (ch == 'u') {
                    for (int i = 0; i < 4; i++) {
                        ch = charReader.next();
                        if (isHex(ch)) {
                            sb.append(ch);
                        } else {
                            throw new JSONParseException("Carácter inválido");
                        }
                    }
                }
            } else if (ch == '"') {
                return new Token(TokenType.STRING, sb.toString());
            } else if (ch == '\r' || ch == '\n') {
                throw new JSONParseException("Carácter inválido");
            } else {
                sb.append(ch);
            }
        }
    }

    private Token readNumber() throws IOException {
        char ch = charReader.peek();
        StringBuilder sb = new StringBuilder();
        if (ch == '-') {  
            sb.append(ch);
            ch = charReader.next();
            if (ch == '0') {    
                sb.append(ch);
                sb.append(readFracAndExp());
            } else if (isDigitOne2Nine(ch)) {
                do {
                    sb.append(ch);
                    ch = charReader.next();
                } while (isDigit(ch));
                if (ch != (char) -1) {
                    charReader.back();
                    sb.append(readFracAndExp());
                }
            } else {
                throw new JSONParseException("Invalid minus number");
            }
        } else if (ch == '0') {  
            sb.append(ch);
            sb.append(readFracAndExp());
        } else {
            do {
                sb.append(ch);
                ch = charReader.next();
            } while (isDigit(ch));
            if (ch != (char) -1) {
                charReader.back();
                sb.append(readFracAndExp());
            }
        }

        return new Token(TokenType.NUMBER, sb.toString());
    }
	private boolean isExp(char ch) throws IOException {
		return ch == 'e' || ch == 'E';
	}

	private boolean isDigit(char ch) {
		return ch >= '0' && ch <= '9';
	}

	private boolean isDigitOne2Nine(char ch) {
		return ch >= '0' && ch <= '9';
	}

	private String readFracAndExp() throws IOException {
		StringBuilder sb = new StringBuilder();
		char ch = charReader.next();
		if (ch == '.') {
			sb.append(ch);
			ch = charReader.next();
			do {
				sb.append(ch);
				ch = charReader.next();
			} while (isDigit(ch));

			if (isExp(ch)) {
				sb.append(ch);
				sb.append(readExp());
			} else {
				if (ch != (char) -1) {
					charReader.back();
				}
			}
		} else if (isExp(ch)) {
			sb.append(ch);
			sb.append(readExp());
		} else {
			charReader.back();
		}

		return sb.toString();
	}

	private String readExp() throws IOException {
		StringBuilder sb = new StringBuilder();
		char ch = charReader.next();
		if (ch == '+' || ch == '-') {
			sb.append(ch);
			ch = charReader.next();
			if (isDigit(ch)) {
				do {
					sb.append(ch);
					ch = charReader.next();
				} while (isDigit(ch));

				if (ch != (char) -1) {
					charReader.back();
				}
			}
		}
		return sb.toString();
	}

	private Token readBoolean() throws IOException {
		if (charReader.peek() == 't') {
			if (charReader.next() == 'r' && charReader.next() == 'u' && charReader.next() == 'e')
				return new Token(TokenType.BOOLEAN, PR_TRUE);
		} else {
			if (charReader.next() == 'a' && charReader.next() == 'l' && charReader.next() == 's'
					&& charReader.next() == 'e')
				return new Token(TokenType.BOOLEAN, PR_FALSE);
		}
		return readString();
	}

	private Token readNull() throws IOException {
		if (charReader.next() == 'u' && charReader.next() == 'l' && charReader.next() == 'l')
			return new Token(TokenType.NULL, PR_NULL);
		return readString();
	}

	private boolean isEscape() throws IOException {
		char ch = charReader.next();
		return (ch == '"' || ch == '\\' || ch == 'u' || ch == 'r'
				|| ch == 'n' || ch == 'b' || ch == 't' || ch == 'f');
	}

	private boolean isHex(char ch) {
		return ((ch >= '0' && ch <= '9') || ('a' <= ch && ch <= 'f')
				|| ('A' <= ch && ch <= 'F'));
	}
}
