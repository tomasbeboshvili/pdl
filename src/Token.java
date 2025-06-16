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
	public String toCleanFormattedString(List<String> lista) {
		if (type == TokenType.finFich) return "";
	
		String attr = "";
		if (type == TokenType.entero ) {
			attr = lexeme;
		} else if (type == TokenType.cadena) {
			attr = "\"" + lexeme + "\"";
		}else if (type == TokenType.id){
			attr = "" + (lista.indexOf(lexeme) + 1);
		}

		return "<" + type + (attr.isEmpty() ? ", >" : ", " + attr + ">");
	}

	
}
