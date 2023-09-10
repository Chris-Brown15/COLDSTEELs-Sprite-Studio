package cs.csss.engine;

import cs.core.graphics.CSVAO;
import cs.csss.utils.VAOUtils;

/**
 * Helper class for tracking and modifying the positions of world space objects.
 */
public class VAOPosition extends Position {
	
	private final CSVAO vertices;
	
	/**
	 * Creates a position monitor for the given VAO and its positions.
	 * 
	 * @param vertices — vertex array object
	 * @param positions — float world space positions 
	 */
	public VAOPosition(CSVAO vertices , float[] positions) {

		super(positions);
		this.vertices = vertices;
		
	}

 	@Override public void translate(int x , int y) {
		
		translate((float)x , (float)y);
		
		//here we verify that the midpoint of the object is not on a half-pixel boundary.
		float offsetX = midX() % 1;
		float offsetY = midY() % 1;
		
		if(offsetX != 0f || offsetY != 0f) translate(offsetX , offsetY);
		
	} 
	
 	@Override public void translate(float x , float y) {
		
		VAOUtils.translateFloats(vertices, x, y);
		super.updatePositions(x, y);
		
	}
	
}
