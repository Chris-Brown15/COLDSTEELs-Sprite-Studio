package cs.csss.editor.events;

import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;

public class DeleteRegionEvent extends CSSSEvent {

	private final Artboard artboard;
	private final int
		xIndex ,
		yIndex ,
		width ,
		height
	;
	
	private final LayerPixel[][] region;
	
	public DeleteRegionEvent(Artboard artboard , int xIndex , int yIndex , int width , int height , LayerPixel[][] region) {

		super(true , false);
		
		this.artboard = artboard;
		
		this.region = region;
		
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.width = width;
		this.height = height;
		
	}

	@Override public void _do() {

		artboard.removePixels(xIndex, yIndex, width, height);
				
	}

	@Override public void undo() {

		artboard.putColorsInImage(xIndex, yIndex, width, height, region);
		
	}

}
