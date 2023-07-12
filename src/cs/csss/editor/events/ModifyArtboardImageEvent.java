package cs.csss.editor.events;

import cs.csss.project.Artboard;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.ArtboardTexture.IndexPixel;

/**
 * Generic event for modifying an artboard index texture layers. This event will write to the index texture conditionally based on layer 
 * logic. This event therefore does its own logic based on the active layer.
 * 
 * @author Chris Brown
 *
 */
public class ModifyArtboardImageEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int
		xIndex ,
		yIndex ,
		width ,
		height
	;
	
	private final PalettePixel color;
	
	private final IndexPixel[][] regionPreviousIndices;
	private final LayerPixel[][] regionPreviousLayerMods;

	public ModifyArtboardImageEvent(Artboard artboard , int xIndex , int yIndex , int width , int height , PalettePixel color) {

		super(true);

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
		
		/*
		 * To undo a write, put at the artbaord texture whatever was previously there. This applies to the texture and the layer.
		 * 
		 * The iteration iterates as offsets from the bottom left of the region and undoes the writes to the index texture and the active
		 * layer, which must be the same as it was then the _do method was invoked.
		 * 
		 */
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
			
			IndexPixel currentPixelIndices = regionPreviousIndices[row][col];
			PalettePixel previousColorForPosition = artboard.getColorFromIndicesOfPalette(
				currentPixelIndices.xIndex , 
				currentPixelIndices.yIndex
			);
			
			artboard.putColorInImage(xIndex + col , yIndex + row , 1 , 1 , previousColorForPosition);
		
			LayerPixel previousLayerPixel = regionPreviousLayerMods[row][col];

			if(previousLayerPixel == null) active.remove(xIndex + col , yIndex + row);
			else active.put(previousLayerPixel);
			
		}
		
	}

}
