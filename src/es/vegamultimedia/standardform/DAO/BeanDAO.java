package es.vegamultimedia.standardform.DAO;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockTimeoutException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;

import es.vegamultimedia.standardform.model.Bean;

public abstract class BeanDAO<T extends Bean> {
	
	// Bean class
	protected Class<T> beanClass;
	
	// EntityManager
	protected EntityManager entityManager;
	
	/**
	 * Create an instance of this Data Object Access (DAO)
	 * @param beanClass
	 * @param entityManager
	 */
	public BeanDAO(Class<T> beanClass, EntityManager entityManager) {
		this.beanClass = beanClass;
		this.entityManager = entityManager;
	}
	
	/**
	 * Save el element T in the database
	 * @param element
	 * @throws IllegalStateException
	 * @throws EntityExistsException
	 * @throws IllegalArgumentException
	 * @throws RollbackException
	 * @throws PersistenceException
	 */
	public void save(T element)
		throws IllegalStateException, EntityExistsException,
			IllegalArgumentException, RollbackException, PersistenceException {
		EntityTransaction transaction = null;
		try {
			transaction = entityManager.getTransaction();
			transaction.begin();
			entityManager.persist(element);
			transaction.commit();
			transaction = null;
		}
		finally {
			if (transaction!=null) {
				transaction.rollback();
			}
		}
	}
	
	/**
	 * Get all the elements from the database
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws QueryTimeoutException
	 * @throws TransactionRequiredException
	 * @throws PessimisticLockException
	 * @throws LockTimeoutException
	 * @throws PersistenceException
	 */
	@SuppressWarnings("unchecked")
	public List<T> getAllElements()
		throws IllegalArgumentException, IllegalStateException, QueryTimeoutException,
			TransactionRequiredException, PessimisticLockException, LockTimeoutException,
			PersistenceException {
		String consulta = "SELECT e FROM " + beanClass.getSimpleName() + " e";
		Query query = entityManager.createQuery(consulta);
		return query.getResultList();
	}
	
	/**
	 * Remove the element from the database
	 * @param element
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 * @throws TransactionRequiredException
	 * @throws RollbackException
	 */
	public void remove(T element)
			throws IllegalStateException, IllegalArgumentException,
				TransactionRequiredException, RollbackException {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
    	entityManager.remove(element);
    	transaction.commit();
	}
}
