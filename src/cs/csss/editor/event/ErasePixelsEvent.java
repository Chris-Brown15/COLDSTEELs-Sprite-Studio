package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;

/**
 * Deletes some pixels from the artboard.
 */
@RenderThreadOnly public class ErasePixelsEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int xIndex , yIndex , width , height;
	
	private volatile LayerPixel[][] previousImageRegion; 
	
	/**
	 * Creates an erase pixels event.
	 * 
	 * @param artboard — the artboard to remove from
	 * @param xIndex — left x coordinate of the region to erase from
	 * @param yIndex — bottom y coordinate of the region to erase from 
	 * @param width — width of the region to erase from
	 * @param height — height of the region to erase from
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

		if(previousImageRegion == null) previousImageRegion = artboard.getRegionOfLayerPixels(xIndex , yIndex , width , height);
		artboard.removePixels(xIndex, yIndex, width, height);
				
	}

	@Override public void undo() {

		artboard.putColorsInImage(xIndex , yIndex , width , height , previousImageRegion);
		
	}

}
