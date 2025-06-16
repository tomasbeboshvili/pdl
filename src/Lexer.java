import java.util.*;
import java.util.regex.*;

public class Lexer {
    private final String input;
    private final List<Token> tokens = new ArrayList<>();
    private int pos = 0;
    private int line = 1;
	private int column = 1;
	private int tokenStartColumn = 1;
	private List<Symbol> SymbolTable = new ArrayList<>();

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("boolean", TokenType.PRboolean);
        keywords.put("for", TokenType.PRfor);
        keywords.put("function", TokenType.PRfun);
        keywords.put("if", TokenType.PRif);
        keywords.put("input", TokenType.PRinput);
        keywords.put("int", TokenType.PRint);
        keywords.put("output", TokenType.PRoutput);
        keywords.put("return", TokenType.PRreturn);
        keywords.put("string", TokenType.PRstring);
        keywords.put("var", TokenType.PRvar);
        keywords.put("void", TokenType.PRvoid);
        keywords.put("true", TokenType.True);
        keywords.put("false", TokenType.False);
    }

    public Lexer(String input) {
        this.input = input;
    }

	public List<Symbol> getSymbolTable() {
		return SymbolTable;
	}

    public List<Token> tokenize() {
        while (pos < input.length()) {
            char current = peek();

            if (current == '\n') {
                line++;
				column = 1;
                advance();
            } else if (Character.isWhitespace(current)) {
                advance();
            } else if (Character.isLetter(current)) {
				tokenStartColumn = column;
                lexIdentifierOrKeyword();
            } else if (Character.isDigit(current)) {
				tokenStartColumn = column;
                lexNumber();
            } else {
                switch (current) {
                    case '+':
						tokenStartColumn = column;
                        advance();
                        if (match('+')) {
                            addToken(TokenType.opIncremen, "++");
                        } else {
                            addToken(TokenType.opSuma, "+");
                        }
                        break;
                    case '=':
                        advance();
                        if (match('=')) {
                            addToken(TokenType.opIgual, "==");
                        } else {
                            addToken(TokenType.igual, "=");
                        }
                        break;
                    case '&':
                        advance();
                        if (match('&')) {
                            addToken(TokenType.opAnd, "&&");
                        } else {
                            error("Símbolo no permitido '&'");
                        }
                        break;
                    case '!':
                        advance();
                        if (match('=')) {
                            error("Operador no permitido '!='");
                        } else {
                            error("Símbolo no permitido '!'");
                        }
                        break;
                    case '/':
                        if (peek(1) == '*') {
                            skipComment();
                        } else if (peek(1) == '=') {
                            error("Operador no permitido '/='");
                            advance();
                            advance();
                        } else {
                            advance(); // podrías reconocer '/' si fuese válido
                            error("Símbolo no permitido '/'");
                        }
                        break;
                    case ',':
                        advance(); addToken(TokenType.coma, ","); break;
                    case ';':
                        advance(); addToken(TokenType.puntoComa, ";"); break;
                    case '(':
                        advance(); addToken(TokenType.parenIzq, "("); break;
                    case ')':
                        advance(); addToken(TokenType.parenDcha, ")"); break;
                    case '{':
                        advance(); addToken(TokenType.llaveIzq, "{"); break;
                    case '}':
                        advance(); addToken(TokenType.llaveDcha, "}"); break;
                    case '\'':
                        lexString();
                        break;
                    default:
                        error("Símbolo no reconocido: '" + current + "'");
                        advance();
                        break;
                }
            }
        }

        tokens.add(new Token(TokenType.finFich, "", line, column, column));
        return tokens;
    }

    private void lexIdentifierOrKeyword() {
        int start = pos;
        while (pos < input.length() && 
               (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }
        String lexeme = input.substring(start, pos);
        TokenType type = keywords.getOrDefault(lexeme, TokenType.id);
        addToken(type, lexeme);
		if (type == TokenType.id && !findSymbol(lexeme)) {
			SymbolTable.add(new Symbol(lexeme)); // Agregar a la tabla de símbolos si es un identificador nuevo
		}
    }

    private boolean findSymbol(String lexeme) {
        for (Symbol symbol : SymbolTable) {
            if (symbol.getLexema().equals(lexeme)) {
                return true;
            }
        }
        return false;
    }

    private void lexNumber() {
        int start = pos;
        while (pos < input.length() && Character.isDigit(peek())) {
            advance();
        }
        String lexeme = input.substring(start, pos);
        try {
            int value = Integer.parseInt(lexeme);
            if (value > 32767) {
                error("Número demasiado grande: " + lexeme);
            } else {
                addToken(TokenType.entero, lexeme);
            }
        } catch (NumberFormatException e) {
            error("Número inválido: " + lexeme);
        }
    }

    private void lexString() {
        advance(); // skip opening '
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && peek() != '\'') {
            if (peek() == '\n') line++;
            sb.append(peek());
            advance();
        }
        if (peek() == '\'') {
            advance();
            addToken(TokenType.cadena, sb.toString());
        } else {
            error("Cadena no cerrada");
        }
    }

    private void skipComment() {
        advance(); // skip /
        advance(); // skip *
        while (pos < input.length()) {
            if (peek() == '*' && peek(1) == '/') {
                advance(); advance(); break;
            }
            if (peek() == '\n') line++;
            advance();
        }
    }

    private void addToken(TokenType type, String lexeme) {
		int endColumn = tokenStartColumn + lexeme.length() - 1;
		tokens.add(new Token(type, lexeme, line, tokenStartColumn, endColumn));
	}

    private void error(String message) {
        System.err.println("[ERROR - Línea " + line + "]: " + message);
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    private char peek(int ahead) {
        return (pos + ahead) < input.length() ? input.charAt(pos + ahead) : '\0';
    }

    private void advance() {
        pos++;
		column++;
    }

    private boolean match(char expected) {
        if (peek() == expected) {
            advance();
            return true;
        }
        return false;
    }
}
