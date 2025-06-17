package Lexico;
public enum TokenType {
    // Palabras reservadas
    PRboolean, PRfor, PRfun, PRif, PRinput, PRint, PRoutput, PRreturn, PRstring, PRvar, PRvoid,
    // Operadores
    opIncremen, opSuma, opAnd, opIgual, igual,
    // Delimitadores y símbolos
    coma, puntoComa, parenIzq, parenDcha, llaveIzq, llaveDcha,
    // Constantes
    entero, cadena, True, False,
    // Identificador
    id,
    // Fin de fichero
    finFich
}


