
package Lexico;

import Sintactico.Parser;
import Sintactico.ASTNode;

import java.io.*;
import java.util.List;

public class TesterSintactico {
    public static void main(String[] args) {
        try {
            File out = new File("salida_tests");
            if (!out.exists()) out.mkdirs();
            for (File f : out.listFiles()) if (!f.isDirectory()) f.delete();

            BufferedReader reader = new BufferedReader(new FileReader("entrada_tests/test1.txt"));
            StringBuilder input = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) input.append(line).append("\n");
            reader.close();

            Lexer lexer = new Lexer(input.toString());
            List<Token> tokens = lexer.tokenize();

            BufferedWriter tokenWriter = new BufferedWriter(new FileWriter("salida_tests/test1_tokens.txt"));
            for (Token t : tokens) {
                tokenWriter.write(t.toString());
                tokenWriter.newLine();
            }
            tokenWriter.close();

            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parseAST();
            parser.exportarParse("salida_tests/test1_parse.txt");

            if (ast != null) {
                BufferedWriter dot = new BufferedWriter(new FileWriter("salida_tests/test1_ast.txt"));
                dot.write(ast.toDotFile());
                dot.close();
            }

            parser.exportarTS("salida_tests/test1_tabla_simbolos.txt");

            BufferedWriter errWriter = new BufferedWriter(new FileWriter("salida_tests/test1_errores.txt"));
            if (!parser.getErrores().isEmpty()) {
                errWriter.write(parser.getErrores());
            } else {
                errWriter.write("No se encontraron errores.");
            }
            errWriter.close();

            System.out.println("✅ Todo generado en salida_tests.");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
