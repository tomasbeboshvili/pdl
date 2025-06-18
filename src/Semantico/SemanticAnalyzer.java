
package Semantico;

import Sintactico.ASTNode;
import Lexico.Symbol;

import java.util.*;

public class SemanticAnalyzer {

    private final Deque<Map<String, Symbol>> tablas = new ArrayDeque<>();
    private final List<String> errores = new ArrayList<>();
    private boolean zonaDecl = true;
    private int despl = 0;
    private int cont = 0;
    private String tipoRetornoActual = "";

    public void analizar(ASTNode raiz) {
        creaTabla();
        recorrer(raiz);
        liberaTabla();
    }

    private void recorrer(ASTNode nodo) {
        String etiqueta = nodo.getLabel();
        List<ASTNode> hijos = nodo.getChildren();

        switch (etiqueta) {
            case "P1":
			zonaDecl = true;
			despl = 0;
			cont = 0;
			creaTabla();
			for (ASTNode hijo : hijos) {
				recorrer(hijo);  // puede ser múltiples P
			}
			zonaDecl = false;
			liberaTabla();
			break;
		
		case "P":
			if (hijos.isEmpty()) {
				// λ: no hay más producción
				break;
			}
		
			ASTNode primer = hijos.get(0);
			if (primer.getLabel().equals("B") || primer.getLabel().equals("F")) {
				recorrer(primer);
				if (hijos.size() > 1) {
					recorrer(hijos.get(1));  // siguiente P
				}
			} else {
				error("Producción inválida en P.");
			}
			break;
		

            case "B":
                if (hijos.size() == 4 && hijos.get(0).getLabel().equals("var")) {
                    String tipo = evaluarTipo(hijos.get(1));
                    String id = hijos.get(2).getLabel();
                    if (zonaDecl) {
                        if (existeEnAmbito(id)) {
                            error("Variable ya declarada: " + id);
                        } else {
                            Symbol sym = new Symbol(id);
                            sym.setTipo(tipo);
                            sym.setInf(despl);
                            tablas.peek().put(id, sym);
                            despl += 1;
                        }
                    } else {
                        error("Declaración fuera de zona de declaración: " + id);
                    }
                } else if (hijos.get(0).getLabel().equals("if")) {
                    String tipoE = evaluarTipo(hijos.get(2));
                    if (!tipoE.equals("bool")) {
                        error("Condición en 'if' debe ser de tipo bool.");
                    }
                    recorrer(hijos.get(4));
                } else if (hijos.get(0).getLabel().equals("for")) {
                    creaTabla();
                    recorrer(hijos.get(2)); // F1
                    String tipoE = evaluarTipo(hijos.get(4));
                    if (!tipoE.equals("bool")) {
                        error("Condición en 'for' debe ser bool.");
                    }
                    recorrer(hijos.get(6)); // A
                    recorrer(hijos.get(9)); // C
                    liberaTabla();
                } else {
                    recorrer(hijos.get(0));
                }
                break;

            case "F":
                zonaDecl = true;
                creaTabla();
                String tipoDevuelto = evaluarTipo(hijos.get(1));
                String nombreFuncion = hijos.get(2).getLabel();
                List<String> tiposParametros = evaluarParametros(hijos.get(4));
                tipoRetornoActual = tipoDevuelto;

                if (existeEnAmbito(nombreFuncion)) {
                    error("Función ya declarada: " + nombreFuncion);
                } else {
                    Symbol fun = new Symbol(nombreFuncion);
                    fun.setTipo("fun");
                    fun.setTipoDevuelto(tipoDevuelto);
                    fun.setTipoParametros(tiposParametros.toArray(new String[0]));
                    fun.setnParametros(tiposParametros.size());
                    tablas.peek().put(nombreFuncion, fun);
                }

                recorrer(hijos.get(6)); // C
                recorrer(hijos.get(7)); // F5
                liberaTabla();
                zonaDecl = false;
                break;

            case "S1":
                String tipoX = evaluarTipo(hijos.get(1));
                if (!tipoX.equals(tipoRetornoActual)) {
                    error("Tipo de retorno incorrecto. Esperado: " + tipoRetornoActual + ", pero se obtuvo: " + tipoX);
                }
                break;

            case "S":
                if (hijos.size() >= 3 && hijos.get(1).getLabel().equals("=")) {
                    String id = hijos.get(0).getLabel();
                    String tipoId = buscaTipo(id);
                    String tipoE = evaluarTipo(hijos.get(2));
                    if (!tipoId.equals(tipoE)) {
                        error("Asignación inválida a " + id + ": tipo " + tipoId + " vs " + tipoE);
                    }
                } else if (hijos.size() >= 4 && hijos.get(1).getLabel().equals("(")) {
                    String id = hijos.get(0).getLabel();
                    Symbol fun = buscaSimbolo(id);
                    if (fun == null || !"fun".equals(fun.getTipo())) {
                        error("Llamada a función no declarada: " + id);
                    } else {
                        List<String> tipos = evaluarArgumentos(hijos.get(2));
                        if (fun.getnParametros() != tipos.size()) {
                            error("Número incorrecto de parámetros en " + id);
                        } else {
                            for (int i = 0; i < tipos.size(); i++) {
                                if (!tipos.get(i).equals(fun.getTipoParametros()[i])) {
                                    error("Tipo incorrecto en parámetro " + (i + 1) + " de " + id);
                                }
                            }
                        }
                    }
                } else {
                    recorrer(hijos.get(0));
                }
                break;

				case "E": {
					String tipoR = evaluarTipo(hijos.get(0));
					String tipoE1 = evaluarTipo(hijos.get(1));
					if (!tipoR.equals("bool")) {
						error("E: se esperaba tipo bool en R.");
					}
					break;
				}
	
				case "E1":
					if (hijos.size() == 3) {
						String tipoR = evaluarTipo(hijos.get(1));
						String tipoE1 = evaluarTipo(hijos.get(2));
						if (!tipoR.equals("bool") || !tipoE1.equals("bool")) {
							error("E1: tipos incompatibles en operación &&.");
						}
					}
					break;
	
				case "R": {
					String tipoU = evaluarTipo(hijos.get(0));
					String tipoR1 = evaluarTipo(hijos.get(1));
					if (!tipoU.equals("bool") && !tipoR1.equals("void")) {
						error("R: tipo no bool.");
					}
					break;
				}
	
				case "R1":
					if (hijos.size() == 3) {
						String tipoU = evaluarTipo(hijos.get(1));
						String tipoR1 = evaluarTipo(hijos.get(2));
						if (!tipoU.equals("bool") || !tipoR1.equals("bool")) {
							error("R1: tipos incompatibles en comparación ==");
						}
					}
					break;
	
				case "U": {
					String tipoV = evaluarTipo(hijos.get(0));
					String tipoU1 = evaluarTipo(hijos.get(1));
					break;
				}
	
				case "U1":
					if (hijos.size() == 3) {
						String tipoV = evaluarTipo(hijos.get(1));
						String tipoU1 = evaluarTipo(hijos.get(2));
						if (!tipoV.equals("int") || !tipoU1.equals("int")) {
							error("U1: suma requiere tipos int.");
						}
					}
					break;
	
				case "V":
					if (hijos.size() == 1) {
						String terminal = hijos.get(0).getLabel();
						if (terminal.equals("id")) {
							String tipo = buscaTipo(terminal);
							if (tipo.equals("tipo_error")) {
								error("V: id no declarado.");
							}
						}
					} else if (hijos.size() == 2 && hijos.get(1).getLabel().equals("++")) {
						String id = hijos.get(0).getLabel();
						String tipo = buscaTipo(id);
						if (!tipo.equals("int")) {
							error("V: 'id++' solo permitido sobre enteros.");
						}
					} else if (hijos.size() == 4 && hijos.get(1).getLabel().equals("(")) {
						String id = hijos.get(0).getLabel();
						Symbol fun = buscaSimbolo(id);
						if (fun == null || !fun.getTipo().equals("fun")) {
							error("V: llamada a función no declarada: " + id);
						} else {
							List<String> args = evaluarArgumentos(hijos.get(2));
							if (fun.getnParametros() != args.size()) {
								error("V: número de argumentos incorrecto.");
							} else {
								for (int i = 0; i < args.size(); i++) {
									if (!args.get(i).equals(fun.getTipoParametros()[i])) {
										error("V: tipo de parámetro " + (i + 1) + " incorrecto.");
									}
								}
							}
						}
					}
					break;
	
				case "L":
					evaluarTipo(hijos.get(0));
					if (hijos.size() > 1) evaluarTipo(hijos.get(1));
					break;
	
				case "Q":
					if (hijos.size() == 3) {
						evaluarTipo(hijos.get(1));
						evaluarTipo(hijos.get(2));
					}
					break;
	
				case "X":
					if (!hijos.isEmpty()) {
						evaluarTipo(hijos.get(0));
					}
					break;
	
				case "F1":
					if (!hijos.isEmpty()) {
						if (hijos.size() >= 3 && hijos.get(1).getLabel().equals("=")) {
							String id = hijos.get(0).getLabel();
							String tipoId = buscaTipo(id);
							String tipoE = evaluarTipo(hijos.get(2));
							if (!tipoId.equals(tipoE)) {
								error("F1: asignación incompatible en for.");
							}
						}
					}
					break;
	
				case "A":
					if (hijos.size() == 1 && hijos.get(0).getLabel().equals("id++")) {
						String id = hijos.get(0).getLabel();
						String tipo = buscaTipo(id);
						if (!"int".equals(tipo)) {
							error("A: sólo se puede incrementar enteros.");
						}
					} else if (!hijos.isEmpty()) {
						recorrer(hijos.get(0));
					}
					break;
	
			default:
                for (ASTNode hijo : hijos) recorrer(hijo);
                break;
        }
    }

