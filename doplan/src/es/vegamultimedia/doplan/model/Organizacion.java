package es.vegamultimedia.doplan.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

@Entity
public class Organizacion {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;

	private Localidad localidad;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
	private String nombre;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
	private String personaContacto;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
	@Email
	private String emailContacto;
	
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