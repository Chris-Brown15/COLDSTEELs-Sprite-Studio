package cs.csss.editor.brush;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardPalette.PalettePixel;
import cs.csss.artboard.ArtboardTexture.IndexPixel;
import cs.csss.editor.Editor;
import cs.csss.editor.events.BlendPixelsEvent;
import cs.csss.editor.events.CSSSEvent;

public class BlenderBrush extends CSSSModifyingBrush {

	public BlenderBrush() {

		super("Blends the clicked pixels with the editor's active pixel.");
		
	}
	
	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		if(radius == 0) return new BlendPixelsEvent(artboard , editor.selectedColors(artboard) , xIndex , yIndex , 1 , 1);
		
		int[] region = centerAroundRadius(xIndex, yIndex);
		
		return new BlendPixelsEvent(artboard , editor.selectedColors(artboard) , region[0] , region[1] , region[2] , region[2]);
		
	}
	
	@Override public boolean canUse(Artboard artboard , Editor editor , int xIndex , int yIndex) {

		/*
		 * Can use if at least one pixel in the region does not match the editor-selected one.
		 */
		
		PalettePixel selected = editor.selectedColors(artboard);
		
		if(radius == 0) {
			
			PalettePixel color = artboard.getColorPointedToByIndexPixel(xIndex, yIndex);
			return color.compareTo(selected) != 0;
			
		}

		int[] values = centerAroundRadius(xIndex, yIndex);
		
		int 
			sizeX = values[2] ,
			sizeY = values[2]
		; 
		
		if(values[0] + sizeX > artboard.width()) values[0] -= (artboard.width() - values[0]) + 1;		
		if(values[1] + sizeY > artboard.height()) values[1] -= (artboard.height() - values[1]) + 1;
		
		IndexPixel[][] region = artboard.getRegionOfIndexPixels(values[0] , values[1] , sizeX , sizeY);
		
		for(int row = 0 ; row < sizeY ; row++) for(int col = 0 ; col < sizeX ; col++) { 
		
			IndexPixel currentIndexPixel = region[row][col];
			PalettePixel currentIndexPixelColor = artboard.getColorFromIndicesOfPalette(currentIndexPixel.xIndex , currentIndexPixel.yIndex);
			
			if(selected.compareTo(currentIndexPixelColor) != 0) return true;
			
		}
		
		return false;
	
	}

}
