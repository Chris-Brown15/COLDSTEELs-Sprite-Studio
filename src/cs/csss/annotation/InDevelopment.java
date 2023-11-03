/**
 * 
 */
package cs.csss.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

@Documented
@Target({TYPE, METHOD})
/**
 * Notates a class or method as in development, and not to be used as of now. Such classes or methods may be implemented in the future, or removed
 * alltogether. 
 */
public @interface InDevelopment {}
