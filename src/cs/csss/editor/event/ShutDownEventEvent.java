package cs.csss.editor.event;

import cs.core.utils.ShutDown;

/**
 * Used to shut down events that are {@code instanceof ShutDown}.
 */
public class ShutDownEventEvent extends CSSSEvent {

	private final ShutDown shutDownThis;
	
	/**
	 * Creates an shut down event event.
	 * 
	 * @param isRenderEvent — {@code true} if this is a render event, which ususally means that the event being shut down is a render event
	 * @param shutDownThis — {@code ShutDown} cast to the event being shutdown
	 */
	public ShutDownEventEvent(boolean isRenderEvent , ShutDown shutDownThis) {

		super(isRenderEvent, true);
		this.shutDownThis = shutDownThis;
		
	}

	@Override public void _do() {

		shutDownThis.shutDown();

	}

	@Override public void undo() {}

}
