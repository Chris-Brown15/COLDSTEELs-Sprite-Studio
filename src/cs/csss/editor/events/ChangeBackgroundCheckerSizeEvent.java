package cs.csss.editor.events;

import cs.csss.artboard.ArtboardTexture;
import cs.csss.core.CSSSProject;

/**
 * Changes all visible background checkers to the current size (so set the new sizes before entering this event)
 * 
 */
public class ChangeBackgroundCheckerSizeEvent extends CSSSEvent {

	private CSSSProject project;
	
	private final int
		previousWidth = ArtboardTexture.backgroundCheckerWidth ,
		previousHeight = ArtboardTexture.backgroundCheckerHeight ,
		newWidth ,
		newHeight
	;
	
	public ChangeBackgroundCheckerSizeEvent(CSSSProject project , int newBackgroundWidth , int newBackgroundHeight) {
		
		super(true);
		this.project = project;
		this.newWidth = newBackgroundWidth;
		this.newHeight = newBackgroundHeight;
		
	}

	@Override public void _do() {
		
		ArtboardTexture.backgroundCheckerWidth = newWidth;
		ArtboardTexture.backgroundCheckerHeight = newHeight;		
		setCheckeredBackgroundByLayerShowing();
	}

	@Override public void undo() {

		ArtboardTexture.backgroundCheckerWidth = previousWidth;
		ArtboardTexture.backgroundCheckerHeight = previousHeight;
		setCheckeredBackgroundByLayerShowing();
		
	}

	private void setCheckeredBackgroundByLayerShowing() {
		
		project.forEachArtboard(artboard -> {
			
			artboard.setToCheckeredBackground();
			artboard.showAllVisualLayers();
			
		});
		
	}
	
}
