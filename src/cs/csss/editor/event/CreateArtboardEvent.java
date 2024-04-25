package cs.csss.editor.event;

import java.util.Objects;

import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;

/**
 * Event for creating an artboard. Only produced from {@link cs.csss.engine.Engine#startNewArtboard() Engine.startNewArtboard()}.
 */
public class CreateArtboardEvent extends CSSSMemoryEvent {

	private final CSSSProject project;
	private final int width , height;
	private Artboard artboard;
	
	/**
	 * Creates a new artboard event.
	 *  
	 * @param project project to own the newly created artboard
	 * @param width width of the artboard
	 * @param height height of the artboard
	 */
	public CreateArtboardEvent(CSSSProject project , int width , int height) {
		
		super(true , false , SHUTDOWN_ON_REMOVE_FROM_REDO);
		this.project = Objects.requireNonNull(project);
		if(width <= 0) throw new IllegalArgumentException("Width is not positive: " + width);
		if(height <= 0) throw new IllegalArgumentException("Height is not positive: " + height);
		
		this.width = width;
		this.height = height;
		
	}

	@Override public void _do() {
		
		if(artboard == null) artboard = project.createArtboard(width, height);
		else project.addLooseArtboard(artboard);

		if(project.currentArtboard() == null) project.currentArtboard(artboard);
		
	}

	@Override public void undo() {
		
		project.removeArtboard(artboard);
		if(project.currentArtboard() == artboard) project.currentArtboard(null);
		
	}

	@Override public void shutDown() {
		
		if(isFreed()) return;
		artboard.shutDown();

	}

	@Override public boolean isFreed() {
		
		return artboard.isFreed();
		
	}

	@Override public void onStackClear(boolean isUndoStack) {
		
		if(!isUndoStack) { 
			
			project.removeArtboard(artboard);
			shutDown();
			
		}
		
	}

}
