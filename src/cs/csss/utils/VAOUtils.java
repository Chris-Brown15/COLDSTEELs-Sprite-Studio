package cs.csss.utils;

import cs.csss.annotation.RenderThreadOnly;
import sc.core.graphics.SCVAO;
import sc.core.graphics.SCVAO.SCVertexBufferAccess;

/**
 * Class providing utilities for modifying artboard positions. In Sprite Studio, artboard movement is accomplished by modifying vertices 
 * rather than by a matrix. This class is used anywhere modification of vertices is done, so DRY is never violated. This class also assumes
 * that the VAOs given are laid out as 2D positions and 2D UVs, which all artboards are.
 */
@RenderThreadOnly public final class VAOUtils {

	private static final int 
		LX = 8 ,
		RX = 0 ,
		BY = 9 ,
		TY = 1;
			
	/**
	 * Translates the given VAO's positions by the floats {@code (x , y)}. 
	 * 
	 * @param vao a vao to translate
	 * @param x x translation
	 * @param y y translation
	 */
	public static void translateFloats(SCVAO vao , float x , float y) {

		vao.activate();
		
		try(SCVertexBufferAccess vertices = vao.mapVBO()) {
		
			translateFloats(vertices , x , y);
			
		}
		
		vao.deactivate();
		
	}
	
	/**
	 * Translates the given VAO's positions by the ints {@code (x , y)}. 
	 * 
	 * @param vao a vao to translate
	 * @param x x translation
	 * @param y y translation
	 */
	public static void translateInts(SCVAO vao , int x , int y) {
		
		vao.activate();
		
		try(SCVertexBufferAccess vertices = vao.mapVBO()) {
			
			translateInts(vertices , x , y);			
			
		}
		
		vao.deactivate();
		
	}
	
	/**
	 * Gets the X midpoint of the vertex buffer of {@code vao}.
	 * 
	 * @param vao a vertex array object
	 * @return The X midpoint of the position attribute of the VAO.
	 */
	public static float midX(SCVAO vao) {

		vao.activate();
		
		float mid = 0;
		
		try(SCVertexBufferAccess vertices = vao.mapVBO()) {
			
			mid = midX(vertices);
			
		}
		
		vao.deactivate();
				
		return mid;
		
	}

	/**
	 * Gets the Y midpoint of the vertex buffer of {@code vao}.
	 * 
	 * @param vao a vertex array object
	 * @return The X midpoint of the position attribute of the VAO.
	 */
	public static float midY(SCVAO vao) {

		vao.activate();
		
		float mid = 0;
		
		try(SCVertexBufferAccess vertices = vao.mapVBO()) {
		
			mid = midY(vertices);
			
		}
		
		vao.deactivate();
				
		return mid;
		
	}

	/**
	 * Moves the given VAO so its midpoint is exactly {@code (worldX , worldY)}.
	 * 
	 * @param vao a vertex array object to move
	 * @param worldX the x position to move to
	 * @param worldY the y position to move to
	 */
	public static void moveToFloats(SCVAO vao , float worldX , float worldY) {
		
		vao.activate();
		
		try(SCVertexBufferAccess vertices = vao.mapVBO()) {
			
			translateFloats(vertices , -midX(vertices) + worldX , -midY(vertices) + worldY);
			
		}
		
		vao.deactivate();
		
	}

	/**
	 * Moves the given VAO so its midpoint is exactly {@code (worldX , worldY)}.
	 * 
	 * @param vao a vertex array object to move
	 * @param worldX the x position to move to
	 * @param worldY the y position to move to
	 */
	public static void moveToInts(SCVAO vao , int worldX , int worldY) {
		
		vao.activate();
		
		try(SCVertexBufferAccess vertices = vao.mapVBO()) {
			
			translateInts(vertices , (int)-midX(vertices) + worldX , (int)-midY(vertices) + worldY);
			
		}
		
		vao.deactivate();
		
	}
	
	/**
	 * Gets and returns the width of the given VAO.
	 * 
	 * @param vao a vertex array object 
	 * @return Width of the vertices of this vertex array object.
	 */
	public static float width(SCVAO vao) {
		
		vao.activate();
		
		float width;
		
		try(SCVertexBufferAccess buffer = vao.mapVBO()) {
			
			width = width(buffer);
					
		}
		
		vao.deactivate();
		
		return width;
		
	}

	/**
	 * Gets and returns the height of the given VAO.
	 * 
	 * @param vao a vertex array object 
	 * @return Height of the vertices of this vertex array object.
	 */
	public static float height(SCVAO vao) {
		
		vao.activate();
		
		float height;
		
		try(SCVertexBufferAccess buffer = vao.mapVBO()) {
			
			height = height(buffer);
					
		}
		
		vao.deactivate();
		
		return height;
		
	}
	
	/**
	 * Gets and returns the left x coordinate of the given VAO.
	 * 
	 * @param vao vertex array object
	 * @return Left x coordinate of this object .
	 */
	public static float leftX(SCVAO vao) {

		vao.activate();
		
		float leftX;
		
		try(SCVertexBufferAccess buffer = vao.mapVBO()) {
			
			leftX = buffer.memory().getFloat(asFloatIndex(LX));
					
		}
		
		vao.deactivate();
		
		return leftX;
		
	}

	/**
	 * Gets and returns the right x coordinate of the given VAO.
	 * 
	 * @param vao vertex array object
	 * @return Right x coordinate of this object .
	 */
	public static float rightX(SCVAO vao) {

		vao.activate();
		
		float x;
		
		try(SCVertexBufferAccess buffer = vao.mapVBO()) {
			
			x = buffer.memory().getFloat(asFloatIndex(RX));
					
		}
		
		vao.deactivate();
		
		return x;
		
	}

