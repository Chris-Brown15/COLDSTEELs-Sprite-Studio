package cs.csss.editor.events;

import static cs.core.utils.CSUtils.specify;

import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;

/**
 * Event that adds an artboard to an animation if it is not prsent, or removes it if it is, as well as causing all artboards to updates 
 * accordingly.
 */
public class ModifyArtboardInAnimationStatusEvent extends CSSSEvent {

	private final CSSSProject project;	
	private final Artboard artboard;
	
	private final boolean initiallyAddArtboard;
	
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
