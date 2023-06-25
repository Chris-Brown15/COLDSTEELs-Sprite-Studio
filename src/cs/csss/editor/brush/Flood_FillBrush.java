package cs.csss.editor.brush;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardPalette.PalettePixel;
import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.FloodFillEvent;

public class Flood_FillBrush extends CSSSBrush {

	public Flood_FillBrush() {
		
		super("Flood fill sets all pixels within an enclosed region to the selected color.");
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		return new FloodFillEvent(artboard , editor.selectedColors(artboard) , xIndex , yIndex);
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		PalettePixel 
			colorClicked = artboard.getColorPointedToByIndexPixel(xIndex, yIndex) ,
			selectedColor = editor.selectedColors(artboard)
		;
		
		return colorClicked.compareTo(selectedColor) != 0;
		
	}

}
