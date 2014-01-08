package es.vegamultimedia.doplan.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Localidad implements Serializable {
	
	private long id;
	private String nombre;
	
	public Localidad(long id, String nombre) {
		setId(id);
		setNombre(nombre);
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	

}
