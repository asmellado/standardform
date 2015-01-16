package es.vegamultimedia.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

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
}
