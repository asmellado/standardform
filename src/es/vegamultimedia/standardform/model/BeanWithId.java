package es.vegamultimedia.standardform.model;

/**
 * An object that implements this interface represents a bean with a id field, the unique key
 */
public interface BeanWithId extends Bean {
	
	public abstract Object getId();
	
	public abstract void setId(Object id);

}