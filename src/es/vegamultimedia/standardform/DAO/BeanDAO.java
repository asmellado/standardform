package es.vegamultimedia.standardform.DAO;

import java.util.List;

import es.vegamultimedia.standardform.model.Bean;

public interface BeanDAO<T extends Bean> {
	
	/**
	 * Inserts the element T in the database
	 * @param element
	 */
	public abstract void insert(T bean);
	
	/**
	 * Updates the element T in the database
	 * @param element
	 */
	public abstract void update(T bean);
	
	/**
	 * Get an element from the database 
	 * @param id Unique bean id
	 * @return
	 */
	public abstract T get(Object id);
	
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