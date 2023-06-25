package cs.csss.editor.brush;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardPalette.PalettePixel;
import cs.csss.artboard.LayerPixel;
import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.SetActiveColorEvent;

public class Eye_DropperBrush extends CSSSBrush {

	public Eye_DropperBrush() {
		
		super("Eyedropper sets the active color to the color of a clicked pixel on an artboard.");
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		LayerPixel layerPixel = artboard.activeLayer().get(xIndex, yIndex);
		PalettePixel color;
		
		if(layerPixel == null) color = editor.selectedColors(artboard);
		else color = artboard.getColorFromIndicesOfPalette(layerPixel.lookupX, layerPixel.lookupY);
		
		return new SetActiveColorEvent(editor , artboard , color);
		
	}

}