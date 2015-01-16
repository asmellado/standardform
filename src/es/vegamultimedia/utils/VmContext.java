package es.vegamultimedia.utils;

import java.util.Date;

import org.apache.commons.mail.EmailConstants;

/**
 * Clase que almacena información de contexto de ejecución. Esta información
 * incluye:  
 * 
 * - Una caché.
 * - VmList para logs de traces de debug y excepciones.
 * - Información del desarrollador (para recibir log en caso de excepción).
 * - La fecha actual, que se puede obtener pero también modificar, util para hacer
 *   simulaciones en el contexto de un tiempo diferente al actual.
 * 
 * Uso: 
 * 
 * 1) extender VmContext en una subclase, inicializar el developer en el
 * constructor, o antes de usarla. Los métodos se que se vayan a ejecutar en
 * el contexto creado serán miembros NO estáticos de la clase, y deben usar
 * getCurrentDate para obtener la fecha actual y los métodos de log para traces
 * y excepciones. Cuando se deje de usar el contexto, llamar a sendLog para
 * enviar las excepciones al desarrollador, por mail (solo se envía en caso
 * de que hayan logueado excepciones).
 * 
 * 2) Otra posibilidad es instanciar e inicializar directamente VmContext y
 * usarlo de la misma forma (habría que pasar por parámetro el contexto a 
 * los métodos afectados, o guardar el contexto en una clase accesible por
 * los métodos, por ejemplo, la suya propia).  
 * 
 * @author antonio.vera
 */
public class VmContext {
    private VmMap _cache;
    private VmList _log;
    private String _name;
    private boolean _enviarMail;
    private VmContext.VmDeveloper _developer;
    private Date _fechaActual;
    private boolean _testing;

    /**
     * Almacena información que permite contactar con el desarrollador de la
     * aplicación. 
     * 
     * @author antonio.vera
     */
    public static class VmDeveloper {
        private String _host;
        private String _user;
        private String _pass;
        private String _from;
        private String _name;
        private String _mail;
        
        public VmDeveloper host(String v) { _host = v; return this; }
        public VmDeveloper user(String v) { _user = v; return this; }
        public VmDeveloper pass(String v) { _pass = v; return this; }
        public VmDeveloper from(String v) { _from = v; return this; }
        public VmDeveloper name(String v) { _name = v; return this; }
        public VmDeveloper mail(String v) { _mail = v; return this; }

        public String getHost() { return _host; }
        public String getUser() { return _user; }
        public String getPass() { return _pass; }
        public String getFrom() { return _from; }
        public String getName() { return _name; }
        public String getMail() { return _mail; }

        void send(String subject, String body) {
            sendHtml(subject, "<pre>" + VmHtml.code(body) + "</pre>");
        }

        void sendHtml(String subject, String body) {
            try {
                new VmMail().
                    authentication(_user, _pass).
                    host(_host).
                    from(_from).
                    to(_mail, _name).
                    subject(subject).
                    date(new Date()).
                    charset(EmailConstants.UTF_8).
                    html(body).
                    send();
            } catch(Exception e) {
                // Si esto falla, lo único que se puede hacer es tracear la pila a la
                // consola.
                e.printStackTrace();
            }
        }
    }
    
        
    /**
     * Constructor de facto.
     * @param email null o emal del desarrollador.
     * @author antonio.vera
     */
    //# VmContext
    private void _init(VmContext.VmDeveloper developer) {
        _cache = new VmMap();
        _log = new VmList();
        _name = "undefined";
        _enviarMail = false;
        _developer = developer;
        _fechaActual = null;
        _testing = false;
    }

    /**
     * Constructor.
     * @author antonio.vera
     */
    //# VmContext
    public VmContext() {
        super();
        _init(null);
    }

    /**
     * Constructor.
     * @param email
     * @author antonio.vera
     */
    //# VmContext
    public VmContext(VmContext.VmDeveloper developer) {
        super();
        _init(developer);
    }
    
    /**
     * Obtiene una subcache
     * @param ruta... (vararg) cada parámetro es un item de la ruta hasta la subcache
     * @return
     * @author antonio.vera
     */
    //# VmContext
    public VmMap getCache(String... ruta) {
        VmMap cache = _cache;
        for(int i=0; i<ruta.length; i++) {
            if(!cache.containsKey(ruta[i])) {
                cache.entry(ruta[i], new VmMap());
            }
            cache = (VmMap)cache.get(ruta[i]);
        }
        return cache;
    }
    
    /**
     * Añade un mensaje de log a la lista interna (no imprime nada en consola)
     * @param log Mensaje del trace.
     * @author antonio.vera
     */
    //# VmContext
    public void log(Object log) {
        _log.item(log);
    }

    /**
     * Añade un mensaje de log a la lista interna (no imprime nada en consola)
     * @param log Mensaje del trace, que puede incluir opciones de formato.
     * @param objs Parámetros para las opciones de formato.
     * @author antonio.vera
     */
    //# VmContext
    public void logFormat(String log, Object... objs) {
        _log.item(String.format(log, objs));
    }
    
    /**
     * 
     * @param e
     * @param variables
     * @author antonio.vera
     */
    //# VmContext
    public void logException(Exception e, Object... variables) {
        VmMap excepcion = new VmMap().
                entry("Type", e.getClass().getName()).
                entry("Detail", e.getMessage()).
                entry("StackTrace", e.getStackTrace()).
                items("Params", variables);
        
        log(excepcion);
        _enviarMail = true;
    }
    
    //# VmContext
    public void sendLog() {
        if(_enviarMail) {
            if(_developer!=null) {
                _developer.sendHtml("["+ _name + "] Application Log.",
                                    VmStr.varToStr("LOG:", _log, true));
            }
        }
    }
    
    //# VmContext
    public void print() {
        VmStr.printVar("LOG:", _log);
    }
    
    //# VmContext
    public Date getCurrentDate() {
        if(_fechaActual==null) {
            _fechaActual = new Date();
        }
        return _fechaActual; 
    }
    
    //# VmContext
    public void setCurrentDate(Date d) {
        _fechaActual = d;
    }

    //# VmContext
    public void setTestingMode(boolean t) {
        _testing = t;
    }

    //# VmContext
    public boolean getTestingMode() {
        return _testing;
    }
    
    //# VmContext
    public VmDeveloper getDeveloper() {
        return _developer;
    }

    //# VmContext
    public void setDeveloper(VmDeveloper developer) {
        _developer = developer;
    }

    //# VmContext
    public String getName() {
        return _name;
    }

    //# VmContext
    public void setName(String name) {
        _name = name;
    }

}


