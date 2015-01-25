package es.vegamultimedia.utils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Clase para almacenar datos binarios. Internamente es un array de bytes y
 * un contador que indica cuantos bytes son válidos (la longitud del array
 * se debe ignorar).
 */
public class VmBytes {
    protected byte[] bytes; 
    protected int numBytes;
    
    /**
     * Constructor
     * @param b Array de bytes.
     * @param n Número de bytes válidos en el array.
     * @author antonio.vera
     */
    //# VmFile.VmBytes
    public VmBytes(byte[] b, int n) {
        bytes = b;
        numBytes = n;
    }

    public VmBytes(byte[] b) {
        bytes = b;
        numBytes = b.length;
    }

    /**
     * Constructor
     * @param size tamaño inicial del array.
     * @author antonio.vera
     */
    //# VmFile.VmBytes
    public VmBytes(int size) {
        bytes = new byte[size];
        this.numBytes = 0;
    }
    
    /**
     * Add a byte to the object. 
     * @param in Array with the source bytes.
     * @return
     * @author antonio.vera
     */
    //# VmFile.VmBytes
    public void addByte(byte in) {
        addBytes(in);
    }

    /**
     * Add bytes to the object. 
     * @param in Array with the source bytes.
     * @return
     * @author antonio.vera
     */
    //# VmFile.VmBytes
    public void addBytes(byte... in) {
        addBytes(in, 0, in.length);
    }

    /**
     * Add bytes to the object. 
     * @param in Array with the source bytes.
     * @param offset First byte offset in the first param to copy.
     * @param length Number of bytes to copy.
     * @return
     * @author antonio.vera
     */
    //# VmFile.VmBytes
    public void addBytes(byte[] in, int offset, int length) {
        int total;
        byte[] tmp;
        int i;
        total = bytes.length;
        if((numBytes+length)>total) {
            do {
                total *= 2;
            } while((numBytes+length)>total);
            tmp = new byte[total];
            for(i=0; i<numBytes; i++) {
                tmp[i] = bytes[i];
            }
            bytes = tmp;
            tmp = null;
        }
        for(i=0; i<length; i++) {
            bytes[i+numBytes] = in[i];
        }
        numBytes += length;
    }

    public void clean() {
        numBytes = 0;
    }
    
    /**
     * Obtiene el array de bytes.
     * @return Los bytes
     * @author antonio.vera
     */
    //# VmFile.VmBytes
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Obtiene el número de bytes guardados.
     * @return El número de bytes que son válidos en el array devuelto por
     *         getBytes.
     * @author antonio.vera
     */
    //# VmFile.VmBytes
    public int getLength() {
        return numBytes;
    }
    
    /**
     * Obtiene un OutputStream que permitirá guardar datos binarios en el
     * objeto VmBytes. 
     * @return
     * @author antonio.vera
     */
    public OutputStream getOutputStream() {
        return new VmBytesOutputStream(this);
    }
    
    public InputStream getInputStream(int chunkSize) {
        return new VmBytesInputStream(this, chunkSize);
    }

    public InputStream getInputStream() {
        return new VmBytesInputStream(this);
    }
}
