package es.vegamultimedia.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream backed by a VmBytes object.
 * 
 * @author antonio.vera
 */
// Ahora sólo uso esta clase para hacer pruebas. Si necesito probar un objeto que usa
// un InputStream y quiero probarlo controlando las posibles situaciones que se pueden
// dar, uso un VmBytesInputStream que obtengo de una llamada a:
// 
// (Objeto VmBytes).getInputStream(chunkSize)
// 
// chunkSize es el número máximo de bytes que se pueden leer de una vez del InputStream,
// haciendo este valor muy pequeño (por ejemplo, 1) se lleva al límite el algoritmo
// que se pretende probar.
// 
public class VmBytesInputStream extends InputStream {
    VmBytes bytes;
    int offset;
    int chunkSize;
    boolean eof;

    
    private void _init(VmBytes bytes, int chunkSize) {
        if(chunkSize<1) {
            chunkSize = 1;
        }
        this.bytes = bytes;
        this.chunkSize = chunkSize;
        offset = 0;
        eof = (bytes.numBytes==0) ? true : false ;
    }
    
    public VmBytesInputStream(VmBytes bytes) {
        _init(bytes, Integer.MAX_VALUE);
    }
    
    public VmBytesInputStream(VmBytes bytes, int chunkSize) {
        _init(bytes, chunkSize);
    }
    
    @Override
    public int available() {
        int remaining = bytes.numBytes - offset;
        if(remaining>chunkSize) {
            remaining = chunkSize;
        }
        return remaining;
    }

    @Override
    public int read() throws IOException {
        byte b;
        if(eof) {
            return -1;
        }
        b = bytes.bytes[offset];
        offset++;
        if(offset>=bytes.numBytes) {
            eof = true;
        }
        return b;
    }
    
    @Override
    public int read(byte[] b, int off, int len) {
        int remaining;
        if(eof) {
            return -1;
        }
        if(len<0) {
            return 0;
        }
        if(len>chunkSize) {
            len = chunkSize;
        }
        remaining = bytes.numBytes - offset;
        if(len>=remaining) {
            len = remaining;
            eof = true;
        }
        int o;
        o = offset;
        for(int i=0; i<len; i++) {
            b[off+i] = bytes.bytes[o];
            o++;
        }
        offset = o; 
        return len;
    }
    
    @Override
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }
    
    @Override
    public long skip(long n) {
        int nInt;
        int remaining;
        if(n<=0 || eof) {
            return 0;
        }
        nInt = (n>Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)n;
        remaining = bytes.numBytes - offset;
        if(nInt>=remaining) {
            eof = true;
            nInt = remaining;
        }
        offset += nInt;
        return nInt; 
    }

    @Override
    public void close() {
        eof = true;
    }

    // TODO De momento no se soporta el marcado:
    @Override
    public void mark(int readlimit) { }
    @Override
    public boolean markSupported() { return false; }
    @Override
    public void reset() {}

}
