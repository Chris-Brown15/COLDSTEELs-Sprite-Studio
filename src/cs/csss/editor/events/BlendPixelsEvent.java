package cs.csss.editor.events;

import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette.PalettePixel;

public class BlendPixelsEvent extends CSSSEvent {

	private int
		bottomLeftX ,
		bottomLeftY ,
		width ,
		height
	;
	
	private final Artboard artboard;
	
	private final PalettePixel activeColor;
	
	public BlendPixelsEvent(
		Artboard artboard , 
		PalettePixel editorActiveColor , 
		int bottomLeftX , 
		int bottomLeftY , 
		int width , 
		int height
	) {

		super(true , false);

		this.bottomLeftX = bottomLeftX;
		this.bottomLeftY = bottomLeftY;
		this.width = width;
		this.height = height;
		
		this.artboard = artboard;
		this.activeColor = editorActiveColor;
		
	}

	@Override public void _do() {
	
		int 
			x = bottomLeftX , 
			y = bottomLeftY
		;
		
		byte[] channelValues = new byte[artboard.activeLayerChannelsPerPixel()];
		
		for(int row = bottomLeftY ; row < bottomLeftY + height ; row++) for(int col = bottomLeftX ; col < bottomLeftX + width ; col++) {
			
			PalettePixel pixel = artboard.getColorPointedToByIndexPixel(x, y);
			
			for(int i = 0 ; i < channelValues.length ; i++) channelValues[i] = (byte) ((pixel.index(i) + activeColor.index(i)) / 2);
			
			PalettePixel average = artboard.createPalettePixel(channelValues); 

			artboard.putColorInImage(col, row, 1, 1, average);
			
		}
		
	}

	@Override public void undo() {

		

	}

}
