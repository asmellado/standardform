package es.vegamultimedia.standardform.DAO;

import java.io.Serializable;
import java.util.List;

import es.vegamultimedia.standardform.model.Bean;

public interface BeanDAO<BEAN extends Bean, KEY> extends Serializable {
	
	/**
	 * Inserts the element T in the database
	 * @param element
	 * @throws BeanDAOException 
	 */
	public abstract void insert(BEAN bean) throws BeanDAOException;
	
	/**
	 * Updates the element T in the database
	 * @param element
	 */
	public abstract void update(BEAN bean) throws BeanDAOException;
	
	/**
	 * Gets an element using its identifier from the database 
	 * @param id Unique bean id
	 * @return
	 */
	public abstract BEAN get(KEY id) throws BeanDAOException;
	
	/**
	 * Gets the first found element from the database whose specified nameField is equal 
	 * to the specified valueField
	 * @param nameField
	 * @param valueField
	 * @return The found element or null if not found
	 */
	public abstract BEAN get(String nameField, Object valueField) throws BeanDAOException;
	
	/**
	 * Returns the number of elements that matches the searchCriteria from the database
	 * @param searchCriteria
	 * @return
	 * @throws BeanDAOException
	 */
	public abstract long getcountElements(SearchCriterion[] searchCriteria) throws BeanDAOException;
	
	/**
	 * Gets the elements that match a search criteria from the database
	 * @param searchCriteria Array with the search criteria or null if there are no criteria
	 * @param firstResult position of the first result to retrieve
	 * @param limitResult maximum number of results to retrieve
	 * @return Elements that match the search criteria with the specified firstResult and limitResult
	 */
	public abstract List<BEAN> getElements(SearchCriterion[] searchCriteria,
			int firstResult, int limitResult) throws BeanDAOException;
	
	/**
	 * Remove the element from the database
	 * @param element
	 */
	public abstract void remove(BEAN bean) throws BeanDAOException;

}