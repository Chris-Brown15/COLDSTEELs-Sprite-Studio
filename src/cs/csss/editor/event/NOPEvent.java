package cs.csss.editor.event;

/**
 * Event which does nothing.
 */
public class NOPEvent extends CSSSEvent {

	/**
	 * Creates a no operation event.
	 */
	public NOPEvent() {
		
		super(false , true);
		
	}

	@Override public void _do() {}

	@Override public void undo() {}

}
