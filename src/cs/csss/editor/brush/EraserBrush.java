package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.ErasePixelsEvent;
import cs.csss.project.Artboard;

/**
 * Brush that erases areas of pixels.
 */
@RenderThreadOnly public class EraserBrush extends CSSSModifyingBrush {

	/**
	 * Creates the eraser brush.
	 */
	public EraserBrush() {

		super("Removes a pixel or region of pixels from the artboard." , false);
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		int[] region = centerAroundRadius(xIndex, yIndex, artboard.width(), artboard.height());
		return new ErasePixelsEvent(artboard , region[0] , region[1] , region[2] , region[3]);
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		if(!super.canUse(artboard, editor, xIndex, yIndex)) return false;
		
		int[] region = centerAroundRadius(xIndex, yIndex, artboard.width(), artboard.height());
		for(int row = 0 ; row < region[3] ; row++) for(int col = 0 ; col < region[2] ; col++) {
			
			if(artboard.activeLayer().containsModificationTo(region[0] + col , region[1] + row)) return true;

		}
		
		return false;
		
	}

}
