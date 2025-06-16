import java.io.*;
import java.util.List;

public class LexerTest {
    public static void main(String[] args) {
        try {
            // Leer archivo de entrada
            BufferedReader reader = new BufferedReader(new FileReader("entrada.txt"));
            StringBuilder input = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                input.append(line).append("\n");
            }
            reader.close();

            // Crear lexer y obtener tokens
            Lexer lexer = new Lexer(input.toString());
            List<Token> tokens = lexer.scanTokens();

            // Escribir tokens en archivo de salida
            BufferedWriter writer = new BufferedWriter(new FileWriter("salida.txt"));
            for (Token token : tokens) {
                writer.write(token.toString());
                writer.newLine();
            }
            writer.close();

            System.out.println("Análisis completado. Revisa el archivo salida.txt.");

        } catch (IOException e) {
            System.err.println("Error de archivo: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error de análisis: " + e.getMessage());
        }
    }
}
