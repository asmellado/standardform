package es.vegamultimedia.standardform.DAO;

import java.io.Serializable;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import es.vegamultimedia.standardform.SaveException;
import es.vegamultimedia.standardform.Utils;
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
}
