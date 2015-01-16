package es.vegamultimedia.utils;

import java.util.TreeMap;


/**
 * Implementa un Map<String, Object> sencillo de inicializar.
 * 
 * Uso: VmMap map = new VmMap().
 *                      entry(k1, v1).
 *                      entry(k2, v2).
 *                      items(k3, v31, v32, v33, v34...). // k3 será una VmList
 *                      ...
 * 
 * @author antonio.vera
 */
public class VmMap extends TreeMap<String, Object> {
    
    //# VmMap
    public VmMap() {
        super();
    }

    //# VmMap
    public VmMap(String s, Object o) {
        super();
        put(s, o);
    }
    
    //# VmMap
    public VmMap entry(String s, Object o) {
        put(s, o);
        return this;
    }
    
    //# VmMap
    public VmMap item(String s, Object o) {
        if(!containsKey(s)) {
            entry(s, new VmList(o));
        } else {
            Object objList;
            objList = get(s);
            if(objList instanceof VmList) {
                ((VmList)objList).item(o);
            } else {
                entry(s, new VmList().item(objList).item(o));
            }
        }
        return this;
    }

    //# VmMap
    public VmMap items(String s, Object... o) {
        if(!containsKey(s)) {
            entry(s, new VmList(o));
        } else {
            Object objList;
            objList = get(s);
            if(objList instanceof VmList) {
                ((VmList)objList).items(o);
            } else {
                entry(s, new VmList().item(objList).items(o));
            }
        }
        return this;
    }

    /**
     * Obtiene un elemento del mapa, como cadena.
     * @param key
     * @return
     * @author antonio.vera
     */
    //# VmMap
    public String getStr(String key) {
        Object o;
        o = get(key);
        if(o==null) {
            return "";
        }
        return o.toString();
    }

}
