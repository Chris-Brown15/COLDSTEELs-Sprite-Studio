package cs.csss.engine;

import org.joml.Matrix4f;

/**
 * Used to express translations on positions via {@code Matrix4f}.
 */
public class TransformPosition extends Position {

	public final Matrix4f translation = new Matrix4f();
	
	/**
	 * Creates a transform position.
	 * 
	 * @param positions — position array
	 */
	public TransformPosition(float[] positions) {

		super(positions);

	}
	
	@Override public void translate(int x, int y) {

		translate((float)x , (float)y);
				
	}

	@Override public void translate(float x, float y) {

		translation.translate(x , y , 0);
		super.updatePositions(x, y);
		
	}

}
