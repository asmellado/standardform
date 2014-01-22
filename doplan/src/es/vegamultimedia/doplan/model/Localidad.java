package es.vegamultimedia.doplan.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import es.vegamultimedia.standardform.annotations.StandardFormField;
import es.vegamultimedia.standardform.annotations.StandardForm;

@Entity
@StandardForm(listViewName = "localidades", detailViewName = "localidad")
public class Localidad {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
	@StandardFormField(caption = "Nombre")
	private String nombre;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}