    private String evaluarTipo(ASTNode nodo) {
        switch (nodo.getLabel()) {
            case "int": return "int";
            case "boolean": return "bool";
            case "string": return "string";
            case "void": return "void";
            default:
                recorrer(nodo);
                return "tipo_desconocido";
        }
    }

    private List<String> evaluarParametros(ASTNode nodoZ) {
        List<String> tipos = new ArrayList<>();
        for (ASTNode hijo : nodoZ.getChildren()) {
            if (hijo.getLabel().equals("T")) {
                tipos.add(evaluarTipo(hijo));
                cont++;
            }
        }
        return tipos;
    }

    private List<String> evaluarArgumentos(ASTNode nodoL) {
        List<String> tipos = new ArrayList<>();
        for (ASTNode hijo : nodoL.getChildren()) {
            tipos.add(evaluarTipo(hijo));
        }
        return tipos;
    }

    private void creaTabla() {
        tablas.push(new HashMap<>());
    }

    private void liberaTabla() {
        if (!tablas.isEmpty()) tablas.pop();
    }

    private boolean existeEnAmbito(String id) {
        return tablas.peek().containsKey(id);
    }

    private Symbol buscaSimbolo(String id) {
        for (Map<String, Symbol> ambito : tablas) {
            if (ambito.containsKey(id)) return ambito.get(id);
        }
        return null;
    }

    private String buscaTipo(String id) {
        Symbol s = buscaSimbolo(id);
        return s != null ? s.getTipo() : "tipo_error";
    }

    private void error(String msg) {
        errores.add("Error semántico: " + msg);
    }

    public void imprimirErrores() {
        if (errores.isEmpty()) {
            System.out.println("Análisis semántico completado sin errores.");
        } else {
            for (String err : errores) {
                System.out.println(err);
            }
        }
    }
	public List<String> getErrores() {
        return errores;
	}
}
