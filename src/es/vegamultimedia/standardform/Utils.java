package es.vegamultimedia.standardform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;

import org.mongodb.morphia.Datastore;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.DAO.BeanJPADAO;
import es.vegamultimedia.standardform.DAO.BeanMongoDAO;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.model.Bean;

abstract public class Utils {
	
	/**
	 * Return the string with the first letter capitalized
	 * @param string
	 * @return
	 */
	public static String capitalizeFirstLetter(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}
	
	/**
	 * Get an BeanDAO instance from a nestedBean class
	 * @param nestedBeanClass
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings("rawtypes")
	public static BeanDAO getBeanDAO(Class<? extends Bean> nestedBeanClass, BeanDAO beanDAOFather)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> classBeanDAO;
		// Obtenemos la anotación StandardForm del bean
		StandardForm anotación = nestedBeanClass.getAnnotation(StandardForm.class);
		// Si el bean tiene anotación nestedBeanClass 
		if (anotación != null && !anotación.beanDAOClassName().isEmpty()) {
			// La anotación es el nombre de la clase del beanDAO
			String nombreClaseBeanDAO = anotación.beanDAOClassName();
			// Obtenemos la clase del BeanDAO
			classBeanDAO = Class.forName(nombreClaseBeanDAO);
		}
		// Si no hay anotación nestedBeanClass
		else {
			// Obtenemos el nombre de la clase del beanDAO en función de la anotación daoType
			switch (anotación.daoType()) {
			case JPA:
				classBeanDAO = BeanJPADAO.class;
				break;
			case MONGO:
				classBeanDAO = BeanMongoDAO.class;
				break;
			default:
				throw new IllegalArgumentException("Tipo de DAO no soportado");
			}
		}
		// Si es un BeanJPADAO o subclase
		try {
			if (classBeanDAO.asSubclass(BeanJPADAO.class) != null) {
				// Obtenemos el constructor del beanJPADAO
				Constructor constructorDAO =
					classBeanDAO.getConstructor(Class.class, EntityManager.class);
				// Creamos el objeto DAO
				return (BeanDAO) constructorDAO.newInstance(nestedBeanClass, ((BeanJPADAO)beanDAOFather).getEntityManager());
			}
		} catch (ClassCastException ignorada) { }
		// Si es un BeanMongoDAO o subclase
		try {
			if (classBeanDAO.asSubclass(BeanMongoDAO.class) != null) {
				// Obtenemos el constructor del beanMongoDAO
				Constructor constructorDAO =
					classBeanDAO.getConstructor(Class.class, Datastore.class);
				// Creamos el objeto DAO
				return (BeanDAO) constructorDAO.newInstance(nestedBeanClass, ((BeanMongoDAO)beanDAOFather).getDatastore());
			}
		} catch (ClassCastException ignorada) { }
		// Si no es BeanJPADAO ni BeanMongoDAO
		throw new ClassNotFoundException("No se ha podido determinar el BeanDAO");
	}
}