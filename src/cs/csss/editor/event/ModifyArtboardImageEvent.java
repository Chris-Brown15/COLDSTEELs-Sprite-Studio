package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.Pixel;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;

/**
 * Generic event for modifying an artboard index texture layers. This event will write to the index texture conditionally based on layer 
 * logic. This event therefore does its own logic based on the active layer.
 * 
 * @author Chris Brown
 *
 */
@RenderThreadOnly public class ModifyArtboardImageEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int xIndex , yIndex , width , height;	
	private final Pixel color;
	//used to track if a new color was added to the palette. If it was, and this event is undone, we want to remove the added color from the palette.
	private boolean addedToPalette = false , didYet = false;
	private final LookupPixel[][] previousRegion;

	/**
	 * Creates a modify artboard image event.
	 * 
	 * @param artboard � an artboard to modify
	 * @param xIndex � left x index of a region
	 * @param yIndex � bottom y index of a region
	 * @param width � width of a region
	 * @param height � height of a region
	 * @param color � color to put in the region
	 */
	public ModifyArtboardImageEvent(Artboard artboard , int xIndex , int yIndex , int width , int height , Pixel color) {

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
		
		this.previousRegion = artboard.getRegionOfLayerPixels(xIndex, yIndex, width, height);

		this.color = color.clone();
		
	}

	@Override public void _do() {

		//in the first time we do this event, we check whether we added a color to the palette as a result of this event.
		if(!didYet) {
			
			didYet = true;
 			ArtboardPalette palette = artboard.activeLayer().palette();
 			int currentPaletteX = palette.currentCol();
 			
 			artboard.putColorInImage2(xIndex, yIndex, width, height, color);
 			
 			int newPaletteX = palette.currentCol(); 			
 							 //if this is true, we have added a new color
 			addedToPalette = newPaletteX != currentPaletteX;
 			
		} else artboard.putColorInImage2(xIndex, yIndex, width, height, color);		

	}

	@Override public void undo() {

		if(addedToPalette) {
			
			ArtboardPalette palette = artboard.activeLayer().palette();
			palette.popRecentColor();
			
		}
		
		artboard.replace(xIndex, yIndex, width, height, previousRegion);
							
	}

}
