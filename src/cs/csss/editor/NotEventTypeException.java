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
package cs.csss.editor;

/**
 * Thrown when a script's name function does not return a {@code PyObject}.
 */
public class NotEventTypeException extends RuntimeException {

	/**
	 * Generated
	 */
	private static final long serialVersionUID = 2696657467483962757L;

	public NotEventTypeException(Object object) {

		super(object.getClass().getName() + " is not an extender of cs.csss.editor.event.CSSSEvent.");
		
	}
	
}
