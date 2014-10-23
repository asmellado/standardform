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
	 * If you use a BeanJPADAO or BeanMongoDAO, you don't specify it (standard form obtains the name depending on the DAOType)
	 * @return
	 */
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
	 * Visibled columns in the table in the correct order. Each element must math with a POJO's field
	 * If empty, the table shows every field in the same order.
	 * @return
	 */
	public String[] columns() default {""};
}