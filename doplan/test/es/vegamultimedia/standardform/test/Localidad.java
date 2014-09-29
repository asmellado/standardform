package es.vegamultimedia.standardform.test;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import es.vegamultimedia.doplan.model.Bean;
import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.annotations.StandardForm;

@Entity
@StandardForm(listViewName = "localidades", detailViewName = "localidad")
public class Localidad implements Bean {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
	@StandardFormField(type = StandardFormField.Type.TEXT_FIELD, caption = "Nombre")
	private String nombre;
	
	@Override
	public int getId() {
		return id;
	}
	@Override
	public void setId(int id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	@Override
	public int hashCode() {
		return id;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==null)
			return false;
		return (id==((Localidad)obj).getId());
	}
	
}