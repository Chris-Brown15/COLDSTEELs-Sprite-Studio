package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ColorPixel;
import cs.csss.project.Artboard;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.project.IndexPixel;

/**
 * Generic event for modifying an artboard index texture layers. This event will write to the index texture conditionally based on layer 
 * logic. This event therefore does its own logic based on the active layer.
 * 
 * @author Chris Brown
 *
 */
@RenderThreadOnly public class ModifyArtboardImageEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int
		xIndex ,
		yIndex ,
		width ,
		height
	;
	
	private final ColorPixel color;
	
	private final IndexPixel[][] regionPreviousIndices;
	private final LayerPixel[][] regionPreviousLayerMods;

	/**
	 * Creates a modify artboard image event.
	 * 
	 * @param artboard — an artboard to modify
	 * @param xIndex — left x index of a region
	 * @param yIndex — bottom y index of a region
	 * @param width — width of a region
	 * @param height — height of a region
	 * @param color — color to put in the region
	 */
	public ModifyArtboardImageEvent(Artboard artboard , int xIndex , int yIndex , int width , int height , ColorPixel color) {

		super(true , false);

		if(xIndex < 0) { 
			
			//shave off the extra
			width += xIndex;
			xIndex = 0;
			
		} 
		
		if(yIndex < 0) { 
			
			height +=  yIndex;
			yIndex = 0;
			
		}

		if(xIndex + width > artboard.width()) width = xIndex + width - artboard.width() + 1;		
		if(yIndex + height > artboard.height()) height = yIndex + height - artboard.height() + 1;
		
		this.artboard = artboard;
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.width = width;
		this.height = height;

		this.color = color;
		regionPreviousIndices = artboard.getRegionOfIndexPixels(xIndex, yIndex, width, height);
		
		regionPreviousLayerMods = new LayerPixel[height][width];
		
		Layer active = artboard.activeLayer();
		
		for(int regionRow = yIndex , arrayRow = 0; regionRow < yIndex + height ; regionRow++ , arrayRow++) {
			
			for(int regionCol = xIndex , arrayCol = 0 ; regionCol < xIndex + width ; regionCol++ , arrayCol++) {
				
				regionPreviousLayerMods[arrayRow][arrayCol] = active.get(regionCol, regionRow);
				
			}			
			
		}
		
	}

	@Override public void _do() {

		artboard.putColorInImage(xIndex, yIndex, width, height, color);

	}

	@Override public void undo() {

		Layer active = artboard.activeLayer();
		artboard.writeToIndexTexture(xIndex, yIndex, width, height, regionPreviousIndices);
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
			
			LayerPixel previousLayerPixel = regionPreviousLayerMods[row][col];

			if(previousLayerPixel == null) active.remove(xIndex + col , yIndex + row);
			else active.put(previousLayerPixel);
			
		}
		
	}

}
