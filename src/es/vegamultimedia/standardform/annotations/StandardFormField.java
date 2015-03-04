package es.vegamultimedia.standardform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * You must use this annotation for every bean field if the bean field must be shown in the standard form
 * Metadata for a field in the standard form 
 * You also can add metadata to every bean field
 * @author alejandro
 *
 */
public @interface StandardFormField {
	enum Type {DEFAULT, TEXT_FIELD, NUM_FIELD, TEXT_AREA, COMBO_BOX,
		CHECK_BOX, OPTION_GROUP, MULTIPLE_SELECTION, DATE, DATETIME, TIME,
		SEARCH, MULTIPLE_SEARCH, EMBEDDED, TABLE, FILE, IMAGE, MONGO_ID, NONE}
	
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
	 * Specifies if DetailForm must create the field or not
	 * @return
	 */
	public boolean createField() default true;	
	
	/**
	 * Specifies if the field is hidden or not
	 * @return
	 */
	public boolean hidden() default false;
	
	/**
	 * Specifies if the field is disabled by default.
	 * A disabled field is not shown in insert mode, only is shown in modify mode
	 * @return
	 */
	public boolean disabled() default false;
	
	/**
	 * Default value for this field. It is the showed value in the detail form when a user
	 * wants to add a bean
	 * It only works with string, numeric, boolean and Enum type fields
	 * If the field type is boolean, the default value must be "true" or "false"
	 * If the field type is Enum, the default value must be the text value
	 * @return
	 */
	public String defaultValue() default "";
	
	/**
	 * Optional help showed bellow the field
	 * @return
	 */
	public String help() default "";
	
	/**
	 * Name of the master field. Only valid for ComboBoxes
	 * If you specify it, when the user selects in the DetailForm an element of the master field,
	 * DetailForm loads and shows the dependent elements in the this field.
	 * @return
	 */
	public String nameMasterField() default "";
}