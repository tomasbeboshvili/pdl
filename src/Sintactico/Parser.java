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
/* 
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
        */
        public ASTNode parseAST() {
        ASTNode root = new ASTNode("P");
        while (!isAtEnd()) {
            if (check(TokenType.PRif) || check(TokenType.PRvar) || check(TokenType.PRfor) || check(TokenType.id) ||
                check(TokenType.PRoutput) || check(TokenType.PRinput) || check(TokenType.PRreturn)){
                    reglasAplicadas.add(1);     // P -> B P
                    root.addChild(B());
            }
            else if (check(TokenType.PRfun)) {
                reglasAplicadas.add(2); // P -> F P
                root.addChild(F());
            } else {
                error(peek(), "no se esperaba este token");
                return new ASTNode("tipo_error");
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
        node.addChild(F3());
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
        if (match(TokenType.PRif)){
            reglasAplicadas.add(35);    // B -> if ( E ) S
            consume(TokenType.parenIzq, "Se esperaba un '('");
            node.addChild(E());
            consume(TokenType.parenDcha, "Se esperaba un ')'");
            node.addChild(S());
        } else if (match(TokenType.PRvar)) {
            reglasAplicadas.add(36);    // B -> var T id ;
            node.addChild(new ASTNode("var"));
            ASTNode tipo = T();
            node.addChild(tipo);
            Token id = consume(TokenType.id, "Se esperaba identificador");
            node.addChild(new ASTNode("id(" + id.lexeme + ")"));
            Symbol s = new Symbol(id.lexeme);
            s.setTipo(tipo.getLabel());
            tablaSimbolos.add(s);  
            consume(TokenType.puntoComa, "Falta ';'");
        } else if (match(TokenType.PRfor)){
            reglasAplicadas.add(38);    // B -> for ( F1 ; E ; A ) { C }
            consume(TokenType.parenIzq, "Se esperaba un '('");
            node.addChild(F1());
            consume(TokenType.puntoComa, "Se esperaba un ';'");
            node.addChild(E());
            consume(TokenType.puntoComa, "Se esperaba un ';'");
            node.addChild(A());
            consume(TokenType.parenDcha, "Se esperaba un ')'");
            consume(TokenType.llaveIzq, "Falta '{'");
            node.addChild(C());
            consume(TokenType.llaveDcha, "Falta '}'");
        } else {
            reglasAplicadas.add(37);    // B -> S
            node.addChild(S());
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
	// ... (todo tu código anterior hasta el final del método B()) permanece igual

// Añadir desde aquí las reglas faltantes

private ASTNode E() {
    reglasAplicadas.add(4); // E -> R E1
    ASTNode node = new ASTNode("E");
    node.addChild(R());
    node.addChild(E1());
    return node;
}

private ASTNode E1() {
    ASTNode node = new ASTNode("E1");
    if (match(TokenType.opAnd)) {
        reglasAplicadas.add(5); // E1 -> && R E1
        node.addChild(new ASTNode("&&"));
        node.addChild(R());
        node.addChild(E1());
    } else {
        reglasAplicadas.add(6); // E1 -> λ
    }
    return node;
}

private ASTNode R() {
    reglasAplicadas.add(7); // R -> U R1
    ASTNode node = new ASTNode("R");
    node.addChild(U());
    node.addChild(R1());
    return node;
}

private ASTNode R1() {
    ASTNode node = new ASTNode("R1");
    if (match(TokenType.opIgual)) {
        reglasAplicadas.add(8); // R1 -> == U R1
        node.addChild(new ASTNode("=="));
        node.addChild(U());
        node.addChild(R1());
    } else {
        reglasAplicadas.add(9); // R1 -> λ
    }
    return node;
}

private ASTNode U() {
    reglasAplicadas.add(10); // U -> V U1
    ASTNode node = new ASTNode("U");
    node.addChild(V());
    node.addChild(U1());
    return node;
}

private ASTNode U1() {
    ASTNode node = new ASTNode("U1");
    if (match(TokenType.opSuma)) {
        reglasAplicadas.add(11); // U1 -> + V U1
        node.addChild(new ASTNode("+"));
        node.addChild(V());
        node.addChild(U1());
    } else {
        reglasAplicadas.add(12); // U1 -> λ
    }
    return node;
}

private ASTNode V() {
    ASTNode node = new ASTNode("V");
    if (match(TokenType.id)) {
        reglasAplicadas.add(13); // V -> id V1
        Token id = previous();
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        node.addChild(V1());
    } else if (match(TokenType.parenIzq)) {
        reglasAplicadas.add(14); // V -> ( E )
        node.addChild(new ASTNode("("));
        node.addChild(E());
        consume(TokenType.parenDcha, "Falta ')'");
    } else if (match(TokenType.entero)) {
        reglasAplicadas.add(15);    // V -> ent
        node.addChild(new ASTNode("ent"));
    } else if (match(TokenType.cadena)) {
        reglasAplicadas.add(16);    // V -> cad
        node.addChild(new ASTNode("cad"));
    } else if (match(TokenType.True)) {
        reglasAplicadas.add(17);    // V -> true
        node.addChild(new ASTNode("true"));
    } else if (match(TokenType.False)) {
        reglasAplicadas.add(18);    // V -> false
        node.addChild(new ASTNode("false"));
    } else {
        error(peek(), "Expresión no válida");
    }
    return node;
}

private ASTNode V1() {
    ASTNode node = new ASTNode("V1");
    if (match(TokenType.opIncremen)) {
        reglasAplicadas.add(19);    // V1 -> ++
        node.addChild(new ASTNode("++"));
    } else if (match(TokenType.parenIzq)) {
        reglasAplicadas.add(20);
        node.addChild(new ASTNode("("));
        node.addChild(L());
        consume(TokenType.parenDcha, "Falta ')'");
    } else {
        reglasAplicadas.add(21);        // V1 -> λ
    }
    return node;
}

private ASTNode L() {
    ASTNode node = new ASTNode("L");
    if (check(TokenType.id) || check(TokenType.entero) || check(TokenType.cadena)
        || check(TokenType.True) || check(TokenType.False) || check(TokenType.parenIzq)) {
        reglasAplicadas.add(22);
        node.addChild(E());
        node.addChild(Q());
    } else {
        reglasAplicadas.add(23);
    }
    return node;
}

private ASTNode Q() {
    ASTNode node = new ASTNode("Q");
    if (match(TokenType.coma)) {
        reglasAplicadas.add(24);
        node.addChild(new ASTNode(","));
        node.addChild(E());
        node.addChild(Q());
    } else {
        reglasAplicadas.add(25);
    }
    return node;
}

private ASTNode S() {
    ASTNode node = new ASTNode("S");
    if (check(TokenType.id)) {
        if (tokens.get(current + 1).type == TokenType.parenIzq) {
            reglasAplicadas.add(27); // S -> id ( L ) ;
            Token id = advance();
            node.addChild(new ASTNode("id(" + id.lexeme + ")"));
            consume(TokenType.parenIzq, "Falta '('");
            node.addChild(L());
            consume(TokenType.parenDcha, "Falta ')'");
            consume(TokenType.puntoComa, "Falta ';'");
        } else {
            reglasAplicadas.add(26); // S -> F1
            node.addChild(F1());
            consume(TokenType.puntoComa, "falta ';'");
        }
    } else if (match(TokenType.PRoutput)) {
        reglasAplicadas.add(28);
        node.addChild(new ASTNode("output"));
        node.addChild(E());
        consume(TokenType.puntoComa, "Falta ';'");
    } else if (match(TokenType.PRinput)) {
        reglasAplicadas.add(29);
        node.addChild(new ASTNode("input"));
        Token id = consume(TokenType.id, "Falta identificador");
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        consume(TokenType.puntoComa, "Falta ';'");
    } else if (match(TokenType.PRreturn)) {
        reglasAplicadas.add(30);
        node.addChild(new ASTNode("return"));
        node.addChild(X());
        consume(TokenType.puntoComa, "Falta ';'");
    } else {
        error(peek(), "Sentencia no válida");
    }
    return node;
}

private ASTNode X() {
    ASTNode node = new ASTNode("X");
    if (!check(TokenType.puntoComa)) {
        reglasAplicadas.add(31);
        node.addChild(E());
    } else {
        reglasAplicadas.add(32);
    }
    return node;
}

private ASTNode F1() {
    ASTNode node = new ASTNode("F1");
    if (match(TokenType.id)) {
        reglasAplicadas.add(33);    //F1 -> id = E
        Token id = previous();
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        consume(TokenType.igual, "Falta '='");
        node.addChild(E());
    } else {
        reglasAplicadas.add(34);
    }
    return node;
}

private ASTNode A() {
    ASTNode node = new ASTNode("A");
    if (check(TokenType.id)) {
        if (tokens.get(current + 1).type == TokenType.igual) {
            reglasAplicadas.add(43);
            node.addChild(F1());
        } else if (tokens.get(current + 1).type == TokenType.opIncremen) {
            reglasAplicadas.add(44);
            Token id = advance();
            node.addChild(new ASTNode("id(" + id.lexeme + ")"));
            advance(); // consume ++
            node.addChild(new ASTNode("++"));
        }
    } else {
        reglasAplicadas.add(42);
    }
    return node;
}

private ASTNode F3() {
    reglasAplicadas.add(49);
    ASTNode node = new ASTNode("F3");
    Token id = consume(TokenType.id, "Se esperaba identificador");
    node.addChild(new ASTNode("id(" + id.lexeme + ")"));
    return node;
}

}
