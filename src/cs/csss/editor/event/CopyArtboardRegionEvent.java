/**
 * 
 */
package cs.csss.editor.event;

import cs.csss.editor.SelectionAreaRender;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.Position;
import cs.csss.project.Artboard;
import cs.csss.project.utils.Artboards;
import cs.csss.utils.ByteBufferUtils.CorrectedParameters;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Copies a region of the current artboard and pastes it elsewhere on the current artboard.
 */
public class CopyArtboardRegionEvent extends CSSSEvent {

	private final Artboard artboard;
	
	private LookupPixel[][] 
		originalRegion = null ,
		newRegionPreviousContents;
	
	private CorrectedParameters region;
	
	public CopyArtboardRegionEvent(Artboard artboard , SelectionAreaRender render) {
		
		super(true , false);
		this.artboard = artboard;
		Position position = render.positions;
		CorrectedResult corrected = Artboards.worldCoordinatesToCorrectArtboardCoordinates(
			artboard, 
			(int)render.positions.leftX(), 
			(int)render.positions.bottomY(), 
			position.width(), 
			position.height()
		);
			
		region = corrected.params();
		int[] originalPositions = artboard.worldToPixelIndices(render.startingLeftX , render.startingBottomY);
		originalRegion = artboard.getRegionOfLayerPixels(originalPositions[0] , originalPositions[1] , render.width , render.height);
		newRegionPreviousContents = artboard.getRegionOfLayerPixels(region);
		
	}

	@Override public void _do() {
		
		artboard.putColorsInImage(region , originalRegion);
		
	}

	@Override public void undo() {

		artboard.removePixels(region.leftX() , region.bottomY() , region.width() , region.height());
		artboard.putColorsInImage(region, newRegionPreviousContents);
				
	}

}
