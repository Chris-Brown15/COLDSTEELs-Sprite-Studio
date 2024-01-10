/**
 * 
 */
package cs.csss.editor.event;

import cs.csss.engine.ColorPixel;
import cs.csss.project.ArtboardPalette;

/**
 * Event used to modify an {@link ArtboardPalette} at a given position. This event can be undone, resetting the color at the given position to the last
 * color.
 */
public class ModifyPaletteDirectEvent extends CSSSEvent {

	private final ArtboardPalette palette;	
	private ColorPixel oldValue , newValue;
	private final int xIndex , yIndex;
	
	/**
	 * Creates a new modify palette direct event. 
	 * 
	 * @param palette — the palette to modify
	 * @param xIndex — the x index of the position to modify
	 * @param yIndex — the y index of the position to modify
	 * @param newColor — the new color to put in the given position
	 */
	public ModifyPaletteDirectEvent(ArtboardPalette palette , int xIndex , int yIndex , ColorPixel newColor) {
		
		super(true , false);
		this.palette = palette;
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.newValue = newColor;
		 
	}

	@Override public void _do() {
		
		if(oldValue == null) oldValue = palette.get(xIndex, yIndex);
		palette.put(xIndex, yIndex, newValue);
				
	}

	@Override public void undo() {
		
		palette.put(xIndex, yIndex, oldValue);
				
	}

}
