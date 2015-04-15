package es.vegamultimedia.standardform.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import es.vegamultimedia.standardform.annotations.StandardFormField;

/**
 * An object of this class represents a bean that uses Java Persistence API (JPA) for persistence
 */
@MappedSuperclass
public class BeanJPA implements BeanWithId, Serializable {

	private static final long serialVersionUID = 2722402737890863274L;
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@StandardFormField(hidden = true)
	private Integer id;
	
	@Override
	public Integer getId() {
		return id;
	}
	@Override
	public void setId(Object id) {
		this.id = (Integer) id;
	}
	@Override
	public int hashCode() {
		if (id == null)
			return super.hashCode();
		return id;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==null || id==null || !(obj instanceof BeanJPA))
			return false;
		return (id.equals(((BeanJPA)obj).getId()));
	}
}