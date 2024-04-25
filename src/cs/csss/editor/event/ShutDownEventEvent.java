package cs.csss.editor.event;

import cs.core.utils.ShutDown;

/**
 * Used to shut down events that are instances of {@link CSSSMemoryEvent}.
 */
public class ShutDownEventEvent extends CSSSEvent {

	private final ShutDown shutDownThis;
	
	/**
	 * Creates an shut down event event.
	 * 
	 * @param shutDownThis event to shut down
	 */
	public ShutDownEventEvent(CSSSMemoryEvent shutDownThis) {

		super(shutDownThis.isRenderEvent, true);
		this.shutDownThis = shutDownThis;
		
	}

	@Override public void _do() {

		shutDownThis.shutDown();

	}

	@Override public void undo() {
		
		throw new UnsupportedOperationException("Cannot undo a memory shut down.");
		
	}

}
