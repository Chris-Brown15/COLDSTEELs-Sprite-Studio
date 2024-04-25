package cs.csss.engine;

/**
 * Responsible for managing when a user presses their {@link Control#TWO_D_CURSOR_DRAG} control. When the control is pressed:
 * <ol>
 * 	<li>For the first frame, the cursor's current position is recorded</li>
 * 	<li>For {@link #CURSOR_DRAG_FRAMES} - 1 frames, the control is checked again, and must be pressed to continue</li>
 * 	<li>
 * 		On the {@link #CURSOR_DRAG_FRAMES} frame, if the control is still pressed, a drag axis is selected based on which axis the new current cursor 
 * 		position is farther from.
 *	</li>
 * </ol>
 */
class CursorDragManager {
	
	private static final int CURSOR_DRAG_FRAMES = 3;

	float previousCursorX , previousCursorY;
	
	private float startCursorX , startCursorY;
	
	private CursorDragState cursorDragState = CursorDragState.NOT_DRAGGING;
	
	private int currentDragFrame = 0;
	
	CursorDragManager() {}

	/**
	 * Sets the previous cursor coordinates.
	 */
	void update(float[] cursorCoords) {
		
		previousCursorX = cursorCoords[0];
		previousCursorY = cursorCoords[1];
		
		if(Control.TWO_D_CURSOR_DRAG.pressed()) {

			if(currentDragFrame == 0) {
				
				startCursorX = cursorCoords[0];
				startCursorY = cursorCoords[1];
				currentDragFrame++;
				
			} else if(cursorDragState == CursorDragState.NOT_DRAGGING) { 
								
				if(currentDragFrame == CURSOR_DRAG_FRAMES) {
					
					float xDistance = Math.abs(startCursorX - cursorCoords[0]);
					float yDistance = Math.abs(startCursorY - cursorCoords[1]);
					cursorDragState = xDistance > yDistance ? CursorDragState.DRAGGING_HORIZONTAL : CursorDragState.DRAGGING_VERTICAL;
					
				} else currentDragFrame++;
				
			}
			
		} else {
			
			currentDragFrame = 0;
			cursorDragState = CursorDragState.NOT_DRAGGING;
			
		}
		
	}

	void updateCurrentDragCoords(float[] worldCoords) {
		
		//set the direction we are not dragging to the previous cursor's position for that axis.
		if(cursorDragState == CursorDragState.DRAGGING_HORIZONTAL) worldCoords[1] = startCursorY;
		else if(cursorDragState == CursorDragState.DRAGGING_VERTICAL) worldCoords[0] = startCursorX;
				
	}
	
	private enum CursorDragState {
		
		NOT_DRAGGING ,
		DRAGGING_HORIZONTAL ,
		DRAGGING_VERTICAL;
		
	}
	
}
