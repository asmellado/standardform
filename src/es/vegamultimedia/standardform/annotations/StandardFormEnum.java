package es.vegamultimedia.standardform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Metadata for the enum elements
 * @author alejandro
 *
 */
public @interface StandardFormEnum {
	
	/**
	 * Caption showed for this enum element
	 * If you don't specify, the caption will be the element itself
	 * @return
	 */
	public String value() default "";
}