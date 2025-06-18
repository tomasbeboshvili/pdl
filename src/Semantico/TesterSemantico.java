package Semantico;
import Lexico.*;
import Sintactico.*;
import Semantico.SemanticAnalyzer;

import java.util.List;

public class TesterSemantico {
    public static void main(String[] args) {
        // Simula el análisis léxico (sustituye esto por tu analizador léxico real)
        List<Token> tokens = List.of(
            new Token(TokenType.PRvar, "var", 1, 0, 3),
            new Token(TokenType.PRint, "int", 1, 4, 7),
            new Token(TokenType.id, "x", 1, 8, 9),
            new Token(TokenType.puntoComa, ";", 1, 9, 10),
            new Token(TokenType.id, "x", 2, 0, 1),
            new Token(TokenType.igual, "=", 2, 2, 3),
            new Token(TokenType.entero, "5", 2, 4, 5),
            new Token(TokenType.puntoComa, ";", 2, 5, 6),
            new Token(TokenType.finFich, "", 3, 0, 0)
        );

        // Ejecuta parser
        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parseAST();

        if (!parser.getErrores().isEmpty()) {
            System.out.println("Errores de sintaxis:");
            System.out.println(parser.getErrores());
            return;
        }

        // Ejecuta análisis semántico
        SemanticAnalyzer sema = new SemanticAnalyzer();
        sema.analizar(ast);

        // Imprime resultados
        if (sema.getErrores().isEmpty()) {
            System.out.println("Análisis semántico completado sin errores.");
        } else {
            System.out.println("Errores semánticos:");
            System.out.println(sema.getErrores());
        }
    }
}
