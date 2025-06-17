package Lexico;
import java.util.List;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final int columnStart;
    public final int columnEnd;

    public Token(TokenType type, String lexeme, int line, int columnStart, int columnEnd) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
    }

    @Override
    public String toString() {
        return "<" + type + ", " + lexeme + "> en línea " + line + ", columnas " + columnStart + "-" + columnEnd;
    }
	public String toCleanFormattedString(List<Symbol> lista) {
		if (type == TokenType.finFich) return "";
	
		String attr = "";
		if (type == TokenType.entero ) {
			attr = lexeme;
		} else if (type == TokenType.cadena) {
			attr = "\"" + lexeme + "\"";
		}else if (type == TokenType.id){
			attr = getSymbolIndex(lexeme, lista);
		}

		return "<" + type + (attr.isEmpty() ? ", >" : ", " + attr + ">");
	}

    private String getSymbolIndex(String lexeme, List<Symbol> lista) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getLexema().equals(lexeme)) {
                return String.valueOf(i + 1);
            }
        }
        return "0"; // Not found
    }
	
}
