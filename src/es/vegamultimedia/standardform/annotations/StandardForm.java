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
	 * BeanDAO Class name, qualified name containing package
	 * If empty, standard form try to get the name replacing ".model" for ".dao" from the package
	 * and adding "DAO" to the bean class name
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
	 * 
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