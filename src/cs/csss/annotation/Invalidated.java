/**
 * Copyright 2025, All Rights Reserved.
 * ————————————————————————————————————
 * This file and any accompanying files
 * belong to STEEL Softworks, LLC. Do 
 * not distribute these files without 
 * permission from Chris Brown, owner 
 * of STEEL Softworks, at 
 * chris@steelsoftworks.net
 * ————————————————————————————————————
 */
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
 * Notates that some parameter to a method or constructor is no longer valid for use after a method returns.
 */
public @interface Invalidated {

}
