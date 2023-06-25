package cs.csss.editor.events;

import static cs.core.utils.CSUtils.require;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.Layer;
import cs.csss.artboard.NonVisualLayer;

public class SwitchToNonVisualLayerEvent extends CSSSEvent {

	private final Artboard artboard;
	private final NonVisualLayer nonVisualLayer;
	private final Layer previousLayer;

	public SwitchToNonVisualLayerEvent(Artboard artboard , NonVisualLayer nonVisualLayer) {
		
		super(true);
		
		require(nonVisualLayer instanceof NonVisualLayer);
		
		this.artboard = artboard;
		this.nonVisualLayer = nonVisualLayer;
		previousLayer = artboard.activeLayer();
		
	}

	@Override public void _do() {

		if(nonVisualLayer == previousLayer) return;

		artboard.setActiveLayer(nonVisualLayer);
		
		artboard.setToCheckeredBackground();
		nonVisualLayer.show(artboard);

	}

	@Override public void undo() {

		if(nonVisualLayer == previousLayer) return;
		 
		artboard.setActiveLayer(previousLayer);
		
		nonVisualLayer.hide(artboard);
		artboard.showAllVisualLayers();
		
	}

}
