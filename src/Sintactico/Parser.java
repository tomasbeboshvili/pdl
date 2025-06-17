package Sintactico;

import java.util.*;
import java.io.*;
import Lexico.Symbol;
import Lexico.Token;
import Lexico.TokenType;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final StringBuilder errores = new StringBuilder();
    private boolean hayErrores = false;
    private final List<Symbol> tablaSimbolos = new ArrayList<>();
    private final List<Integer> reglasAplicadas = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parseAST() {
        ASTNode root = new ASTNode("P");
        while (!isAtEnd()) {
            if (check(TokenType.PRfun)) {
                reglasAplicadas.add(2); // P -> F P
                root.addChild(F());
            } else {
                reglasAplicadas.add(1); // P -> B P
                root.addChild(B());
            }
        }
        reglasAplicadas.add(3); // P -> λ
        return root;
    }

    public void exportarParse(String ruta) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ruta));
        writer.write("descendente");
        for (int num : reglasAplicadas) {
            writer.write(" " + num);
        }
        writer.newLine();
        writer.close();
    }

    private ASTNode F() {
        reglasAplicadas.add(47);
        ASTNode node = new ASTNode("F");
        consume(TokenType.PRfun, "Se esperaba 'function'");
        node.addChild(new ASTNode("function"));
        node.addChild(F2());
        Token id = consume(TokenType.id, "Se esperaba nombre de función");
        reglasAplicadas.add(49);
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        consume(TokenType.parenIzq, "Falta '('");
        node.addChild(F4());
        consume(TokenType.llaveIzq, "Falta '{'");
        node.addChild(C());
        consume(TokenType.llaveDcha, "Falta '}'");
        return node;
    }

    private ASTNode F2() {
        reglasAplicadas.add(48);
        return H();
    }

    private ASTNode F4() {
        reglasAplicadas.add(50);
        ASTNode node = new ASTNode("F4");
        consume(TokenType.parenIzq, "Falta '('");
        if (check(TokenType.PRint) || check(TokenType.PRboolean) || check(TokenType.PRstring)) {
            node.addChild(Z());
        } else if (match(TokenType.PRvoid)) {
            reglasAplicadas.add(54);
            node.addChild(new ASTNode("void"));
        } else {
            reglasAplicadas.add(56);
        }
        consume(TokenType.parenDcha, "Falta ')'");
        return node;
    }

    private ASTNode H() {
        if (check(TokenType.PRint) || check(TokenType.PRboolean) || check(TokenType.PRstring)) {
            reglasAplicadas.add(51);
            return T();
        } else if (match(TokenType.PRvoid)) {
            reglasAplicadas.add(52);
            return new ASTNode("void");
        } else {
            error(peek(), "Se esperaba tipo o void");
            return new ASTNode("tipo_error");
        }
    }

    private ASTNode Z() {
        reglasAplicadas.add(53);
        ASTNode node = new ASTNode("Z");
        node.addChild(T());
        Token id = consume(TokenType.id, "Falta identificador");
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        node.addChild(K());
        return node;
    }

    private ASTNode K() {
        ASTNode node = new ASTNode("K");
        if (match(TokenType.coma)) {
            reglasAplicadas.add(55);
            node.addChild(new ASTNode(","));
            node.addChild(T());
            Token id = consume(TokenType.id, "Falta identificador");
            node.addChild(new ASTNode("id(" + id.lexeme + ")"));
            node.addChild(K());
        } else {
            reglasAplicadas.add(56);
        }
        return node;
    }

    private ASTNode T() {
        if (match(TokenType.PRint)) {
            reglasAplicadas.add(39); return new ASTNode("int");
        }
        if (match(TokenType.PRboolean)) {
            reglasAplicadas.add(40); return new ASTNode("boolean");
        }
        if (match(TokenType.PRstring)) {
            reglasAplicadas.add(41); return new ASTNode("string");
        }
        error(peek(), "Tipo no válido");
        return new ASTNode("tipo_error");
    }

    private ASTNode C() {
        ASTNode node = new ASTNode("C");
        while (!check(TokenType.llaveDcha) && !check(TokenType.finFich) && !hayErrores) {
            node.addChild(B());
            reglasAplicadas.add(45); // C -> B C
        }
        reglasAplicadas.add(46); // C -> λ
        return node;
    }

    private ASTNode B() {
        ASTNode node = new ASTNode("B");
        if (match(TokenType.PRvar)) {
            reglasAplicadas.add(36);
            node.addChild(new ASTNode("var"));
            ASTNode tipo = T();
            node.addChild(tipo);
            Token id = consume(TokenType.id, "Se esperaba identificador");
            node.addChild(new ASTNode("id(" + id.lexeme + ")"));
            Symbol s = new Symbol(id.lexeme);
            s.setTipo(tipo.getLabel());
            tablaSimbolos.add(s);
            consume(TokenType.puntoComa, "Falta ';'");
        } else {
            reglasAplicadas.add(37);
            node.addChild(new ASTNode("sentencia"));
        }
        return node;
    }

    public void exportarTS(String ruta) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ruta));
        writer.write("#1:");
        writer.newLine();

        int desplazamiento = 0;
        for (Symbol s : tablaSimbolos) {
            writer.write("* LEXEMA : '" + s.getLexema() + "'");
            writer.newLine();
            writer.write("ATRIBUTOS:");
            writer.newLine();
            writer.write("+ tipo : '" + mapTipo(s.getTipo()) + "'");
            writer.newLine();
            writer.write("+ despl : " + desplazamiento);
            writer.newLine();
            writer.write("----------");
            writer.newLine();
            desplazamiento += 1;
        }

        writer.close();
    }

    public String mapTipo(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "int" -> "entero";
            case "boolean" -> "logico";
            case "string" -> "cadena";
            default -> "-";
        };
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.finFich;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        error(peek(), message);
        return new Token(TokenType.finFich, "", peek().line, 0, 0);
    }

    private void error(Token token, String message) {
        hayErrores = true;
        errores.append("[ERROR - Línea ").append(token.line).append("]: ").append(message).append("\n");
    }

    public String getErrores() {
        return errores.toString();
    }
}
