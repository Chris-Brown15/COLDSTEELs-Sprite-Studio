package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.SelectionAreaRender;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.Position;
import cs.csss.project.Artboard;
import cs.csss.project.utils.Artboards;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Event for moving a region of an artboard from one place to another.
 */
@RenderThreadOnly public class MoveArtboardRegionEvent extends CSSSEvent {

	private final Artboard artboard;
	private final CorrectedResult newPosition;
	
	private final LookupPixel[][] contentsOfMovedRegion , previousContentsOfMovedToRegion;
	
	int movedRegionX , movedRegionY , width , height;
	
	/**
	 * Creates an artboard region move event.
	 * 
	 * @param current — an artboard
	 * @param render — the selection area render representing the moved region
	 */
	public MoveArtboardRegionEvent(Artboard current , SelectionAreaRender render) {
		
		super(true , false);
		
		this.artboard = current;

		Position position = render.positions;		
		
		int[] renderStartingPositionAsArtboardCoords = current.worldToPixelIndices(render.startingLeftX , render.startingBottomY);
		movedRegionX = renderStartingPositionAsArtboardCoords[0];
		movedRegionY = renderStartingPositionAsArtboardCoords[1];
		width = render.width;
		height = render.height;
				
		this.contentsOfMovedRegion = current.getRegionOfLayerPixels(movedRegionX, movedRegionY, width, height);
		
		newPosition = Artboards.worldCoordinatesToCorrectArtboardCoordinates(
			current, 
			(int)position.leftX(), 
			(int)position.bottomY(), 
			position.width(), 
			position.height()
		);
		
		previousContentsOfMovedToRegion = current.getRegionOfLayerPixels(
			newPosition.leftX() , 
			newPosition.bottomY() , 
			newPosition.width() , 
			newPosition.height()
		);
		
	}

	@Override public void _do() {
		
		artboard.removePixels(movedRegionX, movedRegionY, width, height);
		artboard.replace(newPosition , contentsOfMovedRegion);

	}

	@Override public void undo() {

		artboard.putColorsInImage(movedRegionX, movedRegionY , width , height , contentsOfMovedRegion);
		artboard.replace(newPosition , previousContentsOfMovedToRegion);		
		
	}
	
}
