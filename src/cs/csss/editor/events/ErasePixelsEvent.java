package cs.csss.editor.events;

import cs.csss.project.Artboard;
import cs.csss.project.IndexTexture.IndexPixel;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.project.VisualLayer;

public class ErasePixelsEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int
		xIndex ,
		yIndex ,
		width ,
		height
	;
	
	private volatile IndexPixel[][] previousImageRegion; 
	
	public ErasePixelsEvent(Artboard artboard , int xIndex , int yIndex , int width , int height) {

		super(true , false);
		
		this.artboard = artboard;
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.width = width;
		this.height = height;
		
	}

	@Override public void _do() {

		previousImageRegion = artboard.getRegionOfIndexPixels(xIndex , yIndex , width , height);
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
			
			artboard.putColorInImage(x, y, 1, 1, artboard.getColorPointedToByIndexPixel(previousImageRegion[row][col]));
						
		}
		
	}

}
