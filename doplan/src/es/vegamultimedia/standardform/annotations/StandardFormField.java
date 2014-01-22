package es.vegamultimedia.standardform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Metadata for a field in the standard form 
 * You can use this annotation to add metadata to every bean field 
 * @author alejandro
 *
 */
public @interface StandardFormField {
	/**
	 * Caption showed in the field in the standard form
	 * @return
	 */
	public String caption();
	/**
	 * Optional help showed bellow the field 
	 * @return
	 */
	public String help() default "";
}