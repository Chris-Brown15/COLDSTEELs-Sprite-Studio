package cs.csss.editor.event;

import static cs.core.utils.CSUtils.require;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;
import cs.csss.project.CSSSProject;
import cs.csss.project.Layer;
import cs.csss.project.NonVisualLayer;

/**
 * Event for switching layers to a nonvisual layer.
 */
@RenderThreadOnly public class SwitchToNonVisualLayerEvent extends CSSSEvent {

	private final Artboard artboard;
	private final NonVisualLayer nonVisualLayer;
	private final Layer previousLayer;

	private final CSSSProject project;
	
	/**
	 * Creates a switch to nonvisual layer event.
	 * 
	 * @param artboard � an artboard
	 * @param project � the project
	 * @param nonVisualLayer � the nonvisual layer to switch to
	 */
	public SwitchToNonVisualLayerEvent(Artboard artboard , CSSSProject project , NonVisualLayer nonVisualLayer) {
		
		super(true , false);
		
		require(nonVisualLayer instanceof NonVisualLayer);
		
		this.project = project;
		
		this.artboard = artboard;
		this.nonVisualLayer = nonVisualLayer;
		previousLayer = artboard.activeLayer();
		
	}

	@Override public void _do() {

		if(nonVisualLayer == previousLayer) return;

		artboard.setActiveLayer(nonVisualLayer);
		project.forEachCopyOf(artboard , copy -> copy.setActiveLayer(nonVisualLayer));
		if(project.isCopy(artboard)) project.getSource(artboard).setActiveLayer(nonVisualLayer);
		
		artboard.setToCheckeredBackground();
		nonVisualLayer.show(artboard);

	}

	@Override public void undo() {

		if(nonVisualLayer == previousLayer) return;
		 
		artboard.setActiveLayer(previousLayer);
		project.forEachCopyOf(artboard , copy -> copy.setActiveLayer(previousLayer));
		if(project.isCopy(artboard)) project.getSource(artboard).setActiveLayer(previousLayer);
		
		nonVisualLayer.hide(artboard);
		artboard.showAllVisualLayers();
		
	}

}
