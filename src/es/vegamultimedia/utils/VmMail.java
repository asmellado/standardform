package es.vegamultimedia.utils;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class VmMail extends HtmlEmail {

    public VmMail() {
        super();
        setCharset(EmailConstants.UTF_8);
    }
    
    public VmMail from(String v) throws EmailException {
        setFrom(v);
        return this;
    }

    public VmMail authentication(String user, String pass) {
        setAuthentication(user, pass);
        return this;
    }

    public VmMail host(String host) {
        setHostName(host);
        return this;
    }

    public VmMail subject(String v) {
        setSubject(v);
        return this;
    }

    public VmMail date(Date v) {
        setSentDate(v);
        return this;
    }

    public VmMail charset(String v) {
        setCharset(v);
        return this;
    }
    
    public VmMail html(String v) throws EmailException {
        setHtmlMsg(v);
        return this;
    }

    public VmMail to(String email, String name) throws EmailException {
        addTo(email, name);
        return this;
    }

    public VmMail template(String fullPath, Map<String, ?> variables) throws IOException, EmailException {
        String archivo = VmFile.loadUTF8File(fullPath);
        setHtmlMsg(VmStr.parseVarsFM(archivo, variables, "null"));
        return this;
    }

    public VmMail debug(boolean b) {
        setDebug(b);
        return this;
    }

}
