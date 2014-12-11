package es.vegamultimedia.standardform.DAO;

import java.util.List;

import es.vegamultimedia.standardform.SaveException;
import es.vegamultimedia.standardform.model.Bean;

public interface BeanDAO<T extends Bean, K> {
	
	/**
	 * Inserts the element T in the database
	 * @param element
	 * @throws SaveException 
	 */
	public abstract void insert(T bean) throws SaveException;
	
	/**
	 * Updates the element T in the database
	 * @param element
	 */
	public abstract void update(T bean);
	
	/**
	 * Get an element using its identifier from the database 
	 * @param id Unique bean id
	 * @return
	 */
	public abstract T get(K id);
	
	/**
	 * Get all the elements from the database
	 * @return
	 */
	public abstract List<T> getAllElements();
	
	/**
	 * Remove the element from the database
	 * @param element
	 */
	public abstract void remove(T bean);

}