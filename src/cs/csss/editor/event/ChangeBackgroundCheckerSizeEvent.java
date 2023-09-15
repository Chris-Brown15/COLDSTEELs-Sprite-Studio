package cs.csss.editor.event;

import cs.csss.project.IndexTexture;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.CSSSProject;

/**
 * Changes all visible background checkers to the current size (so set the new sizes before entering this event).
 * 
 */
@RenderThreadOnly public class ChangeBackgroundCheckerSizeEvent extends CSSSEvent {

	private CSSSProject project;
	
	private final int
		previousWidth = IndexTexture.backgroundWidth ,
		previousHeight = IndexTexture.backgroundHeight ,
		newWidth ,
		newHeight
	;
	
	/**
	 * Creates a new event for setting the size of the checkered backgrounds of artboards.
	 * 
	 * @param project — the project
	 * @param newBackgroundWidth — the new width of checkers
	 * @param newBackgroundHeight — the new height of checkers 
	 */
	public ChangeBackgroundCheckerSizeEvent(CSSSProject project , int newBackgroundWidth , int newBackgroundHeight) {
		
		super(true , false);
		this.project = project;
		this.newWidth = newBackgroundWidth;
		this.newHeight = newBackgroundHeight;
		
	}

	@Override public void _do() {
		
		IndexTexture.backgroundWidth = newWidth;
		IndexTexture.backgroundHeight = newHeight;		
		setCheckeredBackgroundByLayerShowing();
	}

	@Override public void undo() {

		IndexTexture.backgroundWidth = previousWidth;
		IndexTexture.backgroundHeight = previousHeight;
		setCheckeredBackgroundByLayerShowing();
		
	}

	private void setCheckeredBackgroundByLayerShowing() {
		
		project.forEachArtboard(artboard -> {
			
			artboard.setToCheckeredBackground();
			artboard.showAllVisualLayers();
			
		});
		
	}
	
}
