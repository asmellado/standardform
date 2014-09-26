package es.vegamultimedia.doplan.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.annotations.StandardForm;

@Entity
@StandardForm(listViewName = "organizaciones", detailViewName = "organizacion",
	columns = {"nombre", "personaContacto", "emailContacto"})
public class Organizacion implements Bean {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;

	@NotNull
	@StandardFormField(type = StandardFormField.Type.COMBO_BOX, caption = "Localidad 1")
	public Localidad localidad1;
	
	@NotNull
	@StandardFormField(type = StandardFormField.Type.OPTION_GROUP, caption = "Localidad 2")
	public Localidad localidad2;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=20)
	@StandardFormField(type = StandardFormField.Type.TEXT_FIELD, caption = "Nombre")
	private String nombre;
	
	@Basic(optional=false)
	@NotNull
	@StandardFormField(type = StandardFormField.Type.TEXT_AREA, caption = "Persona de contacto",
		help = "Principal persona de contacto dentro de la organización")
	private String personaContacto;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
	@Email
	@StandardFormField(type = StandardFormField.Type.TEXT_FIELD, caption = "E-mail de contacto",
		help = "E-mail de la persona de contacto")
	private String emailContacto;
	
	@Basic(optional=false)
	@NotNull
	@StandardFormField(type = StandardFormField.Type.CHECK_BOX, caption = "Interesado",
		help = "Indica si está interesado")
	private boolean interesado;
	
	@Override
	public int getId() {
		return id;
	}
	@Override
	public void setId(int id) {
		this.id = id;
	}
	public Localidad getLocalidad1() {
		return localidad1;
	}
	public void setLocalidad1(Localidad localidad) {
		this.localidad1 = localidad;
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
	public boolean isInteresado() {
		return interesado;
	}
	public void setInteresado(boolean interesado) {
		this.interesado = interesado;
	}
	public Localidad getLocalidad2() {
		return localidad2;
	}
	public void setLocalidad2(Localidad localidad2) {
		this.localidad2 = localidad2;
	}
}