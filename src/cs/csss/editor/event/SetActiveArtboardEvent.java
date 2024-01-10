/**
 * 
 */
package cs.csss.editor.event;

import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;

/**
 * Sets the current artboard for the project to a specific artboard. This event can be undone, resulting in the previous artboard being current again.
 */
public class SetActiveArtboardEvent extends CSSSEvent {

	private final Artboard newCurrent , oldCurrent;
	private final CSSSProject project;
	
	/**
	 * Creates a set active artboard event.
	 * 
	 * @param newCurrent — the new current artboard
	 * @param project — the project
	 */
	public SetActiveArtboardEvent(Artboard newCurrent , CSSSProject project) {
		
		super(false , false);
		this.newCurrent = newCurrent;
		this.oldCurrent = project.currentArtboard();
		
		this.project = project;
		
	}

	@Override public void _do() {

		project.currentArtboard(newCurrent);

	}

	@Override public void undo() {

		project.currentArtboard(oldCurrent);

	}

}
