package es.vegamultimedia.standardform.model;

import es.vegamultimedia.standardform.Utils;

/**
 * An object of this class represents an Entity of Morphia
 * This class overrides the methods hashCode() and equals(). So, it's possible to include objects
 * of this class inside a BeanItemContainer
 */
public abstract class BeanMongoEntity implements BeanMongo {
	
	private static final long serialVersionUID = -563064858467526371L;

	/**
	 * Returns the hashCode of this BeanMongo.
	 * The hashCode of a BeanMongo is the hasCode of the id field with the annotation Id
	 * (org.mongodb.morphia.annotations.Id)
	 */
	@Override
	public int hashCode() {
		try {
			// Obtenemos el valor del campo id
			Object valor = Utils.getId(this);
			// Retorna el hashcode del campo
			return valor.hashCode();
		} catch (Exception e) {
			// Si hay algún problema (ej: null), retorna 0
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof BeanMongoEntity))
			return false;
		return (hashCode() == obj.hashCode());
	}
}