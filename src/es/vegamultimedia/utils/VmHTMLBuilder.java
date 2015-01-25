package es.vegamultimedia.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for low level HTML pages creation.
 * 
 * Maintains an internal binary buffer, with the HTML page encoded in the
 * builder charset.
 * 
 * Use:
 * 
 * builder = new VmHTMLBuilder(StandardCharsets.ISO_8859_1); // Or another charset
 * builder.<appendMethod>(...);
 * builder.<appendMethod>(...);
 * builder.<appendMethod>(...); 
 * ...
 * outputStream.write(builder.getBytes(), 0, builder.getLength());
 * outputStream.flush(); // Optional
 * builder.clean();
 *  
 * or
 * 
 * builder.out(outputStream);
 * 
 * or
 * 
 * builder.flush(outputStream);
 * 
 * or get the bytes with getBytes and getLength, and do with them whatever you want.
 * 
 * @author antonio.vera
 */
public class VmHTMLBuilder {
    // Precompiled RegExp.
    private static Pattern reSpecialChars = Pattern.compile("[&<>\\\"\\']");

    // Internal byte buffer.
    private VmBytes bytes;

    // Charset for the generated HTML page.
    private Charset charset;

    /**
     * Constructor. Normal use for the parameter are StandardCharsets.ISO_8859_1
     * and StandardCharsets.UTF_8.
     * 
     * @param charset
     * @author antonio.vera
     */
    public VmHTMLBuilder(Charset charset) {
        this.charset = charset;
        bytes = new VmBytes(4096);
    }

    /**
     * @return an empty VmHTMLBuilder that uses the same charset that this one.
     * @author antonio.vera
     */
    public VmHTMLBuilder getCompatibleBuilder() {
        return new VmHTMLBuilder(charset); 
    }
    
    /**
     * Sends the buffered bytes to the output stream a cleans the internal buffer.
     * 
     * @param outputStream
     * @throws IOException
     * @author antonio.vera
     */
    public void out(OutputStream outputStream) throws IOException {
        outputStream.write(bytes.getBytes(), 0 ,bytes.getLength());
        bytes.clean();
    }
    
    /**
     * Sends the buffered bytes to the output stream, flushes them, a cleans the
     * internal buffer.
     * 
     * @param outputStream
     * @throws IOException
     * @author antonio.vera
     */
    public void flush(OutputStream outputStream) throws IOException {
        out(outputStream);
        outputStream.flush();
    }
    
    /**
     * Cleans the internal buffer.
     * 
     * @param outputStream
     * @throws IOException
     * @author antonio.vera
     */
    public void clean() {
        bytes.clean();
    }
    
    /**
     * Gets a reference to the internal byte array. Call getLength to get the number of
     * valid bytes in that array. Use only to read the bytes, and don't append anything
     * to the builder in the mean time.
     * 
     * @return a reference to the internal byte array.
     * @author antonio.vera
     */
    public byte[] getBytes() {
        return bytes.getBytes();
    }

    /**
     * Gets the number of valid bytes in the internal buffer.
     *  
     * @return the number of valid bytes in the internal buffer.
     * @author antonio.vera
     */
    public int getLength() {
        return bytes.getLength();
    }
    
    /**
     * Encodes back the buffered bytes, into an unicode string (useful for debug
     * purposes).
     * 
     * @return string with the internal buffer content.   
     * @author antonio.vera
     */
    public String toString() {
        return new String(bytes.getBytes(), 0, bytes.getLength(), charset);
    }
    
    /**
     * Appends the content of another builder. The behavior of this method is not
     * defined if the builders are not compatible.
     * 
     * @param the builder whose bytes will be appended
     * @return this
     * @author antonio.vera
     */
    public VmHTMLBuilder append(VmHTMLBuilder builder) {
        bytes.addBytes(builder.bytes.getBytes(), 0, builder.bytes.getLength());
        return this;
    }
    
    /**
     * Appends a raw CharSequence, without performing any transformation but the charset
     * encoding. Actually, this is equivalent to appendHTML.
     * 
     * @param The CharSequence to append.
     * @return this
     * @author antonio.vera
     */
    private VmHTMLBuilder appendRaw(CharSequence cs) {
        ByteBuffer bb = charset.encode(cs.toString());
        bytes.addBytes(bb.array(), 0, bb.remaining());
        return this;
    }

    /**
     * Appends an HTML string.
     * 
     * @param The HTML string to append.
     * @return this
     * @author antonio.vera
     */
    public VmHTMLBuilder appendHTML(String str) {
        ByteBuffer bb = charset.encode(str);
        bytes.addBytes(bb.array(), 0, bb.remaining());
        return this;
    }

    /**
     * Appends a TEXT string. Encodes as entities the HTML special chars < > & " and '
     * and then performes the charset encoding. 
     * 
     * @param The TEXT string to append.
     * @return this
     * @author antonio.vera
     */
    public VmHTMLBuilder appendText(String str) {
        Matcher m;
        m = reSpecialChars.matcher(str);
        int i1 = 0;
        int i2 = 0;
        int ii = str.length();
        while(true) {
            if(!m.find()) {
                appendRaw(str.subSequence(i1, ii));
                break;
            }
            i2 = m.start();
            if(i2>i1) {
                appendRaw(str.subSequence(i1, i2));
            }
            switch(str.charAt(i2)) {
            case '&':
                appendRaw("&amp;");
                break;
            case '<':
                appendRaw("&lt;");
                break;
            case '>':
                appendRaw("&gt;");
                break;
            case '"':
                appendRaw("&quot;");
                break;
            case '\'':
                appendRaw("&apos;");
                break;
            default:
                appendRaw(str.subSequence(i2, i2+1)); // NDP 
                break;
            }
            i1 = i2+1;
        }
        return this;
    }

    // TODO: "\0 TEXT texto plano" 
    // TODO: "\0 HTML texto HTML" 
    public VmHTMLBuilder append(String str) {
        if(str==null) return this;
        if(str.length()==0) return this;
        if(str.charAt(0)!='\0') {
            return appendText(str);
        }
        return this;
    }
}

