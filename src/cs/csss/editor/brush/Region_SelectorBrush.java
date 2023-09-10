package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.MoveArtboardRegionEvent;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;
import cs.csss.utils.CollisionUtils;

/**
 * Brush used to move regions of a layer around.
 */
public class Region_SelectorBrush extends CSSSSelectingBrush {

	private boolean draggingLastFrame = false;
	private boolean canUse = false;
		
	public Region_SelectorBrush() {

		super("Selects regions of pixels on an artboard and move them around.");
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		canUse = false;
		MoveArtboardRegionEvent event = new MoveArtboardRegionEvent(artboard , render);
		resetRender();
		return event;
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return canUse;
		
	}
	
	@Override public void update(Artboard current , Editor editor) {

		if(!editor.cursorInBoundsForBrush()) return;
		
		float[] cursorCoords = editor.cursorCoords();
		int[] cursorAsInt = {(int)cursorCoords[0] , (int)cursorCoords[1]};

		defaultUpdateBounder(cursorAsInt[0] , cursorAsInt[1]);
		
		if(current == null) return;
		
		selectionBounder.snapBounderToCoordinates((int)current.leftX(), (int)current.rightX(), (int)current.bottomY(), (int)current.topY());
		
		boolean currentlyDragging = Control.ARTBOARD_INTERACT.pressed();
		
		//will be true when we begin to press down the dragging key
		if(!draggingLastFrame && currentlyDragging) editor.rendererPost(() -> newRender(current , editor , cursorAsInt));
		//true while we are pressing the mouse to drag down
		else if(draggingLastFrame && currentlyDragging) editor.rendererPost(() -> render.positions.moveTo(cursorAsInt[0] , cursorAsInt[1]));
		//will be true when we release the mouse button and 'place down' our region
		else if(draggingLastFrame && !currentlyDragging) { 
			
			if(CollisionUtils.colliding(render.positions, current.positions)) {
				
				canUse = true;
				selectionBounder.positions(
					(int)render.positions.leftX() , 
					(int)render.positions.rightX() , 
					(int)render.positions.bottomY() , 
					(int)render.positions.topY()
				);
			
			}
			//if the render is completely off the artboard, don't do anything.
			else editor.rendererPost(this::resetRender); 
			
		}
			
		draggingLastFrame = currentlyDragging;

	}
	
	private void resetRender() {
		
		render.shutDown();
		render = null;
		
	}
	
}