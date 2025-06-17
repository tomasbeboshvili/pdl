package Lexico;

import Sintactico.Parser;
import Sintactico.ASTNode;
import java.io.*;
import java.util.List;

public class TesterSemantico {
    public static void main(String[] args) {
        try {
            // 🧹 Limpiar carpeta de salida
            File outputDir = new File("salida_tests");
            if (outputDir.exists() && outputDir.isDirectory()) {
                for (File file : outputDir.listFiles()) {
                    if (!file.isDirectory()) file.delete();
                }
            }

            // 📥 Leer archivo fuente
            File inputFile = new File("entrada_tests/test1.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            StringBuilder input = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) input.append(line).append("\n");
            reader.close();

            // 🧠 Lexer
            Lexer lexer = new Lexer(input.toString());
            List<Token> tokens = lexer.tokenize();

            // 📝 Guardar tokens en formato legible
            BufferedWriter tokenWriter = new BufferedWriter(new FileWriter("salida_tests/test1_tokens.txt"));
            for (Token t : tokens) {
                tokenWriter.write(t.toString());
                tokenWriter.newLine();
            }
            tokenWriter.close();

            // 🌳 Parser (y acciones semánticas integradas)
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parseAST();
			parser.exportarParse("salida_tests/test1_parse.txt");


            // 📤 AST (formato .dot en .txt)
            BufferedWriter dotWriter = new BufferedWriter(new FileWriter("salida_tests/test1_ast.txt"));
            dotWriter.write(ast.toDotFile());
            dotWriter.close();

            // 📦 Tabla de símbolos
            parser.exportarTS("salida_tests/test1_tabla_simbolos.txt");

            // ⚠️ Errores (si hay)
            BufferedWriter errWriter = new BufferedWriter(new FileWriter("salida_tests/test1_errores.txt"));
            if (!parser.getErrores().isEmpty()) {
                errWriter.write(parser.getErrores());
            } else {
                errWriter.write("No se encontraron errores.");
            }
            errWriter.close();

            System.out.println("✅ Tokens, AST, TS y errores exportados a carpeta salida_tests.");

        } catch (Exception e) {
            System.err.println("❌ Error durante la ejecución: " + e.getMessage());
        }
    }
}
