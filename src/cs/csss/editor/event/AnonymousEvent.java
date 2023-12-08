/**
 * 
 */
package cs.csss.editor.event;

import cs.core.utils.Lambda;

/**
 * Event type that receives its code for {@code _do} and {@code undo} in its constructor.
 */
public class AnonymousEvent extends CSSSEvent {

	private Lambda _do;
	private Lambda undo;

	/**
	 * Creates a new anonymous event which is not transient, that is, it can be undone.
	 * 
	 * @param isRenderEvent — whether this event must be executed in the render thread
	 * @param _do — the code to invoke when this event is executed
	 * @param undo — the code to invoke when this event is undone
	 */
	public AnonymousEvent(boolean isRenderEvent, Lambda _do , Lambda undo) {
		
		super(isRenderEvent, true);
		this._do = _do;
		this.undo = undo;
		
	}

	/**
	 * Creates a new anonymous event which is transient, that is, it cannot be undone.
	 * 
	 * @param isRenderEvent — whether this event must be executed in the render thread
	 * @param _do — the code to invoke when this event is executed
	 */
	public AnonymousEvent(boolean isRenderEvent, Lambda _do) {
		
		super(isRenderEvent , false);
		this._do = _do;
		
	}
	
	@Override public void _do() {

		_do.invoke();

	}

	@Override public void undo() {

		undo.invoke();

	}

}
