package cs.csss.editor.events;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.Layer;
import cs.csss.artboard.VisualLayer;

public class SwitchToVisualLayerEvent extends CSSSEvent {

	private final Artboard artboard;
	
	private final VisualLayer newActiveLayer;
	private final Layer previousLayer;
	
	private final boolean sameTypes;
	
	public SwitchToVisualLayerEvent(Artboard artboard , VisualLayer newActiveLayer) {
		
		super(true);
		
		this.artboard = artboard;
		this.newActiveLayer = newActiveLayer;
		previousLayer = artboard.activeLayer();
		
		sameTypes = newActiveLayer instanceof VisualLayer && previousLayer instanceof VisualLayer;
		
	}

	@Override public void _do() {

		if(newActiveLayer == previousLayer) return;

		artboard.setActiveLayer(newActiveLayer);
			
		//previous was a nonvisual layer
		if(!sameTypes) { 
			
			artboard.setToCheckeredBackground();
			artboard.showAllVisualLayers();
			
		}
		
	}

	@Override public void undo() {

		if(newActiveLayer == previousLayer) return;

		artboard.setActiveLayer(previousLayer);
		
		//switching to a nonvisual layer
		if(!sameTypes) { 
			
			artboard.setToCheckeredBackground();
			//if they aren't the same type we need to show the previous because it was a nonvisual
			previousLayer.show(artboard);
			
		}
		
	}

}
