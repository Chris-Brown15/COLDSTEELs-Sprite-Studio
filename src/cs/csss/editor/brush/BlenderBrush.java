package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.events.BlendPixelsEvent;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette.PalettePixel;
import cs.csss.project.IndexPixel;

public class BlenderBrush extends CSSSModifyingBrush {

	public BlenderBrush() {

		super("Blends the clicked pixels with the editor's active pixel." , true);
		
	}
	
	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		if(radius == 0) return new BlendPixelsEvent(artboard , editor.selectedColors(artboard) , xIndex , yIndex , 1 , 1);
		
		int[] region = centerAroundRadius(xIndex, yIndex , artboard.width() , artboard.height());
		
		return new BlendPixelsEvent(artboard , editor.selectedColors(artboard) , region[0] , region[1] , region[2] , region[3]);
		
	}
	
	@Override public boolean canUse(Artboard artboard , Editor editor , int xIndex , int yIndex) {

		if(!super.canUse(artboard, editor, xIndex, yIndex)) return false;
		
		/*
		 * Can use if at least one pixel in the region does not match the editor-selected one.
		 */
		
		PalettePixel selected = editor.selectedColors(artboard);
		
		if(radius == 0) {
			
			PalettePixel color = artboard.getColorPointedToByIndexPixel(xIndex, yIndex);
			return color.compareTo(selected) != 0;
			
		}

		int[] values = centerAroundRadius(xIndex, yIndex , artboard.width() , artboard.height());
				
		IndexPixel[][] region = artboard.getRegionOfIndexPixels(values[0] , values[1] , values[2] , values[3]);
		
		for(int row = 0 ; row < values[3] ; row++) for(int col = 0 ; col < values[2] ; col++) { 
		
			IndexPixel currentIndexPixel = region[row][col];
			PalettePixel currentIndexPixelColor = artboard.getColorFromIndicesOfPalette(
				currentIndexPixel.xIndex , 
				currentIndexPixel.yIndex
			);
			
			if(selected.compareTo(currentIndexPixelColor) != 0) return true;
			
		}
		
		return false;
	
	}

	@Override public void update(Artboard artboard , Editor editor) {}
	
}
