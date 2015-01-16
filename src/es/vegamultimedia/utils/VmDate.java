package es.vegamultimedia.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Para comparar fechas y obtener mensajes de cuenta atrás. 
 * 
 * Ejemplo, una aplicación puede querer que se avise de los dias que faltan para
 * un evento, o las horas que faltan si falta menos de un día, o los minutos si
 * falta menos de una hora, o incluso los segundos, si falta menos de un minuto.
 * 
 * Eso se consigue con los métodos countDownTime.
 * 
 * Los métodos para comparar fechas que sirven para obtener cuantas unidades de
 * tiempo (dias, minutos, horas...) separan dos fechas, también pueden ser útiles.
 * 
 * @author antonio.vera
 */
public class VmDate {
    static public enum UNIT {SECONDS, MINUTES, HOURS, DAYS, WEEKS};
    static private String[] unitsN = new String[] {"segundos", "minutos", "horas", "días", "semanas"};
    static private String[] units1 = new String[] {"segundo",  "minuto",  "hora",  "día",  "semana"};
    
    private Calendar gc;

    //# VmDate
    public VmDate() {
        gc = GregorianCalendar.getInstance();
    }
    
    //# VmDate
    public VmDate(Date d) {
        gc = GregorianCalendar.getInstance();
        gc.setTime(d);
    }

    //# VmDate
    public VmDate(Calendar d) {
        gc = d;
    }

    //# VmDate
    public VmDate(int year, int month, int day) {
        gc = GregorianCalendar.getInstance();
        gc.clear();
        gc.set(year, month, day);
    }

    //# VmDate
    public VmDate(int year, int month, int day, int hour) {
        gc = GregorianCalendar.getInstance();
        gc.clear();
        gc.set(year, month, day, hour, 0);
    }

    //# VmDate
    public VmDate(int year, int month, int day, int hour, int minute) {
        gc = GregorianCalendar.getInstance();
        gc.clear();
        gc.set(year, month, day, hour, minute);
    }
    
    //# VmDate
    public long compare(Date date) {
        return compare(date, UNIT.DAYS);
    }

    //# VmDate
    public long compare(Date date, UNIT unit) {
        return compare(new VmDate(date), unit);
    }

    //# VmDate
    public long compare(VmDate date) {
        return compare(date, UNIT.DAYS);
    }

    //# VmDate
    public long compare(VmDate date, UNIT unit) {
        return compare(date.gc, unit);
    }
    
    //# VmDate
    public long compare(Calendar c, UNIT unit) {
        return compare(gc, c, unit);
    }
    
    //# VmDate
    public static long compare(Calendar c1, Calendar c2, UNIT unit) {
        long ms = c1.getTimeInMillis();
        long factor = 1;
        ms -= c2.getTimeInMillis();
        switch(unit) {
        case WEEKS:
            factor *= 7;
        case DAYS:
            factor *= 24;
        case HOURS:
            factor *= 60;
        case MINUTES:
            factor *= 60;
        case SECONDS:
            factor *= 1000;
        }
        return (ms+factor-1)/factor;
    }
    /**
     * @param c El tiempo (posterior al del objeto) para el mensaje de cuenta atrás.
     * @param format1 El formato del mensaje en caso de que la cuenta atrás sea
     *                1 (singular) 
     * @param formatN El formato del mensaje en caso de que la cuenta atrás sea
     *                mayor que 1 (plural)
     * @param unit La unidad de tiempo superior en la que hacer la cuenta atrás. Si
     *             el resultado con esta unidad es menor que 1, lo intentará con
     *             la inmediatamente menor, y así.
     * @return El mensaje de cuenta atrás.
     * @author antonio.vera
     */
    //# VmDate
    public String countDownTime(Calendar c, String format1, String formatN, UNIT unit) {
        long d;
        d = compare(c, gc, unit);
        if(d<0) {
            return null;
        }
        if(d==0) {
            if(unit.ordinal()>0) {
                return countDownTime(c, format1, formatN, UNIT.values()[unit.ordinal()-1]);
            }
        }
        if(d==1) {
            return String.format(format1, String.valueOf(d) + " " + units1[unit.ordinal()]);
        } 
        return String.format(formatN, String.valueOf(d) + " " + unitsN[unit.ordinal()]);
    }

    //# VmDate
    public String countDownTime(Date c, String format1, String formatN) {
        return countDownTime(c, format1, formatN, UNIT.DAYS);
    }
    
    //# VmDate
    public String countDownTime(Date c, String format1, String formatN, UNIT unit) {
        return countDownTime(new VmDate(c), format1, formatN, unit);
    }

    //# VmDate
    public String countDownTime(VmDate c, String format1, String formatN) {
        return countDownTime(c, format1, formatN, UNIT.DAYS);
    }
    
    //# VmDate
    public String countDownTime(VmDate c, String format1, String formatN, UNIT unit) {
        return countDownTime(c.gc, format1, formatN, unit);
    }

    //# VmDate
    public String countDownTime(Calendar c, String format1, String formatN) {
        return countDownTime(c, format1, formatN, UNIT.DAYS);
    }
    
}
