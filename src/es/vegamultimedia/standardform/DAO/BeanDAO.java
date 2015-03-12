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
	public abstract void update(T bean) throws SaveException;
	
	/**
	 * Gets an element using its identifier from the database 
	 * @param id Unique bean id
	 * @return
	 */
	public abstract T get(K id);
	
	/**
	 * Gets the first found element from the database whose specified nameField is equal 
	 * to the specified valueField
	 * @param nameField
	 * @param valueField
	 * @return The found element or null if not found
	 */
	public abstract T get(String nameField, Object valueField);
	
	/**
	 * Get all the elements from the database
	 * @return
	 */
	public abstract List<T> getAllElements();
	
	/**
	 * Gets the elements that match a search criteria from the database
	 * @param searchCriteria Array with the search criteria
	 * @return Elements that match the search criteria
	 */
	public abstract List<T> getElements(SearchCriterion[] searchCriteria);
	
	/**
	 * Remove the element from the database
	 * @param element
	 */
	public abstract void remove(T bean);

}