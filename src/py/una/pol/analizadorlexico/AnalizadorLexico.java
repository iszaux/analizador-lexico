package py.una.pol.analizadorlexico;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import py.una.pol.analizadorlexico.tokenizer.CharReader;
import py.una.pol.analizadorlexico.tokenizer.Token;
import py.una.pol.analizadorlexico.tokenizer.TokenList;
import py.una.pol.analizadorlexico.tokenizer.TokenType;
import py.una.pol.analizadorlexico.tokenizer.Tokenizer;
import py.una.pol.analizadorlexico.utils.JSONBeautify;

public class AnalizadorLexico {

	private static Tokenizer tokenizer = new Tokenizer();
	private static JSONBeautify jsonBeautify = new JSONBeautify();

	public static void main(String[] args) {
		String path = args[0];

		try {
			String json = new String(Files.readAllBytes(Paths.get(path)), "utf-8");
			CharReader charReader = new CharReader(new StringReader(json.toLowerCase()));
			TokenList tokens = tokenizer.tokenize(charReader);
			try {
				FileWriter myWriter = new FileWriter("output.txt");
				myWriter.write(jsonBeautify.beautify(tokens));
				myWriter.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
