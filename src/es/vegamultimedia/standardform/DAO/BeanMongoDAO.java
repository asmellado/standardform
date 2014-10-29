package es.vegamultimedia.standardform.DAO;

import java.io.Serializable;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import es.vegamultimedia.standardform.model.BeanMongo;

@SuppressWarnings("serial")
public class BeanMongoDAO<T extends BeanMongo> implements BeanDAO<T>, Serializable {
	
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
	public void save(T element) {
		datastore.save(element);
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
	public void remove(T element) {
		datastore.delete(element);
	}

}
