package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.project.Layer;
import cs.csss.project.VisualLayer;

/**
 * Event used to switch to a visual layer.
 */
@RenderThreadOnly public class SwitchToVisualLayerEvent extends CSSSEvent {

	private final Artboard artboard;
	
	private final VisualLayer newActiveLayer;
	private final Layer previousLayer;
	
	private final boolean sameTypes;
	
	private final CSSSProject project;
	
	/**
	 * Creates a switch to visual layer event.
	 * 
	 * @param artboard — an artboard
	 * @param project — the project
	 * @param newActiveLayer — the new active visual layer
	 */
	public SwitchToVisualLayerEvent(Artboard artboard , CSSSProject project , VisualLayer newActiveLayer) {
		
		super(true , false);
		
		this.artboard = artboard;
		this.newActiveLayer = newActiveLayer;
		previousLayer = artboard.activeLayer();
		
		this.project = project;
		
		sameTypes = newActiveLayer instanceof VisualLayer && previousLayer instanceof VisualLayer;
		
	}

	@Override public void _do() {

		if(newActiveLayer == previousLayer) return;

		artboard.setActiveLayer(newActiveLayer);
		project.forEachCopyOf(artboard , copy -> copy.setActiveLayer(newActiveLayer));
		if(project.isCopy(artboard)) project.getSource(artboard).setActiveLayer(newActiveLayer);
		
		//previous was a nonvisual layer
		if(!sameTypes) {
			
			artboard.setToCheckeredBackground();
			artboard.showAllVisualLayers();
			
		}
		
	}

	@Override public void undo() {

		if(newActiveLayer == previousLayer) return;

		artboard.setActiveLayer(previousLayer);
		project.forEachCopyOf(artboard , copy -> copy.setActiveLayer(previousLayer));
		if(project.isCopy(artboard)) project.getSource(artboard).setActiveLayer(previousLayer);
		
		//switching to a nonvisual layer
		if(!sameTypes) { 
			
			artboard.setToCheckeredBackground();
			//if they aren't the same type we need to show the previous because it was a nonvisual
			previousLayer.show(artboard);
			
		}
		
	}

}
