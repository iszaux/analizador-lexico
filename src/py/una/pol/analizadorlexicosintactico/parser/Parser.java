package py.una.pol.analizadorlexicosintactico.parser;

import py.una.pol.analizadorlexicosintactico.exception.JSONParseException;
import py.una.pol.analizadorlexicosintactico.tokenizer.Token;
import py.una.pol.analizadorlexicosintactico.tokenizer.TokenList;
import py.una.pol.analizadorlexicosintactico.tokenizer.TokenType;

public class Parser {

    private TokenList tokens;
    private StringBuilder errores = new StringBuilder("Errores sintácticos: ").append("\n");
    private boolean tieneErrores = false;

    public String getErrores() {
        return this.errores.toString();
    }

    public boolean tieneErrores() {
        return this.tieneErrores;
    }

    public Object parse(TokenList tokens) {
        this.tokens = tokens;
        return parse();

    }

    public Object parse() {
        Token token = tokens.next();
        if (token == null) {
            return new JsonObject();
        } else if (token.getTokenType() == TokenType.BEGIN_OBJECT) {
            return parseJsonObject();
        } else if (token.getTokenType() == TokenType.BEGIN_ARRAY) {
            return parseJsonArray();
        } else {
            throw new JSONParseException("Error de parseo, Token inválido.");
        }

    }

    private JsonObject parseJsonObject() {
        JsonObject jsonObject = new JsonObject();
        int expectToken = TokenType.STRING.getTokenCode() | TokenType.END_OBJECT.getTokenCode();
        String key = null;
        Object value = null;
        while (tokens.hasMore()) {
            Token token = tokens.next();
            TokenType tokenType = token.getTokenType();
            String tokenValue = token.getValue();
            switch (tokenType) {
                case BEGIN_OBJECT:
                    checkExpectToken(token, expectToken);
                    jsonObject.put(key, parseJsonObject()); // 递归解析 json object
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_OBJECT.getTokenCode();
                    break;
                case END_OBJECT:
                    checkExpectToken(token, expectToken);
                    return jsonObject;
                case BEGIN_ARRAY:
                    checkExpectToken(token, expectToken);
                    jsonObject.put(key, parseJsonArray());
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_OBJECT.getTokenCode();
                    break;
                case NULL:
                    checkExpectToken(token, expectToken);
                    jsonObject.put(key, null);
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_OBJECT.getTokenCode();
                    break;
                case NUMBER:
                    checkExpectToken(token, expectToken);
                    if (tokenValue.contains(".") || tokenValue.contains("e") || tokenValue.contains("E")) {
                        jsonObject.put(key, Double.valueOf(tokenValue));
                    } else {
                        Long num = Long.valueOf(tokenValue);
                        if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
                            jsonObject.put(key, num);
                        } else {
                            jsonObject.put(key, num.intValue());
                        }
                    }
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_OBJECT.getTokenCode();
                    break;
                case BOOLEAN:
                    checkExpectToken(token, expectToken);
                    jsonObject.put(key, Boolean.valueOf(token.getValue()));
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_OBJECT.getTokenCode();
                    break;
                case STRING:
                    checkExpectToken(token, expectToken);
                    Token preToken = tokens.peekPrevious();
                    if (preToken.getTokenType() == TokenType.SEP_COLON) {
                        value = token.getValue();
                        jsonObject.put(key, value);
                        expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_OBJECT.getTokenCode();
                    } else {
                        key = token.getValue();
                        expectToken = TokenType.SEP_COLON.getTokenCode();
                    }
                    break;
                case SEP_COLON:
                    checkExpectToken(token, expectToken);
                    expectToken = TokenType.NULL.getTokenCode() | TokenType.NUMBER.getTokenCode()
                            | TokenType.BOOLEAN.getTokenCode() | TokenType.STRING.getTokenCode()
                            | TokenType.BEGIN_OBJECT.getTokenCode() | TokenType.BEGIN_ARRAY.getTokenCode();
                    break;
                case SEP_COMMA:
                    checkExpectToken(token, expectToken);
                    expectToken = TokenType.STRING.getTokenCode();
                    break;
                case END_DOCUMENT:
                    checkExpectToken(token, expectToken);
                    return jsonObject;
                default:
                    throw new JSONParseException("Token inesperado.");
            }
        }

        throw new JSONParseException("Error de parseo, Token inválido.");
    }

    private void checkExpectToken(Token token, int expectToken) {
        if ((token.getTokenType().getTokenCode() & expectToken) == 0) {
            tieneErrores = true;
            errores.append("Token inválido previo al token: " + token.getValue());
            errores.append("\n");
        }
    }

    private JsonArray parseJsonArray() {
        int expectToken = TokenType.BEGIN_ARRAY.getTokenCode() | TokenType.END_ARRAY.getTokenCode()
                | TokenType.BEGIN_OBJECT.getTokenCode() | TokenType.NULL.getTokenCode()
                | TokenType.NUMBER.getTokenCode() | TokenType.BOOLEAN.getTokenCode() | TokenType.STRING.getTokenCode();

        JsonArray jsonArray = new JsonArray();
        while (tokens.hasMore()) {
            Token token = tokens.next();
            TokenType tokenType = token.getTokenType();
            String tokenValue = token.getValue();
            switch (tokenType) {
                case BEGIN_OBJECT:
                    checkExpectToken(token, expectToken);
                    jsonArray.add(parseJsonObject());
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_ARRAY.getTokenCode();
                    break;
                case BEGIN_ARRAY:
                    checkExpectToken(token, expectToken);
                    jsonArray.add(parseJsonArray());
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_ARRAY.getTokenCode();
                    break;
                case END_ARRAY:
                    checkExpectToken(token, expectToken);
                    return jsonArray;
                case NULL:
                    checkExpectToken(token, expectToken);
                    jsonArray.add(null);
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_ARRAY.getTokenCode();
                    break;
                case NUMBER:
                    checkExpectToken(token, expectToken);
                    if (tokenValue.contains(".") || tokenValue.contains("e") || tokenValue.contains("E")) {
                        jsonArray.add(Double.valueOf(tokenValue));
                    } else {
                        Long num = Long.valueOf(tokenValue);
                        if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
                            jsonArray.add(num);
                        } else {
                            jsonArray.add(num.intValue());
                        }
                    }
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_ARRAY.getTokenCode();
                    break;
                case BOOLEAN:
                    checkExpectToken(token, expectToken);
                    jsonArray.add(Boolean.valueOf(tokenValue));
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_ARRAY.getTokenCode();
                    break;
                case STRING:
                    checkExpectToken(token, expectToken);
                    jsonArray.add(tokenValue);
                    expectToken = TokenType.SEP_COMMA.getTokenCode() | TokenType.END_ARRAY.getTokenCode();
                    break;
                case SEP_COMMA:
                    checkExpectToken(token, expectToken);
                    expectToken = TokenType.STRING.getTokenCode() | TokenType.NULL.getTokenCode()
                            | TokenType.NUMBER.getTokenCode() | TokenType.BOOLEAN.getTokenCode()
                            | TokenType.BEGIN_ARRAY.getTokenCode() | TokenType.BEGIN_OBJECT.getTokenCode();
                    break;
                case END_DOCUMENT:
                    checkExpectToken(token, expectToken);
                    return jsonArray;
                default:
                    throw new JSONParseException("Token inesperado.");
            }
        }

        throw new JSONParseException("Error de parseo, Token inválido.");
    }
}
