package cs.csss.editor.events;

import cs.csss.project.Artboard;

public class HideLayerEvent extends CSSSEvent {

	private final int layerToHide;
	private final Artboard artboard;
	
	public HideLayerEvent(final Artboard artboard , final int layerToHide) {
		
		super(true);
		
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
