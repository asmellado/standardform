package es.vegamultimedia.standardform;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import es.vegamultimedia.standardform.DAO.BeanDAO;
import es.vegamultimedia.standardform.model.Bean;

@SuppressWarnings("serial")
public class BeanUI<T extends Bean> implements Serializable {
	
	/**
	 * Bean class
	 */
	protected Class<T> beanClass;

	/**
	 * Bean data access object (DAO) for this bean
	 */
	protected BeanDAO<T> beanDAO;
	
	public BeanUI(Class<T> beanClass, BeanDAO<T> beanDAO)
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
	public DetailForm<T> getDetailForm(T bean) 
			throws InstantiationException, IllegalAccessException {
		return new DetailForm<T>(this, bean);
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
