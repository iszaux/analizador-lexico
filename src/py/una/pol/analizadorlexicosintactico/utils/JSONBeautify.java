package py.una.pol.analizadorlexicosintactico.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import py.una.pol.analizadorlexicosintactico.parser.JsonArray;
import py.una.pol.analizadorlexicosintactico.parser.JsonObject;
import py.una.pol.analizadorlexicosintactico.tokenizer.Token;
import py.una.pol.analizadorlexicosintactico.tokenizer.TokenList;
import py.una.pol.analizadorlexicosintactico.tokenizer.TokenType;

public class JSONBeautify {

    private static final char ESPACIO = ' ';

    private static final int INDENTAR = 4;

    private static int callDepth = 0;

    private static int callDepthXml = -1;

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

    public static String beautify(JsonObject jsonObject) {

        StringBuilder sb = new StringBuilder();
        sb.append(getIndentString());
        sb.append("{");
        callDepth++;

        List<Map.Entry<String, Object>> keyValues = jsonObject.getAllKeyValue();
        int size = keyValues.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<String, Object> keyValue = keyValues.get(i);

            String key = keyValue.getKey();
            Object value = keyValue.getValue();

            sb.append("\n");
            sb.append(getIndentString());
            sb.append("\"");
            sb.append(key);
            sb.append("\"");
            sb.append(": ");

            if (value instanceof JsonObject) {
                sb.append("\n");
                sb.append(beautify((JsonObject) value));
            } else if (value instanceof JsonArray) {
                sb.append("\n");
                sb.append(beautify((JsonArray) value));
            } else if (value instanceof String) {
                sb.append("\"");
                sb.append(value);
                sb.append("\"");
            } else {
                sb.append(value);
            }

            if (i < size - 1) {
                sb.append(",");
            }
        }

        callDepth--;
        sb.append("\n");
        sb.append(getIndentString());
        sb.append("}");

        return sb.toString();
    }

    public static String beautify(JsonArray jsonArray) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndentString());
        sb.append("[");
        callDepth++;

        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {

            sb.append("\n");

            Object ele = jsonArray.get(i);
            if (ele instanceof JsonObject) {
                sb.append(beautify((JsonObject) ele));
            } else if (ele instanceof JsonArray) {
                sb.append(beautify((JsonArray) ele));
            } else if (ele instanceof String) {
                sb.append(getIndentString());
                sb.append("\"");
                sb.append(ele);
                sb.append("\"");
            } else {
                sb.append(getIndentString());
                sb.append(ele);
            }

            if (i < size - 1) {
                sb.append(",");
            }
        }

        callDepth--;
        sb.append("\n");
        sb.append(getIndentString());
        sb.append("]");

        return sb.toString();
    }

    private static String getIndentString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < callDepth * INDENTAR; i++) {
            sb.append(ESPACIO);
        }
        return sb.toString();
    }

    private static String getIndentStringXml() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < callDepthXml * INDENTAR; i++) {
            sb.append(ESPACIO);
        }
        return sb.toString();
    }

    public static String jsonToXml(JsonObject jsonObject) {

        StringBuilder sb = new StringBuilder();
        sb.append(getIndentStringXml());
        callDepthXml++;

        List<Map.Entry<String, Object>> keyValues = jsonObject.getAllKeyValue();
        int size = keyValues.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<String, Object> keyValue = keyValues.get(i);

            String key = keyValue.getKey();
            Object value = keyValue.getValue();

            sb.append("\n");
            sb.append(getIndentStringXml());

            sb.append("<");
            sb.append(key);
            sb.append(">");

            if (value instanceof JsonObject) {
                sb.append("\n");
                sb.append(jsonToXml((JsonObject) value));
            } else if (value instanceof JsonArray) {
                if (((JsonArray) value).size() > 0) {
                    sb.append("\n");
                    sb.append(jsonToXml((JsonArray) value));
                }
            } else if (value instanceof String) {
                sb.append("\"");
                sb.append(value);
                sb.append("\"");
            } else {
                sb.append(value);
            }

            sb.append("</");
            sb.append(key);
            sb.append(">");

        }

        callDepthXml--;
        sb.append("\n");
        sb.append(getIndentStringXml());

        StringBuilder str = new StringBuilder(sb.toString());
        sb.deleteCharAt(str.length() - 1);
        sb.deleteCharAt(0);

        return sb.toString();
    }

    public static String jsonToXml(JsonArray jsonArray) {
        StringBuilder sb = new StringBuilder();

        callDepthXml++;
        sb.append(getIndentStringXml());
        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {

            Object ele = jsonArray.get(i);
            if (ele instanceof JsonObject) {
                sb.append("<item>");
                sb.append(jsonToXml((JsonObject) ele));
                sb.append(" </item>");
                if (i < size - 1)
                    sb.append("\n");
                sb.append(getIndentStringXml());
            } else if (ele instanceof JsonArray) {
                sb.append(jsonToXml((JsonArray) ele));
            } else if (ele instanceof String) {
                sb.append("\"");
                sb.append(ele);
                sb.append("\"");
            } else {
                sb.append(getIndentStringXml());
                sb.append(ele);
            }
        }

        callDepthXml--;
        sb.append("\n");
        sb.append(getIndentStringXml());

        return sb.toString();
    }
}
