
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

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parseAST() {
        ASTNode root = new ASTNode("P");
        while (match(TokenType.PRvar, TokenType.PRif, TokenType.id, TokenType.PRfor, TokenType.PRoutput, TokenType.PRfun)) {
            if (check(TokenType.PRfun)) {
                root.addChild(F());
            } else {
                root.addChild(B());
            }
        }
        return root;
    }

    private ASTNode F() {
        ASTNode node = new ASTNode("F");
        consume(TokenType.PRfun, "Se esperaba 'function'");
        node.addChild(new ASTNode("function"));
        node.addChild(F2());
        Token id = consume(TokenType.id, "Se esperaba nombre de función");
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        consume(TokenType.parenIzq, "Falta '('");
        if (!check(TokenType.parenDcha)) {
            node.addChild(Z());
        }
        consume(TokenType.parenDcha, "Falta ')'");
        consume(TokenType.llaveIzq, "Falta '{'");
        node.addChild(C());
        consume(TokenType.llaveDcha, "Falta '}'");
        return node;
    }

    private ASTNode F2() {
        if (match(TokenType.PRint)) return new ASTNode("int");
        if (match(TokenType.PRboolean)) return new ASTNode("boolean");
        if (match(TokenType.PRstring)) return new ASTNode("string");
        if (match(TokenType.PRvoid)) return new ASTNode("void");
        error(peek(), "Tipo de retorno no válido");
        return new ASTNode("tipo_error");
    }

    private ASTNode Z() {
        ASTNode node = new ASTNode("Z");
        node.addChild(T());
        Token id = consume(TokenType.id, "Falta identificador");
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        while (match(TokenType.coma)) {
            node.addChild(new ASTNode(","));
            node.addChild(T());
            Token id2 = consume(TokenType.id, "Falta identificador");
            node.addChild(new ASTNode("id(" + id2.lexeme + ")"));
        }
        return node;
    }

    private ASTNode T() {
        if (match(TokenType.PRint)) return new ASTNode("int");
        if (match(TokenType.PRboolean)) return new ASTNode("boolean");
        if (match(TokenType.PRstring)) return new ASTNode("string");
        error(peek(), "Tipo no válido");
        return new ASTNode("tipo_error");
    }

    private ASTNode C() {
        ASTNode node = new ASTNode("C");
        while (!check(TokenType.llaveDcha) && !isAtEnd()) {
            node.addChild(B());
        }
        return node;
    }

    private ASTNode B() {
        ASTNode node = new ASTNode("B");
        if (match(TokenType.PRvar)) {
            node.addChild(new ASTNode("var"));
            ASTNode tipo = T();
            node.addChild(tipo);
            Token id = consume(TokenType.id, "Se esperaba identificador");
            node.addChild(new ASTNode("id(" + id.lexeme + ")"));
            Symbol s = new Symbol(id.lexeme);
            s.setTipo(tipo.toString());
            tablaSimbolos.add(s);
            consume(TokenType.puntoComa, "Falta ';'");
        } else {
            node.addChild(new ASTNode("sentencia"));
        }
        return node;
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

    public void exportarTS(String ruta) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ruta));
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            writer.write((i + 1) + ": " + tablaSimbolos.get(i).toString());
            writer.newLine();
        }
        writer.close();
    }

    public String getErrores() {
        return errores.toString();
    }
}
