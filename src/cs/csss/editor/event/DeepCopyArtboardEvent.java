/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;

import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;

/**
 * Event for deep copying an artboard.
 */
public class DeepCopyArtboardEvent extends CSSSMemoryEvent {

	private final CSSSProject project;
	private final Artboard source;
	private Artboard copy;
	
	/**
	 * Creates a new deep copy artboard event.
	 * @param project the project
	 * @param copyThis the artboard to copy
	 * 
	 * @throws NullPointerException if any parameter is <code>null</code>.
	 */
	public DeepCopyArtboardEvent(CSSSProject project , Artboard copyThis) {

		super(true , false , SHUTDOWN_ON_REMOVE_FROM_REDO);
		this.project = Objects.requireNonNull(project);
		Objects.requireNonNull(copyThis);		
		this.source = project.isCopy(copyThis) ? project.getSource(copyThis) : copyThis;	
				
	}	

	@Override public void _do() {

		//happens only the first time
		if(copy == null) copy = project.deepCopy(source);
		else project.addLooseArtboard(copy);	
		
	}

	@Override public void undo() {

		project.removeArtboard(copy);
		
	}

	@Override public void shutDown() {

		if(isFreed()) return;
		copy.shutDown();

	}

	@Override public boolean isFreed() {

		return copy.isFreed();
		
	}
	
	@Override public void onStackClear(boolean isUndoStack) {

		if(!isUndoStack) {
			
			project.removeArtboard(copy);
			shutDown();
			
		}
		
	}

}
