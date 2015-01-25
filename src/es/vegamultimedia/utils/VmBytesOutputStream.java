package es.vegamultimedia.utils;

import java.io.OutputStream;
/**
 * OutputStream backed by a VmBytes object.
 * 
 * @author antonio.vera
 */
// 
public class VmBytesOutputStream extends OutputStream {
    VmBytes bytes;
    boolean closed;
    
    public VmBytesOutputStream(int initialSize) {
        bytes = new VmBytes(initialSize);
        closed = false;
    }

    public VmBytesOutputStream() {
        bytes = new VmBytes(4096);
        closed = false;
    }

    public VmBytesOutputStream(VmBytes bytes) {
        this.bytes = bytes;
        closed = false;
    }

    
    @Override
    public void write(int b) {
        byte[] tmp = new byte[]{(byte)b};
        bytes.addBytes(tmp, 0, 1);
    }
    @Override
    public void write(byte[] b) {
        bytes.addBytes(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        bytes.addBytes(b, off, len);
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    public byte[] getBytes() {
        return bytes.getBytes();
    }

    public int getLength() {
        return bytes.getLength();
    }
}
