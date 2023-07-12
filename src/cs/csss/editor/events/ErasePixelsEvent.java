package cs.csss.editor.events;

import cs.csss.project.Artboard;
import cs.csss.project.ArtboardTexture.IndexPixel;
import cs.csss.project.Layer;

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

		super(true);
		
		this.artboard = artboard;
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.width = width;
		this.height = height;
		
	}

	@Override public void _do() {

		previousImageRegion = artboard.getRegionOfIndexPixels(xIndex , yIndex , width , height);
		Layer active = artboard.activeLayer();

		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) { 

			int x = xIndex + col;
			int y = yIndex + row;
			
		  	if(active.containsModificationTo(x, y)) active.remove(x , y);
			artboard.writeToIndexTexture(x , y , 1 , 1 , artboard.getBackgroundColor(x , y));
			
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
