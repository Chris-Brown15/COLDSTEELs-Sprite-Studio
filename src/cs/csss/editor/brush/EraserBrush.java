package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.ErasePixelsEvent;
import cs.csss.project.Artboard;

public class EraserBrush extends CSSSModifyingBrush {

	public EraserBrush() {

		super("Removes a pixel or region of pixels from the artboard.");
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		int[] region = centerAroundRadius(xIndex, yIndex, artboard.width(), artboard.height());
		return new ErasePixelsEvent(artboard , region[0] , region[1] , region[2] , region[3]);
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		int[] region = centerAroundRadius(xIndex, yIndex, artboard.width(), artboard.height());
		for(int row = 0 ; row < region[2] ; row++) for(int col = 0 ; col < region[3] ; col++) {
			
			if(artboard.activeLayer().containsModificationTo(region[0] + col , region[1] + row)) return true;

		}
		
		return false;
		
	}

}
