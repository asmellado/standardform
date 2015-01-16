package es.vegamultimedia.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VmStr {

    /**
     * Trimea por la derecha una cadena, los caracteres trimeados son aquellos que
     * estén en el segundo parámetro.
     * @param str
     * @param chars
     * @return
     * @author antonio.vera
     */
    //# VmStr
    public static String rtrim(String str, String chars) {
        int ii;
        ii = str.length();
        while(ii>0) {
            if(!chars.contains(str.substring(ii-1, ii))) {
                return str.substring(0, ii);
            }
            ii--;
        }
        return "";
    }
    
    /**
     * Trimea por la izquierda una cadena, los caracteres trimeados son aquellos que
     * estén en el segundo parámetro.
     * @param str
     * @param chars
     * @return
     * @author antonio.vera
     */
    //# VmStr
    public static String ltrim(String str, String chars) {
        int ii;
        ii = str.length();
        for(int i=0; i<ii; i++) {
            if(!chars.contains(str.substring(i, i+1))) {
                return (i==0) ? str : str.substring(i);
            }
        }
        return "";
    }
    
    /**
     * Trimea por la izquierda y por la derecha una cadena, los caracteres trimeados
     * son aquellos que estén en el segundo parámetro.
     * @param str
     * @param chars
     * @return
     * @author antonio.vera
     */
    //# VmStr
    public static String trim(String str, String chars) {
        return ltrim(rtrim(str, chars), chars);
    }

    /**
     * Hace el trabajo duro de objToStr. 
     * 
     * @param o El objeto que hay que expresar como cadena. 
     * @param sb El StringBuilder en el que construir la cadena.
     * @param isKey true si el objeto es la clave de un Map.
     * @param tab Una cadena con espacios, que se usará como tabulador.
     * @param html true si el resultado debe estar en HTML.
     * @author antonio.vera
     */
    //# VmStr
    private static void _objToStr(Object o,
                                  StringBuilder sb,
                                  boolean isKey,
                                  String tab,
                                  boolean html) {
        if(o==null) {
            sb.append("null");
        } else if(o instanceof Boolean) {
            sb.append( (((Boolean)o).booleanValue()) ? "true" : "false" );
        } else if(o instanceof CharSequence) {
            CharSequence cs = (CharSequence)o;
            boolean f = false;
            if(cs.length()>100) {
                sb.append("\"");
                if(html) {
                    sb.append(VmHtml.code(cs.subSequence(0, 100)));
                } else {
                    sb.append(cs.subSequence(0, 100));
                }
                sb.append("\" + ...");
            } else {
                String str = cs.toString();
                if(isKey && str.matches("\\A[a-zA-Z_][a-zA-Z0-9_]*\\z")) {
                    f = true;
                    if(str.equals("null") || str.equals("true") || str.equals("false")) {
                        f = false;
                    }
                }
                if(html) {
                    str = VmHtml.code(str);
                }
                if(f) {
                    sb.append(str);
                } else {
                    sb.append("\"").append(str).append("\"");
                }
            }
        } else if(o instanceof Number) {
            sb.append(o.toString());
        } else if(isKey) {
            String str = o.toString();
            if(html) {
                sb.append("&lt;").append(VmHtml.code(str)).append("&gt;");
            } else {
                sb.append("<").append(str).append(">");
            }
        } else if(o instanceof byte[]) {
            String coma = "";
            byte[] arr = (byte[]) o;
            sb.append("[");
            for(byte i: arr) {
                sb.append(coma).append(i);
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof short[]) {
            String coma = "";
            short[] arr = (short[]) o;
            sb.append("[");
            for(short i: arr) {
                sb.append(coma).append(i);
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof int[]) {
            String coma = "";
            int[] arr = (int[]) o;
            sb.append("[");
            for(int i: arr) {
                sb.append(coma).append(i);
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof long[]) {
            String coma = "";
            long[] arr = (long[]) o;
            sb.append("[");
            for(long i: arr) {
                sb.append(coma).append(i);
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof float[]) {
            String coma = "";
            float[] arr = (float[]) o;
            sb.append("[");
            for(float i: arr) {
                sb.append(coma).append(i);
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof double[]) {
            String coma = "";
            double[] arr = (double[]) o;
            sb.append("[");
            for(double i: arr) {
                sb.append(coma).append(i);
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof boolean[]) {
            String coma = "";
            boolean[] arr = (boolean[]) o;
            sb.append("[");
            for(boolean i: arr) {
                sb.append(coma).append((i)?"true":"false");
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof char[]) {
            String coma = "";
            String str;
            char[] arr = (char[]) o;
            sb.append("[");
            for(char i: arr) {
                str = String.valueOf(i);
                sb.append(coma).append("'");
                sb.append((html)?VmHtml.code(str):str);
                sb.append("'");
                coma = ", ";
            }
            sb.append("]");
        } else if(o instanceof Object[]) {
            String tab2 = tab + "  ";
            String coma = "\n" + tab2;
            Object[] arr = (Object[]) o;
            sb.append("[");
            for(Object i: arr) {
                sb.append(coma);
                _objToStr(i, sb, false, tab, html);
                coma = ",\n" + tab2;
            }
            sb.append("\n").append(tab).append("]");
        } else if(o instanceof Iterable) {
            String tab2 = tab + "  ";
            String coma = "\n" + tab2;
            Iterable<?> arr = (Iterable<?>) o;
            sb.append("[");
            for(Object i: arr) {
                sb.append(coma);
                _objToStr(i, sb, false, tab2, html);
                coma = ",\n" + tab2;
            }
            sb.append("\n").append(tab).append("]");
        } else if(o instanceof Map) {
            String tab2 = tab + "  ";
            String coma = "\n" + tab2;
            Map<?, ?> arr = (Map<?, ?>) o;
            sb.append("{");
            for(Entry<?, ?>i: arr.entrySet()) {
                sb.append(coma);
                if(html) sb.append("<b>");
                _objToStr(i.getKey(), sb, true, tab2, html);
                if(html) sb.append("</b>");
                sb.append(": ");
                _objToStr(i.getValue(), sb, false, tab2, html);
                coma = ",\n" + tab2;
            }
            sb.append("\n").append(tab).append("}");
        } else {
            String str = o.toString();
            if(html) {
                sb.append("&lt;").append(VmHtml.code(str)).append("&gt;");
            } else {
                sb.append("<").append(str).append(">");
            }
        }
    }
    
    /**
     * Punto de entrada a _objToStr.
     *  
     * _objToStr realiza su trabajo por recursión, y algunos de sus parámetros están
     * relacionados con esta recursión. Esta función establece el valor inicial para
     * estos parámetros.   
     * 
     * @param o El objeto que hay que expresar como cadena. 
     * @param sb El StringBuilder que hay que usar. 
     * @param html true si el resultado debe estar en HTML.
     * @author antonio.vera
     */
    //# VmStr
    private static void _valObjToStr(Object o, StringBuilder sb, boolean html) {
        _objToStr(o, sb, false, "", html);
    }
    
    /**
     * Obtiene una representación en forma de cadena de un objeto, el formato del
     * resultado está inspirado en JSON, pero esta pensada para depuración 
     * solamente (por ejemplo, las cadenas de más de 100 caracteres, se truncan y
     * se les añade unos puntos suspensivos). 
     * 
     * @param o El objeto que hay que expresar como cadena. 
     * @param html true si el resultado debe estar en HTML.
     * @author antonio.vera
     */
    //# VmStr
    public static String objToStr(Object obj, boolean html) {
        StringBuilder sb = new StringBuilder("");
        if(html) {
            sb.append("<pre>");
        }
        _valObjToStr(obj, sb, html);
        if(html) {
            sb.append("</pre>");
        }
        return sb.toString();
    }

    /**
     * Obtiene una representación en forma de cadena de un objeto. 
     * 
     * @param o El objeto que hay que expresar como cadena. 
     * @author antonio.vera
     */
    //# VmStr
    public static String objToStr(Object obj) {
        return objToStr(obj, false);
    }

    /**
     * Obtiene una representación en forma de cadena de una variable.
     * 
     * @param nombreVar nombre de la variable.
     * @param obj valor de la variable.
     * @param html true si el resultado debe estar en HTML.
     * @return
     * @author antonio.vera
     */
    //# VmStr
    public static String varToStr(String nombreVar, Object obj, boolean html) {
        StringBuilder sb = new StringBuilder();
        if(html) {
            sb.append("<pre><b>");
        }
        sb.append(nombreVar);
        if(html) {
            sb.append("</b>");
        }
        sb.append(" = ");
        _valObjToStr(obj, sb, html);
        sb.append(";");
        if(html) {
            sb.append("</pre>");
        }
        return sb.toString();
    }

    /**
     * Obtiene una representación en forma de cadena de una variable.
     * 
     * @param nombreVar nombre de la variable.
     * @param obj valor de la variable.
     * @return
     * @author antonio.vera
     */
    //# VmStr
    public static String varToStr(String nombreVar, Object obj) {
        return varToStr(nombreVar, obj, false);
    }
    
    /**
     * Escribe en consola una representación como cadena de un objeto. 
     * 
     * @param obj Objeto a escribir.
     * @return
     * @author antonio.vera
     */
    //# VmStr
    public static void printObj(Object obj) {
        System.out.println(objToStr(obj));
    }

    /**
     * Escribe en consola una representación como cadena de una variable. 
     * 
     * @param nombreVar Nombre de la variable.
     * @param obj valor de la variable.
     * @return
     * @author antonio.vera
     */
    //# VmStr
    public static void printVar(String nombreVar, Object obj) {
        System.out.println(varToStr(nombreVar, obj));
    }

    /**
     * Obtiene el resultado de sustituir en un texto, variables por su valor.
     * @param texto Texto de entrada, con variables con formato [nombre_variable] 
     * @param regExp Expresión regular, con una captura para el nombre, que localiza las variables. 
     * @param variables Map con las asociaciones nombre_variable = valor 
     * @param defecto Valor que se usará en aquellas variables que no se definan.
     * @return texto con las variables sustituidas.
     */
    //# VmStr
    public static String parseVars(String texto, String regExp, Map<String, ?> variables, String defecto) {
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(texto);
        StringBuilder result = new StringBuilder("");
        String k;
        Object v;
        int i1, i2;
        i1 = 0;
        while(m.find()) {
            i2 = m.start();
            result.append(texto.substring(i1, i2));
            k = m.group(1);
            if(variables.containsKey(k)) {
                v = variables.get(k);
                if(v==null) {
                    result.append(defecto);
                } else {
                    result.append(v.toString());
                }
            } else {
                result.append(defecto);
            }
            i1 = m.end();
        }
        result.append(texto.substring(i1));
        return result.toString();
    }
    
    /**
     * Obtiene el resultado de sustituir en un texto, variables por su valor.
     * @param texto Texto de entrada, con variables con formato [nombre_variable] 
     * @param variables Map con las asociaciones nombre_variable = valor 
     * @param defecto Valor que se usará en aquellas variables que no se definan.
     * @return texto con las variables sustituidas.
     */
    //# VmStr
    public static String parseVars(String texto, Map<String, ?> variables, String defecto) {
        return parseVars(texto, "\\[\\s*([a-zA-Z0-9_-]+)\\s*\\]", variables, "");
    }

    /**
     * Obtiene el resultado de sustituir en un texto, variables por su valor.
     * @param texto Texto de entrada, con variables con formato ${nombre_variable} 
     * @param variables Map con las asociaciones nombre_variable = valor 
     * @param defecto Valor que se usará en aquellas variables que no se definan.
     * @return texto con las variables sustituidas.
     */
    //# VmStr
    public static String parseVarsFM(String texto, Map<String, ?> variables, String defecto) {
        return parseVars(texto, "\\$\\{\\s*([a-zA-Z0-9_-]+)\\s*\\}", variables, "");
    }
    
    /**
     * Obtiene el resultado de sustituir en un texto, variables por su valor.
     * @param texto Texto de entrada, con variables con formato [nombre_variable] 
     * @param variables Map con las asociaciones nombre_variable = valor 
     * @return texto con las variables sustituidas.
     */
    //# VmStr
    public static String parseVars(String texto, Map<String, ?> variables) {
        return parseVars(texto, variables, "");
    }

}
