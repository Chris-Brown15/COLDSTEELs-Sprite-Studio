package cs.csss.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target(PARAMETER)
/**
 * Notates that a parameter to a method or constructor can be null, and that the receiver will handle the null parameter gracefully.
 */
public @interface Nullable {

}
