package es.vegamultimedia.standardform.model;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

@SuppressWarnings("serial")
public abstract class BeanMongo implements Bean, Serializable {

	@Id
	private ObjectId id;
	
	@Override
	public ObjectId getId() {
		return id;
	}
	@Override
	public void setId(Object id) {
		this.id = (ObjectId) id;
	}
	@Override
	public int hashCode() {
		if (id == null)
			return 0;
		else
			return id.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof BeanMongo))
			return false;
		return (hashCode() == obj.hashCode());
	}
}