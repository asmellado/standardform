package es.vegamultimedia.standardform.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import es.vegamultimedia.standardform.annotations.StandardFormField;

/**
 * An object of this class represents a bean that uses Java Persistence API (JPA) for persistence
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class BeanJPA implements BeanWithId, Serializable {

	@Id
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
			return 0;
		return id;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof BeanJPA))
			return false;
		return (id==((BeanJPA)obj).getId());
	}
}