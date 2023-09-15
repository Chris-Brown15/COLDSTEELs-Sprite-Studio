package cs.csss.editor.brush;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.ScaleRegionEvent;
import cs.csss.engine.Control;
import cs.csss.project.Artboard;

/**
 * Brush used to increase or decrease the size of a region of pixels.
 */
@RenderThreadOnly public class Scale_RegionBrush extends CSSSSelectingBrush {

	private boolean
		wasScalingLastUpdate = false ,
		canUse;

	private float previousYPosition = Float.NEGATIVE_INFINITY;
	
	/**
	 * Creates a new scaling brush.
	 */
	public Scale_RegionBrush() {
		
		super("Scales the selected region up or down.");
		selectionBounder.color = 0xffff00ff;
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		canUse = false;
		previousYPosition = Float.NEGATIVE_INFINITY;
		ScaleRegionEvent event = new ScaleRegionEvent(artboard, editor , render , editor::swapBuffers);
		render = null;
		return event;
		
	}

	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return canUse;
		
	}
	
	@Override public void update(Artboard current, Editor editor) {

		float[] cursorPosition = editor.cursorCoords();
		
		float currentYPosition = cursorPosition[1];
		if(Control.MOVE_SELECTION_AREA.pressed()) selectionBounder.moveCorner((int)cursorPosition[0] , (int)currentYPosition);

		if(previousYPosition == Float.NEGATIVE_INFINITY) previousYPosition = currentYPosition;
		
		if(current == null) return; 
				
		selectionBounder.snapBounderToCoordinates((int)current.leftX(), (int)current.rightX(), (int)current.bottomY(), (int)current.topY());
		
		if(!editor.cursorInBoundsForBrush()) return;
		
		boolean scaling = Control.ARTBOARD_INTERACT.pressed();
		
		if(scaling && !wasScalingLastUpdate) editor.rendererPost(() -> newRender(current, editor)).await();
		else if (scaling && wasScalingLastUpdate) {
			
			if(currentYPosition > previousYPosition) scale(true);
			else if(currentYPosition < previousYPosition) scale(false);
			
		} else if (!scaling && wasScalingLastUpdate) canUse = true;

		previousYPosition = currentYPosition;
		wasScalingLastUpdate = scaling;
				
	}

	private void scale(boolean up) {
		
		Matrix4f translation = render.positions.translation;
		Vector3f scale = new Vector3f();

		translation.getScale(scale);
		
		float scaledWidth = render.width * scale.x;
		float scaledHeight = render.height * scale.y;

		translation.scale(up ? 1f + (1 / scaledWidth) : 1f - (1 / scaledHeight));

	}
	
}
