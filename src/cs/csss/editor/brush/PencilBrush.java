package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.ModifyArtboardImageEvent;
import cs.csss.engine.Pixel;
import cs.csss.project.Artboard;

/**
 * Creates events that modify singular regions of the artboard.
 */
@RenderThreadOnly public class PencilBrush extends CSSSModifyingBrush {

	public PencilBrush() {

		super("Pencil colors artboards by clicking on pixels within them." , false);

	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		if(radius == 0) return new ModifyArtboardImageEvent(artboard, xIndex, yIndex, 1, 1 , editor.currentColor());

		int[] region = centerAroundRadius(xIndex, yIndex , artboard.width() , artboard.height());
		
		return new ModifyArtboardImageEvent(artboard , region[0] , region[1] , region[2] , region[3] , editor.currentColor());
		
	}

	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		if(!super.canUse(artboard, editor, xIndex, yIndex)) return false;
		
		/*
		 * The pencil can be used if any pixel modification of the given layer does not match the editor selected color.
		 */
		 
		//this array's contents are the x and y index of the bottom left corner of the modified region, and the diameter of the 
		//modification.
		int[] region = centerAroundRadius(xIndex, yIndex , artboard.width() , artboard.height());
		
		Pixel editorActive = editor.currentColor();
						
		boolean areRegionsEqual = true;
		int x , y;
		
		//starts at the bottom left corner of the modified region and iterates over all pixels within it.
		//regions are not equal if at least one pixel in the region does not satisfy the condition below
		for(int row = 0 ; row < region[3] ; row++) for(int col = 0 ; col < region[2] ; col++) {
			
			x = region[0] + col ; y = region[1] + row;
			
			//the condition inside the parentheses will be true when the pixel does match the current iteration's pixel
			if(!(artboard.doColorsMatch(editorActive, x, y))) {
								
				areRegionsEqual = false;
				break;
				
			}
			
		}
		
		return !areRegionsEqual;
		
	}
	
}
