package es.vegamultimedia.standardform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * You must use this annotation for every bean fieldif the bean field must be shown in the standard form
 * Metadata for a field in the standard form 
 * You also can add metadata to every bean field
 * @author alejandro
 *
 */
public @interface StandardFormField {
	enum Type {DEFAULT, TEXT_FIELD, TEXT_AREA, COMBO_BOX, CHECK_BOX, OPTION_GROUP, DATE}
	/**
	 * It indicates weather the field must be shown in the form of not
	 * If you don't specify it, the field will be shown 
	 * @return
	 */
	public boolean showField() default true;
	/**
	 * Field type. If you don't specify one type, the type will be Type.DEFAULT.
	 * If the type is DEFAULT, Standard Form will assign the type automatically depending on the bean type 
	 * @return
	 */
	public Type type() default Type.DEFAULT;
	/**
	 * Caption showed in the field in the standard form
	 * If you don't specify, the caption will be the field name with the first letter capitalized
	 * @return
	 */
	public String caption() default "";
	/**
	 * Optional help showed bellow the field
	 * @return
	 */
	public String help() default "";
}