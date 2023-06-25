package cs.csss.editor.events;

/**
 * 
 * Base class for all events. COLDSTEEL Sprite Studio is a largely event-driven architected application, and implementors of this class are
 * used to make changes to the state of the application.
 *
 */
public abstract class CSSSEvent {

	/**
	 * If true, the code of this event must be executed in the render thread.
	 */
	public final boolean isRenderEvent;

	/**
	 * Creates an event and assigns its {@code isRenderEvent} value.
	 * 
	 * @param isRenderEvent
	 */
	public CSSSEvent(final boolean isRenderEvent) {
		
		this.isRenderEvent = isRenderEvent;
		
	}
	
	/**
	 * Executes the code of an event.
	 */
	public abstract void _do();
	
	/**
	 * Undoes the affects an event had. This should effectively result in no change if invoked before {@code _do}.
	 */
	public abstract void undo();
	
}
