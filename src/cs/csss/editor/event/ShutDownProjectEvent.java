package cs.csss.editor.event;

import cs.csss.project.CSSSProject;

/**
 *
 * Event used to shut down projects in an asynchronous way.
 * 
 */
public class ShutDownProjectEvent extends CSSSEvent {

	private final CSSSProject shutThisDown;
	
	/**
	 * Creats an event that shuts a project down.
	 * 
	 * @param shutThisDown — the project to shut down
	 */
	public ShutDownProjectEvent(CSSSProject shutThisDown) {

		super(false , true);
		this.shutThisDown = shutThisDown;

	}

	@Override public void _do() {

		shutThisDown.shutDown();

	}

	@Override public void undo() {}

}
