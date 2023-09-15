package cs.csss.editor.event;

import static cs.core.utils.CSUtils.specify;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;

/**
 * Event that adds an artboard to an animation if it is not present, or removes it if it is, as well as causing all artboards to updates 
 * accordingly.
 */
@RenderThreadOnly public class ModifyArtboardInAnimationStatusEvent extends CSSSEvent {

	private final CSSSProject project;	
	private final Artboard artboard;
	
	private final boolean initiallyAddArtboard;
	
	/**
	 * Creates a modify artboard in animation status event.
	 * 
	 * @param project — the current project
	 * @param source — an artboard whose status is to change
	 */
	public ModifyArtboardInAnimationStatusEvent(CSSSProject project , Artboard source) {
		
		super(true , true);
		
		this.project = project;
		
		initiallyAddArtboard = !project.currentAnimation().hasArtboard(source);
		
		if(initiallyAddArtboard) specify(!project.isCopy(source) , "This given artboard must not be a shallow copy");

		this.artboard = source;
		
	}

	@Override public void _do() {

		if(initiallyAddArtboard) project.appendArtboardToCurrentAnimation(artboard);
		else project.removeArtboardFromCurrentAnimation(artboard);
 
	}

	@Override public void undo() {}

}
