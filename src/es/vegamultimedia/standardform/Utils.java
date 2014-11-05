package es.vegamultimedia.standardform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Id;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.DAO.BeanJPADAO;
import es.vegamultimedia.standardform.DAO.BeanMongoDAO;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.model.Bean;
import es.vegamultimedia.standardform.model.BeanMongo;
import es.vegamultimedia.standardform.model.BeanWithId;

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
	 * Returns the "get" method for a bean field (or the "is" method if the field is boolean) 
	 * @param beanClass
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Method getGetMethod(Class<? extends Bean> beanClass, Field field)
			throws NoSuchMethodException, SecurityException {
		// Obtenemos el nombre del campo y ponemos la primera letra en mayúscula
		String nombreCampo = Utils.capitalizeFirstLetter(field.getName());
		// Si el campo es boolean
		if (field.getType() == Boolean.TYPE || field.getType() == Boolean.class) {
			// Obtenemos el método "is" del campo actual
			return beanClass.getMethod("is"+nombreCampo);
		}
		else {
			// Obtenemos el método "get" del campo actual
			return beanClass.getMethod("get"+nombreCampo);
		}
	}
	
	/**
	 * Returns the "set" method for a bean field
	 * @param beanClass
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Method getSetMethod(Class<? extends Bean> beanClass, Field field)
			throws NoSuchMethodException, SecurityException {
		// Obtenemos el nombre del campo y ponemos la primera letra en mayúscula
		String nombreCampo = Utils.capitalizeFirstLetter(field.getName());
		// Obtenemos el método "set" del campo actual
		return beanClass.getMethod("set"+nombreCampo, field.getType());
	}
	
	/**
	 * Returns the value of the field of a bean (calling to the getMethod)
	 * @param element
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object getFieldValue(Bean element, Field field)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Obtenemos el método get del campo
		Method getMethod = Utils.getGetMethod(element.getClass(), field);
		// Obtenemos el valor del campo llamando al método get
		return getMethod.invoke(element);
	}
	
	/**
	 * Sets the value to the specified field of a bean (calling to the setMethod)
	 * @param element
	 * @param field
	 * @param value
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static void setFieldValue(Bean element, Field field, Object value)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Obtenemos el método get del campo
		Method setMethod = Utils.getSetMethod(element.getClass(), field);
		// Obtenemos el valor del campo llamando al método get
		setMethod.invoke(element, value);
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
				// TODO Podría ser un BeanMongoDAO personalizado que no tuviera este constructor
				// En ese caso, lanzaría una excepción NoSuchMethodException
				Constructor constructorDAO =
					classBeanDAO.getConstructor(Class.class, Datastore.class);
				// Creamos el objeto DAO
				return (BeanDAO) constructorDAO.newInstance(nestedBeanClass, ((BeanMongoDAO)beanDAOFather).getDatastore());
			}
		} catch (ClassCastException ignorada) { }
		// Si no es BeanJPADAO ni BeanMongoDAO
		throw new ClassNotFoundException("No se ha podido determinar el BeanDAO");
	}
	
	/**
	 * Returns the value of the id field of a bean
	 * @param bean
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object getId(Bean bean) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Si el bean tiene campo id
		if (bean instanceof BeanWithId) {
			// Retornamoes el valor del id
			return ((BeanWithId)bean).getId();
		}
		// Si el bean es un BeanMongo sin campo id
		else if (bean instanceof BeanMongo) {
			// Obtenemos todos los campos de la clase
			Field[] fields = bean.getClass().getDeclaredFields();
			// Recorremos los campos
			for (Field field : fields) {
				// Si el campo tiene la anotación Id de Morphia
				if (field.getAnnotation(Id.class) != null) {
					// Retornamos el valor del campo
					return getFieldValue(bean, field);
				}
			}
			throw new IllegalArgumentException("No se encuentra un identificador para el elemento");
		}
		throw new IllegalArgumentException("Tipo de bean no soportado");
	}
}