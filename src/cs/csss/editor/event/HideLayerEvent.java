package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;

/**
 * Hides a layer of the artboard.
 */
@RenderThreadOnly public class HideLayerEvent extends CSSSEvent {

	private final int layerToHide;
	private final Artboard artboard;
	
	public HideLayerEvent(final Artboard artboard , final int layerToHide) {
		
		super(true , false);
		
		this.artboard = artboard;
		this.layerToHide = layerToHide;

	}

	@Override public void _do() {
		
		artboard.toggleHideLayer(artboard.getVisualLayer(layerToHide));

	}

	@Override public void undo() {

		artboard.toggleHideLayer(artboard.getVisualLayer(layerToHide));
		
	}

}
