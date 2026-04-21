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
 * Container for metadata for event scripts.
 */
public record EventScriptMeta(
	boolean isRenderEvent , 
	boolean isTransientEvent , 
	boolean takesArguments , 
	String argumentDialogueText ,
	String scriptName
) {

}
