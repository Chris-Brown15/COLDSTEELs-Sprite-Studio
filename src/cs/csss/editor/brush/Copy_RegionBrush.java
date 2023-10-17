/**
 * 
 */
package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.CopyArtboardRegionEvent;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;
import cs.csss.utils.CollisionUtils;

/**
 * 
 */
public class Copy_RegionBrush extends CSSSSelectingBrush {

	private boolean 
		draggingLastFrame = false ,
		canUse = false;
	
	public Copy_RegionBrush() {

		super("Makes a copy of the pixels of the selected region on the current artboard.");

	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		canUse = false;
		CopyArtboardRegionEvent event = new CopyArtboardRegionEvent(artboard , render);
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
		
		selectionBounder.snapBounderToCoordinates((int)current.leftX(), (int)current.rightX() , (int)current.bottomY() , (int)current.topY());
		
		boolean currentlyDragging = Control.ARTBOARD_INTERACT.pressed();

		//will be true when we begin to press down the dragging key
		if(!draggingLastFrame && currentlyDragging) editor.rendererPost(() -> newRender(current , editor , cursorAsInt));
		//true while we are pressing the mouse to drag down
		else if(draggingLastFrame && currentlyDragging) editor.rendererPost(() -> render.positions.moveTo(cursorAsInt[0] , cursorAsInt[1]));
		else if(draggingLastFrame && !currentlyDragging) {
			
			if(CollisionUtils.colliding(render.positions, current.positions)) {
				
				canUse = true;
				selectionBounder.positions(
					(int)render.positions.leftX(), 
					(int)render.positions.rightX(), 
					(int)render.positions.bottomY(), 
					(int)render.positions.topY()
				);				
				
			} else editor.rendererPost(CSSSSelectingBrush::resetRender);
			
		}
		
		draggingLastFrame = currentlyDragging;

	}
	
}
