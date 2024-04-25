/**
 * 
 */
package cs.csss.editor.event;

import cs.core.utils.ShutDown;

/**
 * Extension of {@link CSSSEvent} for events that own native memory which must be shut down at some point. For some events, the memory must be shut down
 * when the event is removed from the editor's redo stack. In other cases, the memory should be freed when the object can no longer be undone because too
 * many more events have been pushed onto the undo stack. But in most cases, the memory should be shut down either way. Therefore, when to shut down is 
 * specified by one or both of {@link #SHUTDOWN_ON_REMOVE_FROM_REDO} and {@link #SHUTDOWN_ON_REMOVE_FROM_UNDO}. As well, the 
 * {@link #onStackClear(boolean)} method is implemented for when the event stacks are cleared in the editor. If this happens, this event will not be 
 * removed from a stack, but will no longer be available no matter what.
 */
public abstract class CSSSMemoryEvent extends CSSSEvent implements ShutDown {

	/**
	 * Notates that this event should be shut down when it is removed from the undo stack. Use this value if the event should be shut down once it's 
	 * locked in forever.
	 */
	public static final byte SHUTDOWN_ON_REMOVE_FROM_UNDO = 0b001;
	
	/**
	 * Notates that this event should be shut down when it is removed from the redo stack. Use this value if the event should be shut down once it's lost
	 * forever.
	 */
	public static final byte SHUTDOWN_ON_REMOVE_FROM_REDO = 0b010;
	
	/**
	 * Checks a value for being a valid shutdown case identifier. It will be valid if it is one of {@link #SHUTDOWN_ON_REMOVE_FROM_REDO}, 
	 * {@link #SHUTDOWN_ON_REMOVE_FROM_UNDO}, or {@code SHUTDOWN_ON_REMOVE_FROM_REDO|SHUTDOWN_ON_REMOVE_FROM_UNDO}.
	 * 
	 * @param value value to check
	 * @return {@code value}.
	 * @throws IllegalArgumentException if {@code value} is not a valid shutdown case identifier.
	 */
	public static byte checkShutdownCaseIdentifier(byte value) {
		
		if(
			value != SHUTDOWN_ON_REMOVE_FROM_REDO && 
			value != SHUTDOWN_ON_REMOVE_FROM_UNDO && 
			value != (SHUTDOWN_ON_REMOVE_FROM_REDO|SHUTDOWN_ON_REMOVE_FROM_UNDO
		)) throw new IllegalArgumentException(value + " is not a valid shutdown case identifier.");
		
		return value;
		
	}
	
	/**
	 * Selected cases for when this event should shut down.
	 */
	public final byte shutDownCases;
	
	/**
	 * Creates a new memory event with the inherited booleans from {@link CSSSEvent} and the shut down when boolean.
	 * 
	 * @param isRenderEvent if {@code true}, this event must be executed in the render thread
	 * @param isTransientEvent if <code>true</code>, this event cannot be undone
	 * @param shutDownOnRemoveFromUndo the cases in which this event can safely be shut down
	 * @throws IllegalArgumentException if {@code value} is not a valid shutdown case identifier.
	 */
	public CSSSMemoryEvent(boolean isRenderEvent , boolean isTransientEvent , byte whenToShutDown) {
		
		super(isRenderEvent , isTransientEvent);
		shutDownCases = checkShutdownCaseIdentifier(whenToShutDown);
		
	}

	public abstract void shutDown();
	
	public abstract boolean isFreed();

	/**
	 * Checks whether at least one case contained in {@code shutDownCases} is present in this event's shut down cases.
	 * 
	 * @param shutDownCases value representing shut down cases
	 * @return <code>true</code> if at least one case matches between this event's shut down cases and the given representation.
	 * @throws IllegalArgumentException if {@code shutDownCases} is not valid as a shut down cases.
	 */
	public final boolean isAny(byte shutDownCases) {
		
		checkShutdownCaseIdentifier(shutDownCases);
		
		return (this.shutDownCases & shutDownCases) != 0;
		
	}

	/**
	 * Checks whether all cases contained in {@code shutDownCases} are present in this event's shut down cases.
	 * 
	 * @param shutDownCases value representing shut down cases
	 * @return <code>true</code> if all cases matches between this event's shut down cases and the given representation.
	 * @throws IllegalArgumentException if {@code shutDownCases} is not valid as a shut down cases.
	 */
	public final boolean isAll(byte shutDownCases) {
		
		checkShutdownCaseIdentifier(shutDownCases);
		
		return this.shutDownCases == shutDownCases;
		
	}
	
	/**
	 * This method defines what to do when the stack owning the event is cleared. This method should call {@link #shutDown()} if needed based on the state
	 * of {@code isUndoStack}.
	 * 
	 * @param isUndoStack whether this event is currently in the undo stack or redo stack; will be true if this event is in the undo stack, false 
	 * 					  otherwise
	 */
	public void onStackClear(boolean isUndoStack) {
		
		if((isUndoStack && isAny(SHUTDOWN_ON_REMOVE_FROM_UNDO)) || (!isUndoStack && isAny(SHUTDOWN_ON_REMOVE_FROM_REDO))) shutDown();
		
	}
	
}
