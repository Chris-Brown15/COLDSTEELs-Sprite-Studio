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
 * Used for using code that is blocked behind debug mode.
 */
public class DebugDisabledException extends Exception {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 4548646702924380645L;

	public DebugDisabledException(Editor editor) {
		
		super("A Debug Method was invoked while the application is not in debug mode." , null , false , true);
		editor.exit();
		
	}

}