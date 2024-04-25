package cs.csss.editor.event;

import cs.csss.editor.Editor;
import cs.csss.project.CSSSProject;

/**
 *
 * Event used to shut down projects in an asynchronous way.
 * 
 */
public class ShutDownProjectEvent extends CSSSEvent {

	private final CSSSProject shutThisDown;
	private final Editor editor;
	
	/**
	 * Creats an event that shuts a project down.
	 * 
	 * @param editor the editor
	 * @param shutThisDown — the project to shut down
	 */
	public ShutDownProjectEvent(Editor editor , CSSSProject shutThisDown) {

		super(true , true);
		this.editor = editor;
		this.shutThisDown = shutThisDown;

	}

	@Override public void _do() {

		shutThisDown.shutDown();
		editor.activeLine(null);
		editor.activeShape(null);
		
	}

	@Override public void undo() {}

}
