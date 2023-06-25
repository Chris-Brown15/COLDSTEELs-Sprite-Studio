package cs.csss.editor.events;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardPalette.PalettePixel;

public class ModifyPaletteEvent extends CSSSEvent {

	private final int 
		xIndex ,
		yIndex 
	;

	private final PalettePixel 
		newValue ,
		oldValue
	;
	
	private final Artboard artboard;
	
	public ModifyPaletteEvent(Artboard artboard , int paletteXIndex , int paletteYIndex , PalettePixel newValue) {
		
		super(true);
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