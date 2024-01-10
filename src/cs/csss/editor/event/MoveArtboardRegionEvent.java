package cs.csss.editor.event;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.SelectionAreaRender;
import cs.csss.project.Artboard;
import cs.csss.project.LayerPixel;

/**
 * Event for moving a region of an artboard from one place to another.
 */
@RenderThreadOnly public class MoveArtboardRegionEvent extends CSSSEvent {

	private final Artboard current;
	private final SelectionAreaRender render;
	private final int 
		startingX ,
		startingY ,
		startingXArtboardIndex ,
		startingYArtboardIndex ,
		endingXArtboardIndex ,
		endingYArtboardIndex ,
		width ,
		height;
	
	private final LayerPixel[][] regionContents , previousRegionContents;
	
	
	/**
	 * Creates an artboard region move event.
	 * 
	 * @param current — an artboard
	 * @param render — the selection area render representing the moved region
	 */
	public MoveArtboardRegionEvent(Artboard current , SelectionAreaRender render) {
		
		super(true , false);
		
		this.render = render;
		this.width = render.width;
		this.height = render.height;
		
		this.current = current;
				
		this.startingX = render.startingLeftX;
		this.startingY = render.startingBottomY;
		
		int[] indices = current.worldToPixelIndices(startingX , startingY);
		startingXArtboardIndex = indices[0];
		startingYArtboardIndex = indices[1];
		
		int endingX = (int)render.positions.leftX();
		int endingY = (int)render.positions.bottomY();
		
		this.regionContents = render.regionContents();

		indices = current.worldToPixelIndices(endingX , endingY);
		endingXArtboardIndex = indices[0];
		endingYArtboardIndex = indices[1];		

		previousRegionContents = current.getRegionOfLayerPixels(endingXArtboardIndex , endingYArtboardIndex , width , height);
		
	}

	@Override public void _do() {
		
		render.removeSectionFromArtboard(current , startingX , startingY);
		current.putColorsInImage(endingXArtboardIndex, endingYArtboardIndex, width, height, regionContents);
						
	}

	@Override public void undo() {

		current.putColorsInImage(startingXArtboardIndex, startingYArtboardIndex, width, height, regionContents);
		current.removePixels(endingXArtboardIndex, endingYArtboardIndex, width, height);
		current.putColorsInImage(endingXArtboardIndex, endingYArtboardIndex, width, height, previousRegionContents);		
		
	}
	
}
