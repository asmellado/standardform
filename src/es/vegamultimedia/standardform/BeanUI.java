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
	
	public static int DEFAULT_ELEMENTS_PER_PAGE = 15;
	
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
	 * Current first element in the list form
	 */
	protected int firstElement;
	
	/**
	 * Current number of elements per page in the list form
	 */
	protected int elementsPerPage;
	
	public BeanUI(Class<T> beanClass, BeanDAO<T, K> beanDAO)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, 
			IllegalAccessException, InvocationTargetException {
		this.beanClass = beanClass;
		this.beanDAO = beanDAO;
		elementsPerPage = DEFAULT_ELEMENTS_PER_PAGE;
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
		return new ListForm<T, K>(this);
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

	public int getFirstElement() {
		return firstElement;
	}

	public void setFirstElement(int firstElement) {
		this.firstElement = firstElement;
	}

	public int getElementsPerPage() {
		return elementsPerPage;
	}

	public void setElementsPerPage(int elementsPerPage) {
		this.elementsPerPage = elementsPerPage;
	}
}