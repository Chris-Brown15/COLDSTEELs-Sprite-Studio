package cs.csss.editor.events;

import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;
import cs.csss.project.VisualLayer;
import cs.csss.project.ArtboardPalette.PalettePixel;

/**
 * Event used for when a visual layer's rank is changed. This class make the assumptions that the current layer is a visual layer, since
 * only visual layers have ranks, and that the current layer is not null.
 * 
 * @author Chris Brown
 *
 */
public class MoveLayerRankEvent extends CSSSEvent {

	private final int 
		moveToThis ,
		active
	;
	
	private final Artboard artboard;
	
	public MoveLayerRankEvent(final Artboard artboard , int moveToThis) {

		super(true);
		
		this.artboard = artboard;
		this.moveToThis = moveToThis;
		active = artboard.getLayerRank((VisualLayer) artboard.activeLayer());
		
	}

	private void swapLayers(int moveTo , int active) {

		VisualLayer currentLayer = (VisualLayer) artboard.getVisualLayer(active);
		
		if(moveTo == active) return;
		
		boolean movingUp = moveTo < active;
		
		//moving the layer's rank down means it will be higher priority, or closer to the 'top'
		
		if(movingUp) currentLayer.forEachModification(pixel -> {
			
			/*
			 * We can update the artboard only if no layer of a greater priority to 'move to' modifies the pixel 'pixel' represents.
			 * If moveToThis is nonzero, we check all layers between it and the highest layer by invoking the isUpperRankLayerModifying from 
			 * the moveToThis value to 0. If this is true, it means some layer beyond the layer we are moving to modifies the pixel we are 
			 * considering writing, so we cannot update the artboard.
			 * 
			 * If moveToThis is zero, we check from the current layer's rank to the moveToThis. If this returns true, we can modify.
			 *   
			 */
			
			boolean canModify;
			
			if(moveTo != 0) canModify = !artboard.isUpperRankLayerModifying(moveTo , 0 ,  pixel.textureX , pixel.textureY);			
			else canModify = artboard.isUpperRankLayerModifying(active , moveTo , pixel.textureX , pixel.textureY);
			
			if(canModify) artboard.writeToIndexTexture(
				pixel.textureX , 
				pixel.textureY , 
				1 , 
				1 , 
				artboard.getColorFromIndicesOfPalette(pixel.lookupX, pixel.lookupY)
			);
			
		});		
		//increasing a layer's rank moves it closer to the 'bottom'
		else currentLayer.forEachModification(pixel -> {
			
			/*
			 * In the case we are decreasing the priority of the layer, a modification would occur if a layer that was previously a lower
			 * priority is now higher priority. If multiple layers of a lower priority modify the pixel position, the one among those with
			 * the highest priority will be the one whose color is chosen.
			 * 
			 */
			
			for(int i = active + 1 ; i <= moveTo ; i++) { 
				
				VisualLayer iterLayer = artboard.getVisualLayer(i); 
				
				if(iterLayer.isModifying(pixel.textureX , pixel.textureY)) {
				
					LayerPixel px = iterLayer.get(pixel.textureX , pixel.textureY);
					PalettePixel p = artboard.getColorFromIndicesOfPalette(px.lookupX , px.lookupY);
					artboard.writeToIndexTexture(pixel.textureX, pixel.textureY, 1, 1, p);
					break;
					
				}
				
			}
			
		});	
		
		artboard.moveVisualLayerRank(moveTo);
		
	}
	
	@Override public void _do() {
		
		swapLayers(moveToThis , active);
		
	}

	@Override public void undo() {

		swapLayers(active , moveToThis);

	}

}

