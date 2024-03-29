package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.SetActiveColorEvent;
import cs.csss.engine.Pixel;
import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;

/**
 * Sets the current color in the left hand side panel to a clicked pixel
 */
public class Eye_DropperBrush extends CSSSBrush {

	public Eye_DropperBrush() {
		
		super("Eyedropper sets the active color to the color of a clicked pixel on an artboard." , false);
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		LayerPixel layerPixel = artboard.activeLayer().get(xIndex, yIndex);
		Pixel color;
		
		if(layerPixel == null) color = editor.currentColor();
		else color = artboard.getColorFromIndicesOfPalette(layerPixel.lookupX, layerPixel.lookupY);
		
		return new SetActiveColorEvent(editor , color);
		
	}

}