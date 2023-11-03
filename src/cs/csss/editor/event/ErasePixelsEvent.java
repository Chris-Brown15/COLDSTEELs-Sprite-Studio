package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;
import cs.csss.project.IndexPixel;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.project.VisualLayer;

/**
 * Deletes some pixels from the artboard.
 */
@RenderThreadOnly public class ErasePixelsEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int
		xIndex ,
		yIndex ,
		width ,
		height
	;
	
	private volatile IndexPixel[][] previousImageRegion; 
	
	/**
	 * Creates an erase pixels event.
	 * 
	 * @param artboard � the artboard to remove from
	 * @param xIndex � left x coordinate of the region to erase from
	 * @param yIndex � bottom y coordinate of the region to erase from 
	 * @param width � width of the region to erase from
	 * @param height � height of the region to erase from
	 */
	public ErasePixelsEvent(Artboard artboard , int xIndex , int yIndex , int width , int height) {

		super(true , false);
		
		this.artboard = artboard;
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.width = width;
		this.height = height;
		
	}

	@Override public void _do() {

		if(previousImageRegion == null) previousImageRegion = artboard.getRegionOfIndexPixels(xIndex , yIndex , width , height);
		
		Layer active = artboard.activeLayer();
		//will have a nonnegative value when the active layer is visual
		int activeLayerRank = artboard.isActiveLayerVisual() ? artboard.getLayerRank((VisualLayer)active) : -1;

		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) { 

			int x = xIndex + col;
			int y = yIndex + row;
			
		  	if(active.containsModificationTo(x, y)) active.remove(x , y);		  	
		  	//if this layer is visual and no higher ranking layer is modifying (in the case a higher ranking layer is modifying, we dont need
		  	//to update the texture)
		  	if(activeLayerRank != -1) {
		  	
		  		if(artboard.isUpperRankLayerModifying(activeLayerRank , x , y)) continue;
		  		VisualLayer nextLowest = artboard.getHighestLowerRankLayerModifying(activeLayerRank, x, y);
		  		
		  		if(nextLowest != null) {
		  			
		  			LayerPixel asPixel = nextLowest.get(x, y);
		  			artboard.writeToIndexTexture(x, y, 1, 1, artboard.getColorFromIndicesOfPalette(asPixel.lookupX, asPixel.lookupY));
		  			
		  		} else artboard.writeToIndexTexture(x , y , 1 , 1 , artboard.getBackgroundColor(x , y));
		  				  		
		  	}		  	
		  	else artboard.writeToIndexTexture(x , y , 1 , 1 , artboard.getBackgroundColor(x , y));
			
		}
		
	}

	@Override public void undo() {

		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
			
			int x = xIndex + col;
			int y = yIndex + row;
			
			artboard.putColorInImage(x, y, 1, 1, artboard.getColorPointedToBy(previousImageRegion[row][col]));
						
		}
		
	}

}