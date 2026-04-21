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

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target({ METHOD, CONSTRUCTOR, TYPE })
/**
 * Notates that some method is only to be invoked in the render thread. If a method containing this annotation is not invoked in the render
 * thread, a native crash will occur. 
 * <p>
 * 	If applied to a type, it should be understood that in general the methods of that type need to be invoked in the render thread. There may
 * 	be exceptions, but not many.
 * </p>
 */
public @interface RenderThreadOnly {}
