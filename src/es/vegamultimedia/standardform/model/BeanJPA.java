package es.vegamultimedia.standardform.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@SuppressWarnings("serial")
@MappedSuperclass
public class BeanJPA implements Bean, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
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
		return id;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof BeanJPA))
			return false;
		return (id==((BeanJPA)obj).getId());
	}
}