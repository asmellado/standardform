package es.vegamultimedia.standardform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * Metadata used to generate the standard forms list and detail
 * If you want to generate the standard forms for a bean you must include this annotation to the class
 * @author alejandro
 *
 */
public @interface StandardForm {
	
	/**
	 * Supported data access object (DAO) types
	 */
	enum DAOType {JPA, MONGO};
	
	/**
	 * It specifies the data access object (DAO) type.
	 */
	public DAOType daoType();
	
	/**
	 * BeanDAO Class name, qualified name containing package
	 * You only need to specify it if the bean uses a custom BeanDAO
	 * If you use a BeanJPADAO or BeanMongoDAO, you don't specify it 
	 * (standard form obtains the name depending on the DAOType)
	 * @return
	 */
	
	// TODO Este campo no es necesario, pues el beanUI contiene ya el beanDAO.
	public String beanDAOClassName() default "";
	
	/**
	 * List View Name showed in the URL
	 * @return
	 */
	public String listViewName();
	
	/**
	 * Detail View Name showed in the URL
	 * @return
	 */
	public String detailViewName();
	
	/**
	 * It indicates if it's allowed to create new elements of the POJO 
	 */
	public boolean allowsAdding() default true;
	
	/**
	 * It indicates if it's allowed to edit the elements of the POJO 
	 */
	public boolean allowsEditing() default true;
	
	/**
	 * It indicates if it's allowed to delete elements of the POJO
	 */
	public boolean allowsDeleting() default true;
	
	/**
	 * Row list component class: qualified name containing package.
	 * If you don't specify it, ListForm use a table to show every elements in the list.
	 * If you specify it, you must implement a component class that must inherit CustomField<BeanType>
	 * and you must create a constructor CustomComponent(Bean bean) because ListForm calls this constructor for
	 * every row of the list.
	 * @return
	 */
	public String customRowListComponent() default "";
	 
	/**
	 * Visibled columns in the table in the correct order. Each element must math with a POJO's field
	 * If empty, the table shows every field in the same order.
	 * @return
	 */
	public String[] columns() default {""};
	
	/**
	 * Showed fields above the list to make searches.
	 * The user could fill in information in these fields to make searches and filter 
	 * the registers showed in the list.
	 * Each array element must math with a POJO's field.
	 * If empty, it's not possible to make searches.
	 * @return
	 */
	public String[] searchFields() default {};
	
	/**
	 * It indicates if it searches immediately or the user must press search button 
	 * @return
	 */
	public boolean immediateSearch() default true;

	public Class<? extends Exception>[] catchSaveExceptions() default {};
}