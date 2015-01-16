package es.vegamultimedia.utils;

import java.util.regex.Pattern;

/**
 * Colección de métodos relacionados con HTML.
 * 
 * @author antonio.vera
 */
public class VmHtml{
    
    /**
     * Codifica una cadena para que pueda usarse en HTML (equivale al
     * htmlspecialchars de PHP).
     * @param str La cadena a codificar.
     * @return La cadena codificada.
     * @author antonio.vera
     */
    //# VmHtml
    // TODO cambiarle el nombre, se llama así porque al principio este método
    // hacía más cosas.
    // TODO hacerlo de forma má eficiente, con una sola expresion regular.
    public static String code(CharSequence str) {
        String res;
        res = Pattern.compile("&").matcher(str).replaceAll("&amp;");
        res = Pattern.compile("<").matcher(res).replaceAll("&lt;");
        res = Pattern.compile(">").matcher(res).replaceAll("&gt;");
        return res;
    }
}
