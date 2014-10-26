package es.vegamultimedia.standardform.model;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.mongodb.morphia.annotations.Id;

import es.vegamultimedia.standardform.Utils;

/**
 * An object of this class represents a bean that uses MongoDB and Morphia for persistence
 */
@SuppressWarnings("serial")
public abstract class BeanMongo implements Bean, Serializable {
	
	/**
	 * Returns the hashCode of this BeanMongo.
	 * The hashCode of a BeanMongo is the hasCode of the field with the annotation Id
	 * (org.mongodb.morphia.annotations.Id)
	 */
	@Override
	public int hashCode() {
		// Obtenemos todos los campos de la clase
		Field[] fields = getClass().getDeclaredFields();
		// Recorremos los campos
		for (Field field : fields) {
			// Si el campo tiene la anotación id de Morphia
			if (field.getAnnotation(Id.class) != null) {
				try{
					// Obtenemos el valor del campo
					Object valor = Utils.getFieldValue(this, field);
					// Retorna el hashcode del campo
					return valor.hashCode();
				}
				catch(Exception e) {
					// Si hay algún problema (ej: null), retorna 0
					return 0;
				}
			}
		}
		// Si no hay anotación Id
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof BeanMongo))
			return false;
		return (hashCode() == obj.hashCode());
	}

}