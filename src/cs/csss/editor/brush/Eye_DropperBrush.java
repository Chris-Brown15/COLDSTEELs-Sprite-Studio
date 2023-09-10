package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.SetActiveColorEvent;
import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;
import cs.csss.project.ArtboardPalette.PalettePixel;

public class Eye_DropperBrush extends CSSSBrush {

	public Eye_DropperBrush() {
		
		super("Eyedropper sets the active color to the color of a clicked pixel on an artboard." , false);
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		LayerPixel layerPixel = artboard.activeLayer().get(xIndex, yIndex);
		PalettePixel color;
		
		if(layerPixel == null) color = editor.selectedColors(artboard);
		else color = artboard.getColorFromIndicesOfPalette(layerPixel.lookupX, layerPixel.lookupY);
		
		return new SetActiveColorEvent(editor , artboard , color);
		
	}

}