package es.vegamultimedia.standardform.model;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

/**
 * An object of this class represents a bean that uses MongoDB and Morphia for persistence
 * This bean has an automatic id field of the ObjectId type
 */
@SuppressWarnings("serial")
public abstract class BeanMongoWithId extends BeanMongo implements BeanWithId, Serializable {

	@Id
	protected ObjectId id;
	
	@Override
	public ObjectId getId() {
		return id;
	}
	@Override
	public void setId(Object id) {
		this.id = (ObjectId) id;
	}
}