package es.vegamultimedia.standardform;

import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.model.Bean;

public class BeanUI<T extends Bean> {
	
	/**
	 * Bean class
	 */
	protected Class<T> beanClass;

	/**
	 * Bean data access object (DAO) for this bean
	 */
	protected BeanDAO<T> beanDAO;
	
	@SuppressWarnings("unchecked")
	public BeanUI(Class<T> beanClass, EntityManager entityManager)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, 
			IllegalAccessException, InvocationTargetException {
		this.beanClass = beanClass;
		beanDAO = Utils.getBeanDAO(beanClass, entityManager);
	}
	
	/**
	 * Returns the standard detail form for the specified element bean
	 * @param element
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public DetailForm<T> getDetailForm(T element) 
			throws InstantiationException, IllegalAccessException {
		return new DetailForm<T>(this, element);
	}
	
	/**
	 * Returns the standard list form for this bean class
	 * @return
	 */
	public ListForm<T> getListForm() {
		return new ListForm<T>(this);
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
	public BeanDAO<T> getBeanDAO() {
		return beanDAO;
	}
}
