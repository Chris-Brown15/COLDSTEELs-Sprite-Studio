package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;

/**
 * Deletes a region of pixels from the active layer.
 */
@RenderThreadOnly public class DeleteRegionEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int
		xIndex ,
		yIndex ,
		width ,
		height
	;
	
	private final LayerPixel[][] region;
	
	/**
	 * Creates a delete region event.
	 * 
	 * @param artboard — artboard to delete from
	 * @param xIndex — left x coordinate of the region to remove from
	 * @param yIndex — bottom y coordinate of the region to remove from
	 * @param width — width of the region to remove from
	 * @param height — height of the region to remove from
	 * @param region — previous region of the artboard, for undoing
	 */
	public DeleteRegionEvent(Artboard artboard , int xIndex , int yIndex , int width , int height , LayerPixel[][] region) {

		super(true , false);
		
		this.artboard = artboard;
		
		this.region = region;
		
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.width = width;
		this.height = height;
		
	}

	@Override public void _do() {

		artboard.removePixels(xIndex, yIndex, width, height);
				
	}

	@Override public void undo() {

		artboard.putColorsInImage(xIndex, yIndex, width, height, region);
		
	}

}
