package es.vegamultimedia.standardform.DAO;

import java.io.Serializable;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;

import es.vegamultimedia.standardform.Utils;
import es.vegamultimedia.standardform.model.Bean;
import es.vegamultimedia.standardform.model.BeanMongo;

@SuppressWarnings("serial")
public class BeanMongoDAO<BEAN extends BeanMongo, KEY> extends BasicDAO<BEAN, KEY>
	implements BeanDAO<BEAN, KEY>, Serializable {
	
	// Bean class
	protected Class<BEAN> beanClass;
	
	// Datastore
	protected Datastore datastore;

	/**
	 * Create an instance of this Data Object Access (DAO)
	 * @param beanClass
	 * @param datastore
	 */
	public BeanMongoDAO(Class<BEAN> beanClass, Datastore datastore) {
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
	public void insert(BEAN bean) throws BeanDAOException {
		// Comprobamos que NO existe un documento con el mismo id
		Object id;
		BEAN existingBean = null;
		try {
			id = Utils.getId(bean);
			Query<BEAN> query = createQuery().field("_id").equal(id);
			existingBean = query.get();
		} catch (Exception e) {
			// No debería ocurrir nunca
			e.printStackTrace();
		}
		if (existingBean != null) {
			throw new BeanDAOException("Ya existe un elemento con la misma clave");
		}
		datastore.save(bean);
	}
	
	@Override
	public void update(BEAN bean) {
		datastore.save(bean);
	}
	
	@Override
	public BEAN get(KEY id) {
		return datastore.get(beanClass, id);
	}

	@Override
	public BEAN get(String nameField, Object valueField) {
		Query<BEAN> query = createQuery();
		query.field(nameField).equal(valueField);
		return query.get();
	}
	
	@Override
	public void remove(BEAN bean) {
		datastore.delete(bean);
	}

	@Override
	public long getcountElements(SearchCriterion[] searchCriteria)
			throws BeanDAOException {
		Query<BEAN> query = getQuery(searchCriteria);
		return query.countAll();
	}
	
	@Override
	public List<BEAN> getElements(SearchCriterion[] searchCriteria, int firstResult, int limitResult) {
		Query<BEAN> query = getQuery(searchCriteria);
		// Añadimos el primer y número límite de resultados
		query.offset(firstResult);
		query.limit(limitResult);
		// Obtenemos los elementos que cumplen la query
		return query.asList();
	}

	/**
	 * Returns a Morphia query with the searchCriteria
	 * @param searchCriteria
	 * @return
	 */
	private Query<BEAN> getQuery(SearchCriterion[] searchCriteria) {
		// Creamos una query
		Query<BEAN> query = createQuery();
		// Inicializamos un array de criterios de Morphia
		Criteria[] morphiaCriteria = new Criteria[searchCriteria.length];
		// Recorremos los criterios de búsqueda
		for (int i=0; i<searchCriteria.length; i++) {
			String nameField = searchCriteria[i].getNameField();
			Object valueField = searchCriteria[i].getValueField();
			// Si es de tipo texto
			switch (searchCriteria[i].getTypeCriteria()) {
			case TEXT:
				// El campo debe contener el valor de tipo String (ignorando mayúsculas)
				String valueString = valueField.toString();
				morphiaCriteria[i] = query.criteria(nameField).containsIgnoreCase(valueString);
				break;
			// Si es de tipo enumerado
			case ENUM:
				// El campo debe ser igual al valor seleccionado
				morphiaCriteria[i] = query.criteria(nameField).equal(valueField);
				break;
			// Si es de tipo bean
			case BEAN:
				try {
					// Obtenemos el valor del campo id del bean
					Object id = Utils.getId((Bean) valueField);
					// El campo debe ser igual al id
					morphiaCriteria[i] = query.criteria(nameField).equal(id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// Añadimos a la query con el operador AND todos los criterios
		query.and(morphiaCriteria);
		return query;
	}
}