	/**
	 * Gets and returns the bottom y coordinate of the given VAO.
	 * 
	 * @param vao vertex array object
	 * @return Bottom y coordinate of this object.
	 */
	public static float bottomY(SCVAO vao) {

		vao.activate();
		
		float y;
		
		try(SCVertexBufferAccess buffer = vao.mapVBO()) {
			
			y = buffer.memory().getFloat(asFloatIndex(BY));
					
		}
		
		vao.deactivate();
		
		return y;
		
	}

	/**
	 * Gets and returns the top y coordinate of the given VAO.
	 * 
	 * @param vao vertex array object
	 * @return Top y coordinate of this object .
	 */
	public static float topX(SCVAO vao) {

		vao.activate();
		
		float y;
		
		try(SCVertexBufferAccess buffer = vao.mapVBO()) {
			
			y = buffer.memory().getFloat(asFloatIndex(TY));
					
		}
		
		vao.deactivate();
		
		return y;
		
	}
	
	/**
	 * Translates the vertices contained within the buffer access {@code buffer}.
	 * 
	 * @param buffer buffer access created from a VAO
	 * @param x x translation
	 * @param y y translation
	 */
	public static void translateFloats(SCVertexBufferAccess buffer , float x , float y) {

		buffer.memory().putFloat(0 , buffer.memory().getFloat(0) + x).putFloat(1 << 2 , buffer.memory().getFloat(1 << 2) + y);
		buffer.memory().putFloat(4 << 2 , buffer.memory().getFloat(4 << 2) + x).putFloat(5 << 2 , buffer.memory().getFloat(5 << 2) + y);
		buffer.memory().putFloat(8 << 2 , buffer.memory().getFloat(8 << 2) + x).putFloat(9 << 2 , buffer.memory().getFloat(9 << 2) + y);
		buffer.memory().putFloat(12 << 2 , buffer.memory().getFloat(12 << 2) + x).putFloat(13 << 2 ,buffer.memory().getFloat(13 << 2) + y);
		
	}

	/**
	 * Translates the vertices contained within the buffer access {@code buffer}. This also ensures the vertices cannot lie on sub-pixel
	 * boundaries.
	 *  
	 * @param buffer buffer access created from a VAO
	 * @param x x translation
	 * @param y y translation
	 */
	public static void translateInts(SCVertexBufferAccess buffer , int x , int y) {

		translateFloats(buffer , x , y);

		//here we verify that the midpoint of the object is not on a half-pixel boundary.
		float offsetX = midX(buffer) % 1;
		float offsetY = midY(buffer) % 1;
		
		if(offsetX != 0f || offsetY != 0f) translateFloats(buffer , offsetX , offsetY);
		
	}
	
	/**
	 * Gets the X midpoint of the positions contaiend within {@code buffer}.
	 * 
	 * @param buffer a buffer access to get the positions from
	 * @return The X midpoint of the positions of the buffer.
	 */
	public static float midX(SCVertexBufferAccess buffer) {
		
		float leftX = buffer.memory().getFloat(asFloatIndex(LX));
		
		float halfWidth = (buffer.memory().getFloat(RX) - leftX) / 2; 
		return leftX + halfWidth;
		
	}
	
	/**
	 * Gets the Y midpoint of the positions contained within {@code buffer}.
	 * 
	 * @param buffer a buffer access to get the positions from
	 * @return The Y midpoint of the positions of the buffer.
	 */
	public static float midY(SCVertexBufferAccess buffer) {

		float bottomY = buffer.memory().getFloat(asFloatIndex(BY));
		
		float halfHeight = (buffer.memory().getFloat(asFloatIndex(1)) - bottomY) / 2;
		return bottomY + halfHeight;
		
	}
	
	/**
	 * Performs an {@code int} move operation on the given buffer. The position of the vertices will be centered around the coordinate 
	 * {@code (x , y)}.
	 * 
	 * @param buffer a buffer to move
	 * @param x an x world coordinate
	 * @param y a y world coordinate
	 */
	public static void moveTo(SCVertexBufferAccess buffer , int x , int y) {
		
		int midX = (int)midX(buffer);
		int midY = (int)midY(buffer);
		
		translateInts(buffer , -midX + x , -midY + y);
		
	}

	/**
	 * Performs a {@code float} move operation on the given buffer. The position of the vertices will be centered around the coordinate 
	 * {@code (x , y)}.
	 * 
	 * @param buffer a buffer to move
	 * @param x an x world coordinate
	 * @param y a y world coordinate
	 */
	public static void moveTo(SCVertexBufferAccess buffer , float x , float y) {
	
		float midX = midX(buffer);
		float midY = midY(buffer);
		
		translateFloats(buffer , -midX + x , -midY + y);
		
	}
	
	/**
	 * Gets the width of the vertices contained within {@code buffer}.
	 * 
	 * @param buffer a buffer acces
	 * @return Width of the vertices contained within {@code buffer}.
	 */ 
	public static float width(SCVertexBufferAccess buffer) {
		
		return buffer.memory().getFloat(asFloatIndex(RX)) - buffer.memory().getFloat(asFloatIndex(LX));
		
	}

	/**
	 * Gets the height of the vertices contained within {@code buffer}.
	 * 
	 * @param buffer a buffer acces
	 * @return Height of the vertices contained within {@code buffer}.
	 */ 
	public static float height(SCVertexBufferAccess buffer) {
		
		return buffer.memory().getFloat(asFloatIndex(TY)) - buffer.memory().getFloat(asFloatIndex(BY));
		
	}
	
	private static int asFloatIndex(int index) {
	
		return index << 2;
		
	}			
	
	private VAOUtils() {}

}
