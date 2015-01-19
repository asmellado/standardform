package es.vegamultimedia.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class VmFile {

    /**
     * Clase para almacenar datos binarios. Internamente es un array de bytes y
     * un contador que indica cuantos bytes son válidos (la longitud del array
     * se debe ignorar).
     */
    //# VmFile
    public static class VmBytes {
        private byte[] bytes; 
        private int numBytes;
        
        /**
         * Constructor
         * @param b Array de bytes.
         * @param n Número de bytes válidos en el array.
         * @author antonio.vera
         */
        //# VmFile.VmBytes
        public VmBytes(byte[]b, int n) {
            bytes = b;
            numBytes = n;
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
         * @author antonio.vera
         */
        //# VmFile.VmBytes
        public byte[] addBytes(byte[] in, int offset, int length) {
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
            return bytes;
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
    }
    
    /**
     * Carga un archivo binario desde disco.
     * @param fileName El nombre del archivo.
     * @return Un objeto VmBytes con los bytes del archivo.
     * @throws IOException
     * @author antonio.vera
     */
    //# VmFile
    public static VmBytes loadBinFile(String fileName) throws IOException  {
        FileInputStream fileInput = null;
        BufferedInputStream bufferedInput = null;
        
        try {
            fileInput = new FileInputStream(fileName);
            bufferedInput = new BufferedInputStream(fileInput);
            int numActual = 0;
            int numLeidos = 0;
            int numTotal = 10000;
            int i;
            byte[] entrada = new byte[1000];
            byte[] bytes = new byte[numTotal];
            byte[] tmp;
            numLeidos = bufferedInput.read(entrada);
            while(numLeidos>0) {
                if((numActual+numLeidos)>numTotal) {
                    do {
                        numTotal *= 2;
                    } while((numActual+numLeidos)>numTotal);
                    tmp = new byte[numTotal];
                    for(i=0; i<numActual; i++) {
                        tmp[i] = bytes[i];
                    }
                    bytes = tmp;
                    tmp = null;
                }
                for(i=0; i<numLeidos; i++) {
                    bytes[i+numActual] = entrada[i];
                }
                numActual += numLeidos; 
                numLeidos = bufferedInput.read(entrada);
            }
            return new VmBytes(bytes, numActual);
        } finally {
            if(bufferedInput!=null) {
                try {
                    bufferedInput.close();
                } catch(Exception e){}
            }
            if(fileInput!=null) {
                try {
                    fileInput.close();
                } catch(Exception e){}
            }
        }
    }
    
    /**
     * Carga un archivo de texto intentando detectar automáticamente su codificación.
     * @param fileName El nombre del archivo.
     * @return La cadena con el contenido del archivo
     * @throws IOException
     * @author antonio.vera
     */
    //# VmFile
    public static String loadTextAutoFile(String fileName) throws IOException {
        VmBytes vmb;
        vmb = loadBinFile(fileName);
        return binToString(vmb.getBytes(), vmb.getLength());
    }
    
    /**
     * A partir de un a
     * @author antonio.vera
     * @throws UnsupportedEncodingException 
     */
    //# VmFile
    public static String binToString(byte[] bytes, int length) throws UnsupportedEncodingException {
        int i;
        byte b;
        byte st;
        i = 0;
        
        if(length>=3) {
            if(bytes[0]==-17 && bytes[1]==-69 && bytes[2]==-65) {
                return new String(bytes, 3, length-3, "UTF-8");
            }
        }
        st = 0;
        for(i=0; i<length; i++) {
            b = bytes[i];
            if(st==0) {
                if((b&0x80)==0) {
                    continue;
                }
                if((b&0x40)==0) {
                    break;
                }
                if((b&0x20)==0) {
                    st=1;
                    continue;
                }
                if((b&0x10)==0) {
                    st=2;
                    continue;
                }
                if((b&0x08)==0) {
                    st=3;
                    continue;
                }
                break;
            } else {
                if((b&0x80)==0) {
                    break;
                }
                if((b&0x40)!=0) {
                    break;
                }
                st--;
            }
        }
        if(i>=length && st==0) {
            return new String(bytes, 0, length, "UTF-8");
        }
        return new String(bytes, 0, length, "ISO-8859-1");
        
    }

    /**
     * Carga un archivo de texto que está codificado en UTF-8.
     * @param fileName El nombre del archivo.
     * @return La cadena con el contenido del archivo
     * @throws IOException
     * @author antonio.vera
     */
    //# VmFile
    public static String loadUTF8File(String fileName) throws IOException {
        VmBytes vmb;
        vmb = loadBinFile(fileName);
        return new String(vmb.getBytes(), 0, vmb.getLength(), "UTF-8");
    }
    
    
    // 0xEF,0xBB,0xBF
    
    /**
     * Carga un archivo de texto.
     * @param fileName El nombre del archivo.
     * @return La cadena con el contenido del archivo
     * @throws IOException
     * @author antonio.vera
     */
    //# VmFile
    public static String loadTextFile(String fileName) throws IOException {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        String linea;
        String nl = "";
        try {
            archivo = new File(fileName);
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);
            // Lectura del fichero
            while((linea=br.readLine())!=null) {
                sb.append(nl).append(linea);
                nl = "\n";
            }
        } 
        finally {
            if(fr!=null){
                fr.close();
            }
        }
        return sb.toString();
    }

    public static class VmBytesArrayOutputStream extends OutputStream {
        VmFile.VmBytes bytes;
        boolean closed;
        
        public VmBytesArrayOutputStream(int initialSize) {
            bytes = new VmFile.VmBytes(initialSize);
            closed = false;
        }

        public VmBytesArrayOutputStream() {
            bytes = new VmFile.VmBytes(4096);
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

}
