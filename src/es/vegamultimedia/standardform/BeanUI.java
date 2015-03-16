package es.vegamultimedia.standardform;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import com.vaadin.ui.Component;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.DAO.BeanDAOException;
import es.vegamultimedia.standardform.DAO.SearchCriterion;
import es.vegamultimedia.standardform.model.Bean;

@SuppressWarnings("serial")
public class BeanUI<T extends Bean, K> implements Serializable {
	
	/**
	 * Bean class
	 */
	protected Class<T> beanClass;

	/**
	 * Bean data access object (DAO) for this bean
	 */
	protected BeanDAO<T, K> beanDAO;
	
	/**
	 * Current search in the list form
	 */
	protected SearchCriterion[] currentSearch;
	
	/**
	 * Current page in the list form
	 */
	protected int currentPage;
	
	public BeanUI(Class<T> beanClass, BeanDAO<T, K> beanDAO)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, 
			IllegalAccessException, InvocationTargetException {
		this.beanClass = beanClass;
		this.beanDAO = beanDAO;
	}
	
	/**
	 * Returns the standard detail form for the specified element bean
	 * @param bean
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Component buildDetailForm(T bean) 
			throws InstantiationException, IllegalAccessException {
		return new DetailForm<T, K>(this, bean);
	}
	
	/**
	 * Returns the standard list form for this bean class
	 * @return
	 * @throws BeanDAOException 
	 */
	public Component buidListForm() throws BeanDAOException {
		return new ListForm<T, K>(this, null);
	}
	
	/**
	 * Returns the bean class
	 * @return
	 */
	public Class<T> getBeanClass() {
		return beanClass;
	}
	
	/**
	 * Returns the beanDAO object for this bean
	 * @return
	 */
	public BeanDAO<T, K> getBeanDAO() {
		return beanDAO;
	}

	public SearchCriterion[] getCurrentSearch() {
		return currentSearch;
	}

	public void setCurrentSearch(SearchCriterion[] currentSearch) {
		this.currentSearch = currentSearch;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
}
