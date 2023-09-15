package cs.csss.utils;

import cs.core.graphics.CSVAO;
import cs.core.graphics.CSVAO.VertexBufferAccess;
import cs.csss.annotation.RenderThreadOnly;

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
	 * @param vao — a vao to translate
	 * @param x — x translation
	 * @param y — y translation
	 */
	public static void translateFloats(CSVAO vao , float x , float y) {

		vao.activate();
		
		try(VertexBufferAccess vertices = vao.new VertexBufferAccess()) {
		
			translateFloats(vertices , x , y);
			
		}
		
		vao.deactivate();
		
	}
	
	/**
	 * Translates the given VAO's positions by the ints {@code (x , y)}. 
	 * 
	 * @param vao — a vao to translate
	 * @param x — x translation
	 * @param y — y translation
	 */
	public static void translateInts(CSVAO vao , int x , int y) {
		
		vao.activate();
		
		try(VertexBufferAccess vertices = vao.new VertexBufferAccess()) {
			
			translateInts(vertices , x , y);			
			
		}
		
		vao.deactivate();
		
	}
	
	/**
	 * Gets the X midpoint of the vertex buffer of {@code vao}.
	 * 
	 * @param vao — a vertex array object
	 * @return The X midpoint of the position attribute of the VAO.
	 */
	public static float midX(CSVAO vao) {

		vao.activate();
		
		float mid = 0;
		
		try(VertexBufferAccess vertices = vao.new VertexBufferAccess()) {
			
			mid = midX(vertices);
			
		}
		
		vao.deactivate();
				
		return mid;
		
	}

	/**
	 * Gets the Y midpoint of the vertex buffer of {@code vao}.
	 * 
	 * @param vao — a vertex array object
	 * @return The X midpoint of the position attribute of the VAO.
	 */
	public static float midY(CSVAO vao) {

		vao.activate();
		
		float mid = 0;
		
		try(VertexBufferAccess vertices = vao.new VertexBufferAccess()) {
		
			mid = midY(vertices);
			
		}
		
		vao.deactivate();
				
		return mid;
		
	}

	/**
	 * Moves the given VAO so its midpoint is exactly {@code (worldX , worldY)}.
	 * 
	 * @param vao — a vertex array object to move
	 * @param worldX — the x position to move to
	 * @param worldY — the y position to move to
	 */
	public static void moveToFloats(CSVAO vao , float worldX , float worldY) {
		
		vao.activate();
		
		try(VertexBufferAccess vertices = vao.new VertexBufferAccess()) {
			
			translateFloats(vertices , -midX(vertices) + worldX , -midY(vertices) + worldY);
			
		}
		
		vao.deactivate();
		
	}

	/**
	 * Moves the given VAO so its midpoint is exactly {@code (worldX , worldY)}.
	 * 
	 * @param vao — a vertex array object to move
	 * @param worldX — the x position to move to
	 * @param worldY — the y position to move to
	 */
	public static void moveToInts(CSVAO vao , int worldX , int worldY) {
		
		vao.activate();
		
		try(VertexBufferAccess vertices = vao.new VertexBufferAccess()) {
			
			translateInts(vertices , (int)-midX(vertices) + worldX , (int)-midY(vertices) + worldY);
			
		}
		
		vao.deactivate();
		
	}
	
	/**
	 * Gets and returns the width of the given VAO.
	 * 
	 * @param vao — a vertex array object 
	 * @return Width of the vertices of this vertex array object.
	 */
	public static float width(CSVAO vao) {
		
		vao.activate();
		
		float width;
		
		try(VertexBufferAccess buffer = vao.new VertexBufferAccess()) {
			
			width = width(buffer);
					
		}
		
		vao.deactivate();
		
		return width;
		
	}

	/**
	 * Gets and returns the height of the given VAO.
	 * 
	 * @param vao — a vertex array object 
	 * @return Height of the vertices of this vertex array object.
	 */
	public static float height(CSVAO vao) {
		
		vao.activate();
		
		float height;
		
		try(VertexBufferAccess buffer = vao.new VertexBufferAccess()) {
			
			height = height(buffer);
					
		}
		
		vao.deactivate();
		
		return height;
		
	}
	
	/**
	 * Gets and returns the left x coordinate of the given VAO.
	 * 
	 * @param vao — vertex array object
	 * @return Left x coordinate of this object .
	 */
	public static float leftX(CSVAO vao) {

		vao.activate();
		
		float leftX;
		
		try(VertexBufferAccess buffer = vao.new VertexBufferAccess()) {
			
			leftX = buffer.getFloat(asFloatIndex(LX));
					
		}
		
		vao.deactivate();
		
		return leftX;
		
	}

	/**
	 * Gets and returns the right x coordinate of the given VAO.
	 * 
	 * @param vao — vertex array object
	 * @return Right x coordinate of this object .
	 */
	public static float rightX(CSVAO vao) {

		vao.activate();
		
		float x;
		
		try(VertexBufferAccess buffer = vao.new VertexBufferAccess()) {
			
			x = buffer.getFloat(asFloatIndex(RX));
					
		}
		
		vao.deactivate();
		
		return x;
		
	}

	/**
	 * Gets and returns the bottom y coordinate of the given VAO.
	 * 
	 * @param vao — vertex array object
	 * @return Bottom y coordinate of this object.
	 */
	public static float bottomY(CSVAO vao) {

		vao.activate();
		
		float y;
		
		try(VertexBufferAccess buffer = vao.new VertexBufferAccess()) {
			
			y = buffer.getFloat(asFloatIndex(BY));
					
		}
		
		vao.deactivate();
		
		return y;
		
	}

	/**
	 * Gets and returns the top y coordinate of the given VAO.
	 * 
	 * @param vao — vertex array object
	 * @return Top y coordinate of this object .
	 */
	public static float topX(CSVAO vao) {

		vao.activate();
		
		float y;
		
		try(VertexBufferAccess buffer = vao.new VertexBufferAccess()) {
			
			y = buffer.getFloat(asFloatIndex(TY));
					
		}
		
		vao.deactivate();
		
		return y;
		
	}
	
	/**
	 * Translates the vertices contained within the buffer access {@code buffer}.
	 * 
	 * @param buffer — buffer access created from a VAO
	 * @param x — x translation
	 * @param y — y translation
	 */
	public static void translateFloats(VertexBufferAccess buffer , float x , float y) {

		buffer.putFloat(0 , buffer.getFloat(0) + x).putFloat(1 << 2 , buffer.getFloat(1 << 2) + y);
		buffer.putFloat(4 << 2 , buffer.getFloat(4 << 2) + x).putFloat(5 << 2 , buffer.getFloat(5 << 2) + y);
		buffer.putFloat(8 << 2 , buffer.getFloat(8 << 2) + x).putFloat(9 << 2 , buffer.getFloat(9 << 2) + y);
		buffer.putFloat(12 << 2 , buffer.getFloat(12 << 2) + x).putFloat(13 << 2 ,buffer.getFloat(13 << 2) + y);
		
	}

	/**
	 * Translates the vertices contained within the buffer access {@code buffer}. This also ensures the vertices cannot lie on sub-pixel
	 * boundaries.
	 *  
	 * @param buffer — buffer access created from a VAO
	 * @param x — x translation
	 * @param y — y translation
	 */
	public static void translateInts(VertexBufferAccess buffer , int x , int y) {

		translateFloats(buffer , x , y);

		//here we verify that the midpoint of the object is not on a half-pixel boundary.
		float offsetX = midX(buffer) % 1;
		float offsetY = midY(buffer) % 1;
		
		if(offsetX != 0f || offsetY != 0f) translateFloats(buffer , offsetX , offsetY);
		
	}
	
	/**
	 * Gets the X midpoint of the positions contaiend within {@code buffer}.
	 * 
	 * @param buffer — a buffer access to get the positions from
	 * @return The X midpoint of the positions of the buffer.
	 */
	public static float midX(VertexBufferAccess buffer) {
		
		float leftX = buffer.getFloat(asFloatIndex(LX));
		
		float halfWidth = (buffer.getFloat(RX) - leftX) / 2; 
		return leftX + halfWidth;
		
	}
	
	/**
	 * Gets the Y midpoint of the positions contained within {@code buffer}.
	 * 
	 * @param buffer — a buffer access to get the positions from
	 * @return The Y midpoint of the positions of the buffer.
	 */
	public static float midY(VertexBufferAccess buffer) {

		float bottomY = buffer.getFloat(asFloatIndex(BY));
		
		float halfHeight = (buffer.getFloat(asFloatIndex(1)) - bottomY) / 2;
		return bottomY + halfHeight;
		
	}
	
	/**
	 * Performs an {@code int} move operation on the given buffer. The position of the vertices will be centered around the coordinate 
	 * {@code (x , y)}.
	 * 
	 * @param buffer — a buffer to move
	 * @param x — an x world coordinate
	 * @param y — a y world coordinate
	 */
	public static void moveTo(VertexBufferAccess buffer , int x , int y) {
		
		int midX = (int)midX(buffer);
		int midY = (int)midY(buffer);
		
		translateInts(buffer , -midX + x , -midY + y);
		
	}

	/**
	 * Performs a {@code float} move operation on the given buffer. The position of the vertices will be centered around the coordinate 
	 * {@code (x , y)}.
	 * 
	 * @param buffer — a buffer to move
	 * @param x — an x world coordinate
	 * @param y — a y world coordinate
	 */
	public static void moveTo(VertexBufferAccess buffer , float x , float y) {
	
		float midX = midX(buffer);
		float midY = midY(buffer);
		
		translateFloats(buffer , -midX + x , -midY + y);
		
	}
	
	/**
	 * Gets the width of the vertices contained within {@code buffer}.
	 * 
	 * @param buffer — a buffer acces
	 * @return Width of the vertices contained within {@code buffer}.
	 */ 
	public static float width(VertexBufferAccess buffer) {
		
		return buffer.getFloat(asFloatIndex(RX)) - buffer.getFloat(asFloatIndex(LX));
		
	}

	/**
	 * Gets the height of the vertices contained within {@code buffer}.
	 * 
	 * @param buffer — a buffer acces
	 * @return Height of the vertices contained within {@code buffer}.
	 */ 
	public static float height(VertexBufferAccess buffer) {
		
		return buffer.getFloat(asFloatIndex(TY)) - buffer.getFloat(asFloatIndex(BY));
		
	}
	
	private static int asFloatIndex(int index) {
	
		return index << 2;
		
	}			
	
	private VAOUtils() {}

}
