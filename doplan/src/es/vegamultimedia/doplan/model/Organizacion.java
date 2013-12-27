package es.vegamultimedia.doplan.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Organizacion implements Serializable {
	
	private int id;
	private Localidad localidad;
	private String nombre;
	private String personaContacto;
	private String emailContacto;
	
	public Organizacion(int id, Localidad localidad, String nombre,
			String personaContacto, String emailContacto) {
		setId(id);
		setLocalidadId(localidad);
		setNombre(nombre);
		setPersonaContacto(personaContacto);
		setEmailContacto(emailContacto);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Localidad getLocalidad() {
		return localidad;
	}
	public void setLocalidadId(Localidad localidad) {
		this.localidad = localidad;
	}

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getPersonaContacto() {
		return personaContacto;
	}
	public void setPersonaContacto(String personaContacto) {
		this.personaContacto = personaContacto;
	}
	public String getEmailContacto() {
		return emailContacto;
	}
	public void setEmailContacto(String emailContacto) {
		this.emailContacto = emailContacto;
	}
}