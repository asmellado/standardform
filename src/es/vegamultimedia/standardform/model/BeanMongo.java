package es.vegamultimedia.standardform.model;

import java.io.Serializable;

import es.vegamultimedia.standardform.Utils;

/**
 * An object of this class represents a bean that uses MongoDB and Morphia for persistence
 */
@SuppressWarnings("serial")
public abstract class BeanMongo implements Bean, Serializable {
	
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
		if (obj==null || !(obj instanceof BeanMongo))
			return false;
		return (hashCode() == obj.hashCode());
	}
}