package Sintactico;
import java.util.*;

import Lexico.Token;
import Lexico.TokenType;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final StringBuilder errores = new StringBuilder();
    private boolean hayErrores = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean parse() {
        try {
            P(); // punto de entrada
            Token t = peek();
            if (t.type != TokenType.finFich) {
                error(t, "Se esperaban más sentencias o fin de fichero");
            }
        } catch (Exception e) {
            hayErrores = true;
        }

        return !hayErrores;
    }

    public String getErrores() {
        return errores.toString();
    }

	private void F() {
		consume(TokenType.PRfun, "Se esperaba 'function'");
		F2(); // tipo devuelto
		consume(TokenType.id, "Se esperaba nombre de función");
		consume(TokenType.parenIzq, "Falta '(' en cabecera de función");
		if (!check(TokenType.parenDcha)) {
			Z(); // parámetros
		}
		consume(TokenType.parenDcha, "Falta ')' en cabecera de función");
		consume(TokenType.llaveIzq, "Falta '{' en cuerpo de función");
		C(); // cuerpo
		consume(TokenType.llaveDcha, "Falta '}' al final de función");
	}

	private void F2() {
		if (!(match(TokenType.PRint) || match(TokenType.PRboolean) ||
			  match(TokenType.PRstring) || match(TokenType.PRvoid))) {
			error(peek(), "Tipo de retorno no válido");
		}
	}

	private void Z() {
		T();
		consume(TokenType.id, "Falta identificador de parámetro");
		while (match(TokenType.coma)) {
			T();
			consume(TokenType.id, "Falta identificador de parámetro");
		}
	}

	private void X() {
		if (!check(TokenType.puntoComa)) {
			E(); // hay una expresión antes de ;
		}
	}
	

    // ------------------ P -> B P | F P | λ ------------------
    private void P() {
        while (match(TokenType.PRvar, TokenType.PRif, TokenType.id, TokenType.PRfor, TokenType.PRoutput, TokenType.PRfun)) {
            if (check(TokenType.PRvar) || check(TokenType.PRif) || check(TokenType.id) || check(TokenType.PRfor) || check(TokenType.PRoutput)) {
                B();
            } else if (check(TokenType.PRfun)) {
                F();
            }
        }
    }

    // ------------------ B -> if ( E ) S | var T id ; | S | for (...) { C } ------------------
    private void B() {
        if (match(TokenType.PRif)) {
            consume(TokenType.parenIzq, "Se esperaba '(' después de 'if'");
            E();
            consume(TokenType.parenDcha, "Se esperaba ')' después de la condición");
            S();
        } else if (match(TokenType.PRvar)) {
            T();
            consume(TokenType.id, "Se esperaba un identificador");
            consume(TokenType.puntoComa, "Falta ';' después de declaración");
        } else if (check(TokenType.id) || check(TokenType.PRoutput) || check(TokenType.PRinput) || check(TokenType.PRreturn)) {
            S();
        } else if (match(TokenType.PRfor)) {
            consume(TokenType.parenIzq, "Falta '(' después de 'for'");
            F1();
            consume(TokenType.puntoComa, "Falta ';' después de inicialización");
            E();
            consume(TokenType.puntoComa, "Falta ';' después de condición");
            A();
            consume(TokenType.parenDcha, "Falta ')' en la cabecera del 'for'");
            consume(TokenType.llaveIzq, "Falta '{' en el 'for'");
            C();
            consume(TokenType.llaveDcha, "Falta '}' al final del 'for'");
        } else {
            error(peek(), "Sentencia no válida");
            advance(); // intentar continuar
        }
    }

    // ------------------ T -> int | boolean | string ------------------
    private void T() {
        if (!(match(TokenType.PRint) || match(TokenType.PRboolean) || match(TokenType.PRstring))) {
            error(peek(), "Tipo no válido, se esperaba int, boolean o string");
        }
    }

    // ------------------ S -> id = E ; | output E ; | input id ; | return X ; ------------------
    private void S() {
        if (match(TokenType.id)) {
            if (match(TokenType.igual)) {
                E();
                consume(TokenType.puntoComa, "Falta ';' después de la asignación");
            } else {
                error(previous(), "Se esperaba '=' después del identificador");
            }
        } else if (match(TokenType.PRoutput)) {
            E();
            consume(TokenType.puntoComa, "Falta ';' después de output");
        } else if (match(TokenType.PRinput)) {
            consume(TokenType.id, "Se esperaba un identificador en input");
            consume(TokenType.puntoComa, "Falta ';' después de input");
        } else if (match(TokenType.PRreturn)) {
            if (!check(TokenType.puntoComa)) {
                X();
            }
            consume(TokenType.puntoComa, "Falta ';' después de return");
        } else {
            error(peek(), "Sentencia no válida");
            advance();
        }
    }

    // ------------------ E, R, U, V (expresiones lógicas y aritméticas) ------------------
    private void E() {
        R();
        while (match(TokenType.opAnd)) R();
    }

    private void R() {
        U();
        while (match(TokenType.opIgual)) U();
    }

    private void U() {
        V();
        while (match(TokenType.opSuma)) V();
    }

    private void V() {
        if (match(TokenType.id)) {
            if (match(TokenType.opIncremen)) return;
            if (match(TokenType.parenIzq)) {
                if (!check(TokenType.parenDcha)) L();
                consume(TokenType.parenDcha, "Falta ')' en llamada a función");
            }
        } else if (match(TokenType.entero) || match(TokenType.cadena) || match(TokenType.True) || match(TokenType.False)) {
            return;
        } else if (match(TokenType.parenIzq)) {
            E();
            consume(TokenType.parenDcha, "Falta ')' en expresión");
        } else {
            error(peek(), "Expresión no válida");
            advance();
        }
    }

    private void L() {
        E();
        while (match(TokenType.coma)) {
            E();
        }
    }

    private void F1() {
        if (match(TokenType.id)) {
            if (match(TokenType.igual)) {
                E();
            } else {
                error(peek(), "Se esperaba '=' después del identificador");
            }
        }
    }

    private void A() {
        if (match(TokenType.id)) {
            if (match(TokenType.opIncremen)) return;
            else if (match(TokenType.igual)) {
                E();
            } else {
                error(peek(), "Se esperaba '++' o '=' en actualización");
            }
        }
    }

    private void C() {
        while (!check(TokenType.llaveDcha) && !isAtEnd()) {
            B();
        }
    }

    // ------------------ Funciones de utilidades ------------------
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

    private void consume(TokenType type, String message) {
        if (check(type)) {
            advance();
            return;
        }
        error(peek(), message);
    }

    private void error(Token token, String message) {
        hayErrores = true;
        errores.append("[ERROR - Línea ").append(token.line).append("]: ").append(message).append("\n");
    }
}
