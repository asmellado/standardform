package es.vegamultimedia.standardform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.annotations.StandardForm;

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
	public static BeanDAO getBeanDAO(Class<Object> nestedBeanClass, EntityManager entityManager) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		String NombreClaseBeanDAO;
		// Obtenemos la anotación StandardForm del bean
		StandardForm anotación = nestedBeanClass.getAnnotation(StandardForm.class);
		// Si el bean tiene anotación nestedBeanClass 
		if (anotación != null && !anotación.beanDAOClassName().isEmpty()) {
			// La anotación es el nombre de la clase del beanDAO
			NombreClaseBeanDAO = anotación.beanDAOClassName();
		}
		// Si no hay anotación nestedBeanClass
		else {
			// Calculamos el nombre de la clase del BeanDAO
			// Sustituyendo en el paquete del bean ".model" por ".dao"
			// Y añadiendo "DAO" al nombre de la clase del bean
			NombreClaseBeanDAO =
					nestedBeanClass.getPackage().getName().replace(".model", ".dao.") +
					nestedBeanClass.getSimpleName() + "DAO";
		}
		// Obtenemos la clase del BeanDAO
		Class<?> classBeanDAO = Class.forName(NombreClaseBeanDAO);
		// Obtenemos el constructor del beanDAO
		Constructor constructorDAO =
			classBeanDAO.getConstructor(Class.class, EntityManager.class);
		// Creamos el objeto DAO
		return (BeanDAO)constructorDAO.newInstance(nestedBeanClass, entityManager);
	}
}