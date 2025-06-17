package Lexico;
import java.io.*;
import java.util.*;

public class LexerBatchTest {
    public static void main(String[] args) {
		File tablaSimbolos= new File("tabla_simbolos.txt");
        File entradaFolder = new File("entrada_tests");
        File salidaFolder = new File("salida_tests");

        if (!salidaFolder.exists()) {
            salidaFolder.mkdirs();
        }

        File[] testFiles = entradaFolder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (testFiles == null || testFiles.length == 0) {
            System.out.println("No se encontraron archivos de prueba en entrada_tests/");
            return;
        }

        for (File testFile : testFiles) {
            String nombre = testFile.getName();
            String nombreSalida = nombre.replace(".txt", "_out.txt");
            File outputFile = new File(salidaFolder, nombreSalida);


            System.out.println("Procesando: " + nombre);

            try {
                BufferedReader reader = new BufferedReader(new FileReader(testFile));
                StringBuilder input = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    input.append(line).append("\n");
                }
                reader.close();

                Lexer lexer = new Lexer(input.toString());
                List<Token> tokens = lexer.tokenize();

                BufferedWriter writerRaw = new BufferedWriter(new FileWriter(new File(salidaFolder, nombreSalida.replace(".txt", "_raw.txt"))));
				BufferedWriter writerTokens = new BufferedWriter(new FileWriter(new File(salidaFolder, nombreSalida.replace(".txt", "_tokens.txt"))));

				for (Token token : tokens) {
					String raw = token.toString();
					String formatted = token.toCleanFormattedString(lexer.getSymbolTable());

					if (!formatted.isEmpty()) {
						writerTokens.write(formatted);
						writerTokens.newLine();
					}

					writerRaw.write(raw);
					writerRaw.newLine();
				}
				writerTokens.write("<finFich, >");
				writerRaw.write("<finFich, >");
				writerRaw.close();
				writerTokens.close();
				
				BufferedWriter writerSymbol = new BufferedWriter(new FileWriter(new File(salidaFolder, tablaSimbolos.getName())));
				writerSymbol.write(lexer.printSymbolTable());
				writerSymbol.close();

				System.out.println("✅ " + nombre + " procesado correctamente.");
            } catch (IOException e) {
                System.err.println("[ERROR]: No se pudo procesar " + nombre + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Todos los tests han sido procesados.");
    }
}
