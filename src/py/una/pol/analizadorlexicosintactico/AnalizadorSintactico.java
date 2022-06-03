package py.una.pol.analizadorlexicosintactico;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import py.una.pol.analizadorlexicosintactico.parser.JsonObject;
import py.una.pol.analizadorlexicosintactico.parser.Parser;
import py.una.pol.analizadorlexicosintactico.tokenizer.*;
import py.una.pol.analizadorlexicosintactico.utils.JSONBeautify;

public class AnalizadorSintactico {

	private static Tokenizer tokenizer = new Tokenizer();

	public static void main(String[] args) {
		String path = args[0];

		try {
			String json = new String(Files.readAllBytes(Paths.get(path)), "utf-8");
			CharReader charReader = new CharReader(new StringReader(json));
			TokenList tokens = tokenizer.tokenize(charReader);

			Parser parser = new Parser();
			JsonObject jsonObject = (JsonObject) parser.parse(tokens);

			if (parser.tieneErrores())
				System.out.println(parser.getErrores());
			else
				System.out.println("Sintácticamente correcto.");

			try {
				FileWriter myWriter = new FileWriter("output.xml");
				myWriter.write(JSONBeautify.jsonToXml(jsonObject));
				myWriter.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
