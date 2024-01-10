package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ColorPixel;
import cs.csss.project.Artboard;

/**
 * Overwrites a palette pixel to a new value. Every index pixel that pointed to that value will display a different color after this.
 */
@RenderThreadOnly public class ModifyPaletteEvent extends CSSSEvent {

	private final int xIndex , yIndex;

	private final ColorPixel newValue , oldValue;
	
	private final Artboard artboard;
	
	/**
	 * Creates a modify palette event.
	 * 
	 * @param artboard — an artboard
	 * @param paletteXIndex — x index of the palette pixel to change
	 * @param paletteYIndex — y index of the palette pixel to change
	 * @param newValue — new color to write in the palette
	 */
	public ModifyPaletteEvent(Artboard artboard , int paletteXIndex , int paletteYIndex , ColorPixel newValue) {
		
		super(true , false);
		this.artboard = artboard;
		this.xIndex = paletteXIndex;
		this.yIndex = paletteYIndex;
		this.newValue = newValue;
		this.oldValue = artboard.getColorFromIndicesOfPalette(paletteXIndex, paletteYIndex);
		
	}

	@Override public void _do() {
				
		artboard.replacePalettePixelAtIndex(xIndex, yIndex, newValue);
		
	}

	@Override public void undo() {
		
		artboard.replacePalettePixelAtIndex(xIndex, yIndex, oldValue);

	}

}
