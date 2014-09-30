package es.vegamultimedia.standardform.test;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.model.Bean;

@SuppressWarnings("serial")
@Entity
@StandardForm(listViewName = "localidades", detailViewName = "localidad")
public class Localidad extends Bean {
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
	@StandardFormField(type = StandardFormField.Type.TEXT_FIELD, caption = "Nombre")
	private String nombre;

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}