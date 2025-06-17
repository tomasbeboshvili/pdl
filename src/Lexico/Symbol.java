package Lexico;

public class Symbol {

    private String lexema;
    private String Tipo;
    private int inf;
    private int sup;
    private int nParametros;
    private String[] tipoParametros;
    private String tipoDevuelto;

    public Symbol(String lexema) {
        this.lexema = lexema;
        this.Tipo = "";
        this.inf = 0;
        this.sup = 0;
        this.nParametros = 0;
        this.tipoParametros = new String[0];
        this.tipoDevuelto = "";
    }

    public String getLexema() { return lexema; }
    public String getTipo() { return Tipo; }
    public int getInf() { return inf; }
    public int getSup() { return sup; }
    public int getnParametros() { return nParametros; }
    public String[] getTipoParametros() { return tipoParametros; }
    public String getTipoDevuelto() { return tipoDevuelto; }

    public void setLexema(String lexema) { this.lexema = lexema; }
    public void setTipo(String tipo) { this.Tipo = tipo; }
    public void setInf(int inf) { this.inf = inf; }
    public void setSup(int sup) { this.sup = sup; }
    public void setnParametros(int nParametros) { this.nParametros = nParametros; }
    public void setTipoParametros(String[] tipoParametros) { this.tipoParametros = tipoParametros; }
    public void setTipoDevuelto(String tipoDevuelto) { this.tipoDevuelto = tipoDevuelto; }

    @Override
    public String toString() {
        return "Symbol{" +
                "lexema='" + lexema + '\'' +
                ", Tipo='" + Tipo + '\'' +
                ", inf=" + inf +
                ", sup=" + sup +
                ", nParametros=" + nParametros +
                ", tipoParametros=" + String.join(", ", tipoParametros) +
                ", tipoDevuelto='" + tipoDevuelto + '\'' +
                '}';
    }
}
