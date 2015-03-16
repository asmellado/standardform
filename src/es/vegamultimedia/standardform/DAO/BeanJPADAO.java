package es.vegamultimedia.standardform.DAO;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import es.vegamultimedia.standardform.model.BeanJPA;

public class BeanJPADAO<BEAN extends BeanJPA, KEY> implements BeanDAO<BEAN, KEY>{
	
	// Bean class
	protected Class<BEAN> beanClass;
	
	// EntityManager
	protected EntityManager entityManager;

	/**
	 * Create an instance of this Data Object Access (DAO)
	 * @param beanClass
	 * @param entityManager
	 */
	public BeanJPADAO(Class<BEAN> beanClass, EntityManager entityManager) {
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
	public void insert(BEAN bean) throws BeanDAOException {
		update(bean);
	}
	
	@Override
	public void update(BEAN bean)
		throws IllegalStateException, EntityExistsException,
			IllegalArgumentException, RollbackException, PersistenceException, BeanDAOException {
		EntityTransaction transaction = null;
		try {
			transaction = entityManager.getTransaction();
			transaction.begin();
			entityManager.persist(bean);
			transaction.commit();
			transaction = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BeanDAOException(e.getMessage(), e.getCause());
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
	public BEAN get(KEY id) {
		return entityManager.find(beanClass, id);
	}
	
	@Override
	public BEAN get(String nameField, Object valueField) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BEAN> criteriaQuery = criteriaBuilder.createQuery(beanClass);
		Root<BEAN> root = criteriaQuery.from(beanClass);
		Predicate predicate = criteriaBuilder.equal(root.get(nameField), valueField);
		TypedQuery<BEAN> typedQuery = entityManager.createQuery(criteriaQuery.where(predicate));
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
	public void remove(BEAN bean)
			throws IllegalStateException, IllegalArgumentException,
				TransactionRequiredException, RollbackException {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
    	entityManager.remove(bean);
    	transaction.commit();
	}
	
	@Override
	public long getcountElements(SearchCriterion[] searchCriteria)
			throws BeanDAOException {
		// Creamos la query usando Criteria API
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<BEAN> root = criteriaQuery.from(beanClass);
		criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));
		Predicate[] predicates =
				getJPAPredicates(searchCriteria, criteriaBuilder, root);
		criteriaQuery.where(criteriaBuilder.and(predicates));
		TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
		return typedQuery.getSingleResult();
	}

	@Override
	public List<BEAN> getElements(SearchCriterion[] searchCriteria, int firstResult, int limitResult) {
		// Creamos la query usando Criteria API
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BEAN> criteriaQuery = criteriaBuilder.createQuery(beanClass);
		Root<BEAN> root = criteriaQuery.from(beanClass);
		Predicate[] predicates =
				getJPAPredicates(searchCriteria, criteriaBuilder, root);
		criteriaQuery.where(criteriaBuilder.and(predicates));
		TypedQuery<BEAN> typedQuery = entityManager.createQuery(criteriaQuery);
		// Añadimos el primer y número límite de resultados
		typedQuery.setFirstResult(firstResult);
		typedQuery.setMaxResults(limitResult);
		// Obtenemos los elementos que cumplen la query
		return typedQuery.getResultList();
	}
	
	/**
	 * Gets the JPA predicates from the searchCriteria
	 * You can override this method if you need to modify or add some criteria to the query
	 * @param searchCriteria
	 * @param criteriaBuilder
	 * @param root
	 * @return predicates if there is some searchCriteria, otherwise an empty array
	 */
	protected Predicate[] getJPAPredicates(SearchCriterion[] searchCriteria,
			CriteriaBuilder criteriaBuilder, Root<BEAN> root) {
		if (searchCriteria != null) {
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
			return predicates;
		}
		return new Predicate[0];
	}
}