package es.vegamultimedia.doplan.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Localidad implements Serializable {
	
	private int id;
	private String nombre;
	
	public Localidad(int id, String nombre) {
		setId(id);
		setNombre(nombre);
	}
	
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
