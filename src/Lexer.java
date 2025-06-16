import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos = 0;
    private final int length;

    public Lexer(String input) {
        this.input = input;
        this.length = input.length();
    }

    private char peek() {
        return pos < length ? input.charAt(pos) : '\0';
    }

    private char advance() {
        return pos < length ? input.charAt(pos++) : '\0';
    }

    private boolean match(char expected) {
        if (peek() == expected) {
            advance();
            return true;
        }
        return false;
    }

    private boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    public List<Token> scanTokens() {
        List<Token> tokens = new ArrayList<>();

        while (pos < length) {
            char current = advance();
            switch (current) {
                case ' ': case '\t': case '\n': case '\r': break; // ignorar espacios
                case '+':
                    if (match('+')) tokens.add(new Token(TokenType.opIncremen, null));
                    else tokens.add(new Token(TokenType.opSuma, null));
                    break;
                case '=':
                    if (match('=')) tokens.add(new Token(TokenType.opIgual, null));
                    else tokens.add(new Token(TokenType.igual, null));
                    break;
                case '&':
                    if (match('&')) tokens.add(new Token(TokenType.opAnd, null));
                    else throw new RuntimeException("Error: Símbolo no permitido '&'");
                    break;
                case '(': tokens.add(new Token(TokenType.parenIzq, null)); break;
                case ')': tokens.add(new Token(TokenType.parenDcha, null)); break;
                case '{': tokens.add(new Token(TokenType.llaveIzq, null)); break;
                case '}': tokens.add(new Token(TokenType.llaveDcha, null)); break;
                case ',': tokens.add(new Token(TokenType.coma, null)); break;
                case ';': tokens.add(new Token(TokenType.puntoComa, null)); break;
                case '/':
                    if (match('*')) skipComment();
                    else if (match('=')) throw new RuntimeException("Error: Operador '/=' no permitido");
                    else tokens.add(new Token(TokenType.id, "/")); // fallback
                    break;
                case '\'':
                    tokens.add(new Token(TokenType.cadena, readString()));
                    break;
                default:
                    if (isDigit(current)) {
                        tokens.add(new Token(TokenType.entero, readNumber(current)));
                    } else if (isLetter(current)) {
                        tokens.add(identifierOrKeyword(current));
                    } else {
                        throw new RuntimeException("Error: Símbolo no permitido '" + current + "'");
                    }
            }
        }
        tokens.add(new Token(TokenType.finFich, null));
        return tokens;
    }

    private String readNumber(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (isDigit(peek())) sb.append(advance());
        return sb.toString();
    }

    private Token identifierOrKeyword(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (isLetter(peek()) || isDigit(peek()) || peek() == '_') {
            sb.append(advance());
        }
        String word = sb.toString();
        switch (word) {
            case "boolean": return new Token(TokenType.PRboolean, null);
            case "for": return new Token(TokenType.PRfor, null);
            case "function": return new Token(TokenType.PRfun, null);
            case "if": return new Token(TokenType.PRif, null);
            case "input": return new Token(TokenType.PRinput, null);
            case "int": return new Token(TokenType.PRint, null);
            case "output": return new Token(TokenType.PRoutput, null);
            case "return": return new Token(TokenType.PRreturn, null);
            case "string": return new Token(TokenType.PRstring, null);
            case "var": return new Token(TokenType.PRvar, null);
            case "void": return new Token(TokenType.PRvoid, null);
            case "true": return new Token(TokenType.trueLit, null);
            case "false": return new Token(TokenType.falseLit, null);
            default: return new Token(TokenType.id, word);
        }
    }

    private String readString() {
        StringBuilder sb = new StringBuilder();
        while (peek() != '\'' && peek() != '\0') {
            sb.append(advance());
        }
        if (peek() == '\'') advance(); // cerrar comilla
        return sb.toString();
    }

    private void skipComment() {
        while (pos < length - 1) {
            if (peek() == '*' && input.charAt(pos + 1) == '/') {
                pos += 2;
                return;
            }
            advance();
        }
        throw new RuntimeException("Error: Comentario no cerrado");
    }
}
