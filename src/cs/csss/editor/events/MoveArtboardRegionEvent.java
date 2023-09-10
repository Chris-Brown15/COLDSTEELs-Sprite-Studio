package cs.csss.editor.events;

import cs.csss.editor.SelectionAreaRender;
import cs.csss.project.Artboard;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.utils.ByteBufferUtils;

/**
 * Event for moving a region of an artboard from one place to another.
 */
public class MoveArtboardRegionEvent extends CSSSEvent {

	private final Artboard current;
	private final SelectionAreaRender render;
	private final int 
		startingX ,
		startingY ,
		endingX ,
		endingY ,
		width ,
		height;
	
	private final LayerPixel[][] regionContents;
	
	public MoveArtboardRegionEvent(Artboard current , SelectionAreaRender render) {
		
		super(true , false);
		
		this.render = render;
		this.width = render.width;
		this.height = render.height;
		
		this.current = current;
				
		this.startingX = render.startingLeftX;
		this.startingY = render.startingBottomY;
		
		this.endingX = (int)render.positions.leftX();
		this.endingY = (int)render.positions.bottomY();
		
		this.regionContents = render.regionContents();
		
	}

	@Override public void _do() {
		
		render.removeSectionFromArtboard(current , startingX , startingY);

		int[] indices = current.worldToPixelIndices(endingX , endingY);

		ByteBufferUtils.putBufferInArtboard(
			current , 
			indices[0] , 
			indices[1] , 
			width , 
			height , 
			Layer.toByteBuffer(regionContents) , 
			(byte1 , byte2) -> byte1 == 0 && byte2 == 0
		);
		
	}

	@Override public void undo() {

		render.removeSectionFromArtboard(current , endingX , endingY);
		int[] indices = current.worldToPixelIndices(startingX , startingY);

		ByteBufferUtils.putBufferInArtboard(
			current , 
			indices[0] , 
			indices[1] , 
			width , 
			height , 
			Layer.toByteBuffer(regionContents), 
			(byte1 , byte2) -> byte1 == 0 && byte2 == 0
		);
		
	}

}
