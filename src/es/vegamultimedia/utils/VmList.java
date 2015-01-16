package es.vegamultimedia.utils;

import java.util.LinkedList;

/**
 * Implementa un List<Object> sencilla de inicializar.
 * 
 * Usos: VmList list = new VmList().item(v1).item(v2).item(v3)...;
 *       VmList list = new VmList(v1, v2, v3, v4...);
 *       VmList list = new VmList().items(v1, v2, v3...);
 *       O una combinación de las anteriores.
 * 
 * @author antonio.vera
 */
public class VmList extends LinkedList<Object> {
    
    //# VmList
    public VmList() {
        super();
    }

    //# VmList
    public VmList(Object... os) {
        super();
        for(Object o: os) { 
            add(o);
        }
    }
    
    //# VmList
    public VmList item(Object o) {
        add(o);
        return this;
    }

    //# VmList
    public VmList items(Object... os) {
        for(Object o: os) { 
            add(o);
        }
        return this;
    }

}
