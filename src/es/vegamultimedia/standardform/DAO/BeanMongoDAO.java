package es.vegamultimedia.standardform.DAO;

import java.io.Serializable;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;

import es.vegamultimedia.standardform.SaveException;
import es.vegamultimedia.standardform.Utils;
import es.vegamultimedia.standardform.DAO.SearchCriterion.SearchType;
import es.vegamultimedia.standardform.model.BeanMongo;

@SuppressWarnings("serial")
public class BeanMongoDAO<T extends BeanMongo, K> extends BasicDAO<T, K>
	implements BeanDAO<T, K>, Serializable {
	
	// Bean class
	protected Class<T> beanClass;
	
	// Datastore
	protected Datastore datastore;

	/**
	 * Create an instance of this Data Object Access (DAO)
	 * @param beanClass
	 * @param datastore
	 */
	public BeanMongoDAO(Class<T> beanClass, Datastore datastore) {
		super(beanClass, datastore);
		this.beanClass = beanClass;
		this.datastore = datastore;
	}
	
	/**
	 * Get the Datastore
	 * @return
	 */
	public Datastore getDatastore() {
		return datastore;
	}
	
	@Override
	public void insert(T bean) throws SaveException {
		// Comprobamos que NO existe un documento con el mismo id
		Object id;
		T existingBean = null;
		try {
			id = Utils.getId(bean);
			Query<T> query = createQuery().field("_id").equal(id);
			existingBean = query.get();
		} catch (Exception e) {
			// No debería ocurrir nunca
			e.printStackTrace();
		}
		if (existingBean != null) {
			throw new SaveException("Ya existe un elemento con la misma clave");
		}
		datastore.save(bean);
	}
	
	@Override
	public void update(T bean) {
		datastore.save(bean);
	}
	
	@Override
	public T get(Object id) {
		return datastore.get(beanClass, id);
	}
	
	@Override
	public List<T> getAllElements() {
		Query<T> query = datastore.find(beanClass);
		return query.asList();
	}
	
	@Override
	public void remove(T bean) {
		datastore.delete(bean);
	}

	@Override
	public List<T> getElements(SearchCriterion[] searchCriteria) {
		// Creamos una query
		Query<T> query = createQuery();
		// Inicializamos un array de criterios de Morphia
		Criteria[] morphiaCriteria = new Criteria[searchCriteria.length];
		// Recorremos los criterios de búsqueda
		for (int i=0; i<searchCriteria.length; i++) {
			String nameField = searchCriteria[i].getNameField();
			Object valueField = searchCriteria[i].getValueField();
			// Si es de tipo texto
			if (searchCriteria[i].getTypeCriteria() == SearchType.TEXT) {
				// El campo debe contener el valor de tipo String
				// TODO Hacer "case insensitive"
				// NO funciona con Morphia poner entre "/" y "/i" para que sea "case insensitive"
//				String valueString = "/" + valueField.toString() + "/i";
				String valueString = valueField.toString();
				morphiaCriteria[i] = query.criteria(nameField).contains(valueString);
			}
		}
		// Añadimos a la query con el operador AND todos los criterios
		query.and(morphiaCriteria);
		// Obtenemos los elementos que cumplen la query
		return query.asList();
	}
}
