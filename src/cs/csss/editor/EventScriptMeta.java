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
