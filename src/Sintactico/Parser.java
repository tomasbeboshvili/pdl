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
        reglasAplicadas.add(1); // P1 -> P
        return P();
    }

    private ASTNode P() {
        ASTNode node = new ASTNode("P");
        if (check(TokenType.PRif) || check(TokenType.PRvar) || check(TokenType.PRfor) ||
            check(TokenType.id) || check(TokenType.PRoutput) || check(TokenType.PRinput) || check(TokenType.PRreturn)) {
            ASTNode b = B();
            ASTNode p = P();
            node.addChild(b);
            node.addChild(p);
            reglasAplicadas.add(2); // P -> B P
        } else if (check(TokenType.PRfun)) {
            ASTNode f = F();
            ASTNode p = P();
            node.addChild(f);
            node.addChild(p);
            reglasAplicadas.add(3); // P -> F P
        } else {
            reglasAplicadas.add(4); // P -> λ
        }
        return node;
    }

    private ASTNode B() {
        ASTNode node = new ASTNode("B");
        if (match(TokenType.PRif)) {
            reglasAplicadas.add(36);
            consume(TokenType.parenIzq, "Se esperaba '(' después de 'if'");
            node.addChild(E());
            consume(TokenType.parenDcha, "Se esperaba ')' después de la condición");
            node.addChild(S());
        } else if (match(TokenType.PRvar)) {
            reglasAplicadas.add(37);
            node.addChild(new ASTNode("var"));
            ASTNode tipo = T();
            node.addChild(tipo);
            Token id = consume(TokenType.id, "Se esperaba identificador");
            node.addChild(new ASTNode("id(" + id.lexeme + ")"));
            tablaSimbolos.add(new Symbol(id.lexeme));
            consume(TokenType.puntoComa, "Falta ';'");
        } else if (match(TokenType.PRfor)) {
            reglasAplicadas.add(39);
            consume(TokenType.parenIzq, "Se esperaba '('");
            node.addChild(F1());
            consume(TokenType.puntoComa, "Falta ';'");
            node.addChild(E());
            consume(TokenType.puntoComa, "Falta ';'");
            node.addChild(A());
            consume(TokenType.parenDcha, "Falta ')'");
            consume(TokenType.llaveIzq, "Falta '{'");
            node.addChild(C());
            consume(TokenType.llaveDcha, "Falta '}'");
        } else {
            reglasAplicadas.add(38);
            node.addChild(S());
        }
        return node;
    }

    private ASTNode F() {
        reglasAplicadas.add(48);
        ASTNode node = new ASTNode("F");
        consume(TokenType.PRfun, "Se esperaba 'function'");
        node.addChild(new ASTNode("function"));
        node.addChild(F2());
        node.addChild(F3());
        node.addChild(F4());
        consume(TokenType.llaveIzq, "Falta '{'");
        node.addChild(C());
        node.addChild(F5());
        consume(TokenType.llaveDcha, "Falta '}'");
        return node;
    }

	private ASTNode C() {
		ASTNode node = new ASTNode("C");
		if (check(TokenType.PRif) || check(TokenType.PRvar) || check(TokenType.PRfor) ||
			check(TokenType.id) || check(TokenType.PRoutput) || check(TokenType.PRinput) || check(TokenType.PRreturn)) {
			reglasAplicadas.add(46); // C -> B C
			node.addChild(B());
			node.addChild(C());
		} else {
			reglasAplicadas.add(47); // C -> λ
		}
		return node;
	}
	
    private ASTNode F2() {
        reglasAplicadas.add(49);
        return H();
    }

    private ASTNode F3() {
        reglasAplicadas.add(50);
        ASTNode node = new ASTNode("F3");
        Token id = consume(TokenType.id, "Se esperaba identificador");
        node.addChild(new ASTNode("id(" + id.lexeme + ")"));
        return node;
    }

    private ASTNode F4() {
        reglasAplicadas.add(51);
        ASTNode node = new ASTNode("F4");
        consume(TokenType.parenIzq, "Falta '(' en declaracion de funcion");
        if (check(TokenType.PRint) || check(TokenType.PRboolean) || check(TokenType.PRstring)) {
            node.addChild(Z());
        } else if (match(TokenType.PRvoid)) {
            reglasAplicadas.add(55); // Z -> void
            node.addChild(new ASTNode("void"));
        } else {
            reglasAplicadas.add(57); // Z -> λ
        }
        consume(TokenType.parenDcha, "Falta ')' en declaracion de funcion");
        return node;
    }

    private ASTNode F5() {
        ASTNode node = new ASTNode("F5");
        if (check(TokenType.PRreturn)) {
            reglasAplicadas.add(56); // F5 -> S1
            node.addChild(S1());
        } else {
            reglasAplicadas.add(57); // F5 -> λ
        }
        return node;
    }

    private ASTNode T() {
        if (match(TokenType.PRint)) {
            reglasAplicadas.add(40);
            return new ASTNode("int");
        }
        if (match(TokenType.PRboolean)) {
            reglasAplicadas.add(41);
            return new ASTNode("boolean");
        }
        if (match(TokenType.PRstring)) {
            reglasAplicadas.add(42);
            return new ASTNode("string");
        }
        error(peek(), "Tipo no válido");
        return new ASTNode("tipo_error");
    }

    private ASTNode H() {
        if (check(TokenType.PRint) || check(TokenType.PRboolean) || check(TokenType.PRstring)) {
            reglasAplicadas.add(52);
            return T();
        } else if (match(TokenType.PRvoid)) {
            reglasAplicadas.add(53);
            return new ASTNode("void");
        } else {
            error(peek(), "Se esperaba tipo o void");
            return new ASTNode("tipo_error");
        }
    }

    private ASTNode Z() {
        reglasAplicadas.add(54);
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
            reglasAplicadas.add(57);
        }
        return node;
    }
	private ASTNode E() {
		reglasAplicadas.add(5);
		ASTNode node = new ASTNode("E");
		node.addChild(R());
		node.addChild(E1());
		return node;
	}
	
	private ASTNode E1() {
		ASTNode node = new ASTNode("E1");
		if (match(TokenType.opAnd)) {
			reglasAplicadas.add(6);
			node.addChild(new ASTNode("&&"));
			node.addChild(R());
			node.addChild(E1());
		} else {
			reglasAplicadas.add(7);
		}
		return node;
	}
	
	private ASTNode R() {
		reglasAplicadas.add(8);
		ASTNode node = new ASTNode("R");
		node.addChild(U());
		node.addChild(R1());
		return node;
	}
	
	private ASTNode R1() {
		ASTNode node = new ASTNode("R1");
		if (match(TokenType.opIgual)) {
			reglasAplicadas.add(9);
			node.addChild(new ASTNode("=="));
			node.addChild(U());
			node.addChild(R1());
		} else {
			reglasAplicadas.add(10);
		}
		return node;
	}
	
	private ASTNode U() {
		reglasAplicadas.add(11);
		ASTNode node = new ASTNode("U");
		node.addChild(V());
		node.addChild(U1());
		return node;
	}
	
	private ASTNode U1() {
		ASTNode node = new ASTNode("U1");
		if (match(TokenType.opSuma)) {
			reglasAplicadas.add(12);
			node.addChild(new ASTNode("+"));
			node.addChild(V());
			node.addChild(U1());
		} else {
			reglasAplicadas.add(13);
		}
		return node;
	}
	
	private ASTNode V() {
		ASTNode node = new ASTNode("V");
		if (match(TokenType.id)) {
			reglasAplicadas.add(14);
			node.addChild(new ASTNode("id(" + previous().lexeme + ")"));
			node.addChild(V1());
		} else if (match(TokenType.parenIzq)) {
			reglasAplicadas.add(15);
			node.addChild(new ASTNode("("));
			node.addChild(E());
			consume(TokenType.parenDcha, "Falta ')'");
		} else if (match(TokenType.entero)) {
			reglasAplicadas.add(16);
			node.addChild(new ASTNode("ent"));
		} else if (match(TokenType.cadena)) {
			reglasAplicadas.add(17);
			node.addChild(new ASTNode("cad"));
		} else if (match(TokenType.True)) {
			reglasAplicadas.add(18);
			node.addChild(new ASTNode("true"));
		} else if (match(TokenType.False)) {
			reglasAplicadas.add(19);
			node.addChild(new ASTNode("false"));
		} else {
			error(peek(), "Expresión no válida");
		}
		return node;
	}
	
	private ASTNode V1() {
		ASTNode node = new ASTNode("V1");
		if (match(TokenType.opIncremen)) {
			reglasAplicadas.add(20);
			node.addChild(new ASTNode("++"));
		} else if (match(TokenType.parenIzq)) {
			reglasAplicadas.add(21);
			node.addChild(new ASTNode("("));
			node.addChild(L());
			consume(TokenType.parenDcha, "Falta ')'");
		} else {
			reglasAplicadas.add(22);
		}
		return node;
	}
	
	private ASTNode L() {
		ASTNode node = new ASTNode("L");
		if (check(TokenType.id) || check(TokenType.entero) || check(TokenType.cadena)
			|| check(TokenType.True) || check(TokenType.False) || check(TokenType.parenIzq)) {
			reglasAplicadas.add(23);
			node.addChild(E());
			node.addChild(Q());
		} else {
			reglasAplicadas.add(24);
		}
		return node;
	}
	
	private ASTNode Q() {
		ASTNode node = new ASTNode("Q");
		if (match(TokenType.coma)) {
			reglasAplicadas.add(25);
			node.addChild(new ASTNode(","));
			node.addChild(E());
			node.addChild(Q());
		} else {
			reglasAplicadas.add(26);
		}
		return node;
	}
	
	private ASTNode S() {
		ASTNode node = new ASTNode("S");
		if (check(TokenType.id)) {
			if (tokens.get(current + 1).type == TokenType.parenIzq) {
				reglasAplicadas.add(28);
				Token id = advance();
				node.addChild(new ASTNode("id(" + id.lexeme + ")"));
				consume(TokenType.parenIzq, "Falta '('");
				node.addChild(L());
				consume(TokenType.parenDcha, "Falta ')'");
				consume(TokenType.puntoComa, "Falta ';'");
			} else {
				reglasAplicadas.add(27);
				node.addChild(F1());
				consume(TokenType.puntoComa, "Falta ';'");
			}
		} else if (match(TokenType.PRoutput)) {
			reglasAplicadas.add(29);
			node.addChild(new ASTNode("output"));
			node.addChild(E());
			consume(TokenType.puntoComa, "Falta ';'");
		} else if (match(TokenType.PRinput)) {
			reglasAplicadas.add(30);
			node.addChild(new ASTNode("input"));
			Token id = consume(TokenType.id, "Falta identificador");
			node.addChild(new ASTNode("id(" + id.lexeme + ")"));
			consume(TokenType.puntoComa, "Falta ';'");
		} else if (check(TokenType.PRreturn)) {
			reglasAplicadas.add(31);
			node.addChild(S1());
			consume(TokenType.puntoComa, "Falta ';'");
		} else {
			error(peek(), "Sentencia no válida");
		}
		return node;
	}
	
	private ASTNode S1() {
		ASTNode node = new ASTNode("S1");
		consume(TokenType.PRreturn, "Falta 'return'");
		node.addChild(new ASTNode("return"));
		node.addChild(X());
		return node;
	}
	
	private ASTNode X() {
		ASTNode node = new ASTNode("X");
		if (!check(TokenType.puntoComa)) {
			reglasAplicadas.add(32);
			node.addChild(E());
		} else {
			reglasAplicadas.add(33);
		}
		return node;
	}
	
	private ASTNode F1() {
		ASTNode node = new ASTNode("F1");
		if (match(TokenType.id)) {
			reglasAplicadas.add(34);
			node.addChild(new ASTNode("id(" + previous().lexeme + ")"));
			consume(TokenType.igual, "Falta '='");
			node.addChild(E());
		} else {
			reglasAplicadas.add(35);
		}
		return node;
	}
	
	private ASTNode A() {
		ASTNode node = new ASTNode("A");
		if (check(TokenType.id)) {
			if (tokens.get(current + 1).type == TokenType.igual) {
				reglasAplicadas.add(44);
				node.addChild(F1());
			} else if (tokens.get(current + 1).type == TokenType.opIncremen) {
				reglasAplicadas.add(45);
				Token id = advance();
				node.addChild(new ASTNode("id(" + id.lexeme + ")"));
				advance(); // consume ++
				node.addChild(new ASTNode("++"));
			}
		} else {
			reglasAplicadas.add(43);
		}
		return node;
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
