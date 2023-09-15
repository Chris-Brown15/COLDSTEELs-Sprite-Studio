package cs.csss.annotation;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target(TYPE_USE)
/**
 * Used to notate that a return type must be freed after use and before it goes out of scope.
 */
public @interface FreeAfterUse {

}
