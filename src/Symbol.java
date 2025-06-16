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
    }
    public String getLexema() {
        return lexema;
    }
    public String getTipo() {
        return Tipo;    
    }
    public int getInf() {
        return inf;
    }
    public int getSup() {
        return sup;
    }   
    public int getnParametros() {
        return nParametros;
    }
    public String[] getTipoParametros() {
        return tipoParametros;
    }
    public String getTipoDevuelto() {
        return tipoDevuelto;
    }

    

}
