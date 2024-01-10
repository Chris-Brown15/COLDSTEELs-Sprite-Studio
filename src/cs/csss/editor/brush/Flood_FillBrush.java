package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.FloodFillEvent;
import cs.csss.engine.Pixel;
import cs.csss.project.Artboard;

/**
 * Fills in an enclosed region. WIP.
 */
@RenderThreadOnly public class Flood_FillBrush extends CSSSBrush {

	public Flood_FillBrush() {
		
		super("Flood fill sets all pixels within an enclosed region to the selected color." , false);
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		return new FloodFillEvent(artboard , editor.currentColor() , xIndex , yIndex);
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		if(!super.canUse(artboard, editor, xIndex, yIndex)) return false;
		
		Pixel colorClicked = artboard.getColorPointedToByIndexPixel(xIndex, yIndex) , selectedColor = editor.selectedColorValues();
		
		return colorClicked.compareTo(selectedColor) != 0;
		
	}

}
