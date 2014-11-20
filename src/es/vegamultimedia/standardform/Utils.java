package es.vegamultimedia.standardform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.persistence.EntityManager;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Id;

import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.DAO.BeanJPADAO;
import es.vegamultimedia.standardform.DAO.BeanMongoDAO;
import es.vegamultimedia.standardform.annotations.StandardForm;
import es.vegamultimedia.standardform.model.Bean;
import es.vegamultimedia.standardform.model.BeanMongo;
import es.vegamultimedia.standardform.model.BeanWithId;

abstract public class Utils {
	
	/**
	 * Gets every fields of the current bean, adding every fields of every superclass
	 * @param currentBean
	 * @return
	 */
	public static java.lang.reflect.Field[] getBeanFields(Class<? extends Bean> beanClass) {
		// Obtenemos los campos del bean elementoActual
		java.lang.reflect.Field[] currentBeanFields = beanClass.getDeclaredFields();
		// Añadimos los campos de las superclases hasta llegar a Object
		Class<?> superclass = beanClass.getSuperclass();
		while (superclass != Object.class) {
			java.lang.reflect.Field[] fields = superclass.getDeclaredFields();
			ArrayList<java.lang.reflect.Field> beanFieldsList = new ArrayList<java.lang.reflect.Field>();
			beanFieldsList.addAll(Arrays.asList(currentBeanFields));
			for (java.lang.reflect.Field field : fields) {
				beanFieldsList.add(field);
			}
			currentBeanFields = beanFieldsList.toArray(new java.lang.reflect.Field[beanFieldsList.size()]);
			superclass = superclass.getSuperclass();
		}
		return currentBeanFields;
	}
	
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
				// TODO Podría ser un BeanMongoDAO personalizado que no tuviera este constructor
				// En ese caso, lanzaría una excepción NoSuchMethodException
				try {
					// Obtenemos el constructor del beanMongoDAO
					Constructor constructorDAO =
						classBeanDAO.getConstructor(Class.class, Datastore.class);
					// Creamos el objeto DAO
					return (BeanDAO) constructorDAO.newInstance(nestedBeanClass, ((BeanMongoDAO)beanDAOFather).getDatastore());
				}
				catch (NoSuchMethodException ignorada) { }
				try {
					// Si no tiene el constructor genérico, buscamos si tiene constructo con sólo Datastore
					Constructor constructorDAO =
						classBeanDAO.getConstructor(Datastore.class);
					// Creamos el objeto DAO
					return (BeanDAO) constructorDAO.newInstance(((BeanMongoDAO)beanDAOFather).getDatastore());
				}
				catch (NoSuchMethodException ignorada) { }
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
			// Obtenemos todos los campos del bean
			Field[] fields = getBeanFields(bean.getClass());
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
	
	/**
	 * Returns true if the subClass is a subclass of the superClass
	 * @param superClass
	 * @param subClass
	 * @return
	 */
	public static boolean isSubClass(Class<?> superClass, Class<?> subClass) {
		try {
			if (superClass.asSubclass(subClass) != null) {
				return true;
			}
		} catch (ClassCastException ignorada) { }
		return false;
	}
	
	/**
	 * Returns the parametrized type of a generic field
	 * @param currentField
	 * @return
	 */
	public static Type getParametrizedType(Field currentField) {
		// Obtenemos la clase parametrizada del arrayList
		java.lang.reflect.Type genericType = currentField.getGenericType();
		// El tipo de elementos es el primer (y único) tipo parametrizado del array de tipos
		java.lang.reflect.Type[] parameterizedtypes =
				((ParameterizedType)genericType).getActualTypeArguments();
		return parameterizedtypes[0];
	}
	
	/**
     * Find a Component form his root component and its id.
     * This method is recursive for nested components.
     * @param root
     * @param id
     * @return
     */
	public static Component findComponentById(HasComponents root, String id) {
        Iterator<Component> iterate = root.iterator();
        while (iterate.hasNext()) {
            Component c = iterate.next();
            if (id.equals(c.getId())) {
                return c;
            }
            if (c instanceof HasComponents) {
                Component cc = findComponentById((HasComponents) c, id);
                if (cc != null)
                    return cc;
            }
        }

        return null;
    }
	
	/**
     * Iterate all subcomponents of a root component.
     * This method is recursive for nested components.
	 * @param root Root component
	 * @param disableSubcomponents If is true, disable all components.
	 * @param showHiddenComponents If is true, show all hidden components.
	 * @return
	 */
	protected static void iterateSubComponents(HasComponents root,
			boolean disableSubcomponents, boolean showHiddenComponents) {
        Iterator<Component> iterate = root.iterator();
        while (iterate.hasNext()) {
            Component c = iterate.next();
            if (disableSubcomponents)
                c.setEnabled(false);
            if (showHiddenComponents)
            	c.setVisible(true);
            if (c instanceof HasComponents) {
                iterateSubComponents((HasComponents) c, disableSubcomponents, showHiddenComponents);
            }
        }
    }
}