package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.editor.events.RotateRegionEvent;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;

public class RotateBrush extends CSSSSelectingBrush {

	private volatile boolean 
		wasInteractingLastFrame = false ,
		canUse = false;
	
	private float previousCursorY = Float.NEGATIVE_INFINITY;
	
	public RotateBrush() {
		
		super("Rotates the selected region.");		
	
		selectionBounder.color = 0xeeff;
		selectionBounder.thickness = 2.0f;
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		canUse = false;
		previousCursorY = Float.NEGATIVE_INFINITY;
		RotateRegionEvent event = new RotateRegionEvent(artboard , editor , render , selectionBounder , editor::swapBuffers);		
		render = null;		
		return event;
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
			
		return canUse;
		
	}

	@Override public void update(Artboard artboard , Editor editor) {
		
		float[] cursorCoords = editor.cursorCoords();
		float currentCursorY = cursorCoords[1];
		
		if(previousCursorY == Float.NEGATIVE_INFINITY) previousCursorY = currentCursorY;
		
		int[] cursorCoordInts = {(int)cursorCoords[0] , (int)cursorCoords[1]};
		defaultUpdateBounder(cursorCoordInts[0] , cursorCoordInts[1]);
		
		if(artboard == null) return; 
		
		selectionBounder.snapBounderToCoordinates(
			(int)artboard.leftX() , 
			(int)artboard.rightX() , 
			(int)artboard.bottomY() , 
			(int)artboard.topY()
		);
			
		if(!editor.cursorInBoundsForBrush()) return;
		
		boolean currentlyInteracting = Control.ARTBOARD_INTERACT.pressed(); 
		
		//starting rotation
		if(!wasInteractingLastFrame && currentlyInteracting) editor.rendererPost(() -> newRender(artboard, editor));
		//continuing rotation
		else if (wasInteractingLastFrame && currentlyInteracting) {
			
			if(currentCursorY > previousCursorY) render.rotate(-1f);
			else if(currentCursorY < previousCursorY) render.rotate(1f);
			
		}
		//end rotation, render rotated sprite, and download region
		else if(wasInteractingLastFrame && !currentlyInteracting) canUse = true;
				
		wasInteractingLastFrame = currentlyInteracting;
		previousCursorY = cursorCoords[1];
		
	}
	
}
