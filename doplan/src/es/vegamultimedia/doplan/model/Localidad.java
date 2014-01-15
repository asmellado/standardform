package es.vegamultimedia.doplan.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@NamedQuery(name = Localidad.QUERY_OBTENER_TODAS, query = "SELECT l FROM Localidad l")
public class Localidad {

	public static final String QUERY_OBTENER_TODAS = "Localidad.obtenerTodas";
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;
	
	@Basic(optional=false)
	@NotNull
	@Size(min=1,max=100)
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