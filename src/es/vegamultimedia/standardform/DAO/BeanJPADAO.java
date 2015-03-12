package es.vegamultimedia.standardform.DAO;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.QueryTimeoutException;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import es.vegamultimedia.standardform.SaveException;
import es.vegamultimedia.standardform.model.BeanJPA;

public class BeanJPADAO<T extends BeanJPA, K> implements BeanDAO<T, K>{
	
	// Bean class
	protected Class<T> beanClass;
	
	// EntityManager
	protected EntityManager entityManager;

	/**
	 * Create an instance of this Data Object Access (DAO)
	 * @param beanClass
	 * @param entityManager
	 */
	public BeanJPADAO(Class<T> beanClass, EntityManager entityManager) {
		this.beanClass = beanClass;
		this.entityManager = entityManager;
	}
	
	/**
	 * Get the entity manager
	 * @return
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}
	
	@Override
	public void insert(T bean) throws SaveException {
		update(bean);
	}
	
	@Override
	public void update(T bean)
		throws IllegalStateException, EntityExistsException,
			IllegalArgumentException, RollbackException, PersistenceException, SaveException {
		EntityTransaction transaction = null;
		try {
			transaction = entityManager.getTransaction();
			transaction.begin();
			entityManager.persist(bean);
//			// We must persist every join
//			for (Field field : Utils.getBeanFields(beanClass)){
//				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
//				if (oneToMany != null) {
//					Object value = Utils.getFieldValue(bean, field);
//					entityManager.persist(value);
//				}
//			}
			transaction.commit();
			transaction = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SaveException(e.getMessage(), e.getCause());
		}
		finally {
			if (transaction!=null) {
				try {
					transaction.rollback();
				}
				catch(Exception ignorada) {}
			}
		}
	}
	
	@Override
	public T get(Object id) {
		return entityManager.find(beanClass, id);
	}
	
	@Override
	public T get(String nameField, Object valueField) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(beanClass);
		Root<T> root = criteriaQuery.from(beanClass);
		Predicate predicate = criteriaBuilder.equal(root.get(nameField), valueField);
		TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery.where(predicate));
		try {
			return typedQuery.getSingleResult();
		}
		catch(NoResultException e) {
			return null;
		}
		catch(NonUniqueResultException e) {
			return typedQuery.getResultList().get(0);
		}
	}
	
	@Override
	public List<T> getAllElements()
		throws IllegalArgumentException, IllegalStateException, QueryTimeoutException,
			TransactionRequiredException, PessimisticLockException, LockTimeoutException,
			PersistenceException {
		return getElements(new SearchCriterion[]{});
	}

	@Override
	public void remove(T bean)
			throws IllegalStateException, IllegalArgumentException,
				TransactionRequiredException, RollbackException {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
    	entityManager.remove(bean);
    	transaction.commit();
	}

	@Override
	public List<T> getElements(SearchCriterion[] searchCriteria) {
		// Creamos la query usando Criteria API
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(beanClass);
		Root<T> root = criteriaQuery.from(beanClass);
		// Inicializamos un array de Predicates
		Predicate[] predicates = new Predicate[searchCriteria.length];
		// Recorremos los criterios de búsqueda
		for (int i=0; i<searchCriteria.length; i++) {
			String nameField = searchCriteria[i].getNameField();
			Object valueField = searchCriteria[i].getValueField();
			switch (searchCriteria[i].getTypeCriteria()) {
			case TEXT:
				predicates[i] =
					criteriaBuilder.like(root.<String>get(nameField), "%" + valueField + "%");
				break;
			case ENUM: case BEAN:
				predicates[i] = criteriaBuilder.equal(root.get(nameField), valueField);
				break;
			}
		}
		criteriaQuery = criteriaQuery.where(criteriaBuilder.and(predicates));
		TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}
}
