package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.ModifyArtboardImageEvent;
import cs.csss.project.Artboard;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.project.ArtboardPalette.PalettePixel;

public class PencilBrush extends CSSSModifyingBrush {

	public PencilBrush() {

		super("Pencil colors artboards by clicking on pixels within them.");

	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		if(radius == 0) return new ModifyArtboardImageEvent(artboard, xIndex, yIndex, 1, 1 , editor.selectedColors(artboard));

		int[] region = centerAroundRadius(xIndex, yIndex , artboard.width() , artboard.height());
		
		return new ModifyArtboardImageEvent(artboard , region[0] , region[1] , region[2] , region[3] , editor.selectedColors(artboard));
		
	}

	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		/*
		 * The pencil can be used if any pixel modification of the given layer does not match the editor selected color.
		 */
		 
		//this array's contents are the x and y index of the bottom left corner of the modified region, and the diameter of the 
		//modification.
		int[] region = centerAroundRadius(xIndex, yIndex , artboard.width() , artboard.height());
		
		PalettePixel editorActive = editor.selectedColors(artboard);
		short[] indices = artboard.putInPalette(editorActive);
		
		Layer activeLayer = artboard.activeLayer();
		
		boolean areRegionsEqual = true;
		int x , y;
		
		//starts at the bottom left corner of the modified region and iterates over all pixels within it.
		//regions are not equal if at least one pixel in the region does not satisfy the condition below
		for(int row = 0 ; row < region[3] ; row++) for(int col = 0 ; col < region[2] ; col++) {
			
			x = region[0] + col ; y = region[1] + row;
			
			LayerPixel at = activeLayer.get(x , y); 
			
			//the condition inside the parentheses will be true when the pixel does match the current iteration's pixel
			if(!(activeLayer.containsModificationTo(x , y) && at.lookupX == indices[0] && at.lookupY == indices[1])) {
				
				areRegionsEqual = false;
				break;
				
			}
			
		}
		
		return !areRegionsEqual;
		
	}
	
}
