package cs.csss.editor.brush;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardTexture.IndexPixel;
import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.ModifyPaletteEvent;

public class Replace_AllBrush extends CSSSBrush {

	public Replace_AllBrush() {

		super("Replaces all pixels of the value of the clicked pixel on the current artboard with the active color.");

	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		IndexPixel clickedPixel = artboard.getIndexPixelAtIndices(xIndex, yIndex);		
		return new ModifyPaletteEvent(artboard , clickedPixel.xIndex , clickedPixel.yIndex , editor.selectedColors(artboard));

	}

}