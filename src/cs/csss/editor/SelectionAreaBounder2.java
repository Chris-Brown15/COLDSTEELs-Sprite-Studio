package cs.csss.editor;

import cs.coreext.nanovg.NanoVGFrame;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Differently handled version of {@link SelectionAreaBounder}. This class's {@link #update(float, float) update} and {@link #reset() reset} need to
 * be called to handle this bounder.
 */
public class SelectionAreaBounder2 {

	public static final int CURSOR_DRAG_FRAMES = 3;
	
	private float lx = 0 , rx = 0 , by = 0 , ty = 0;
	
	private float cursorStartX , cursorStartY;
	
	boolean drugLeft , drugDown;
	
	private int frame = 0;
	
	public int color = 0xffffffff , thickness = 3;
		
	/**
	 * Renders this bounding box in the given {@code NanoVGFrame}.
	 * 
	 * @param frame — a nanovg frame
	 */
	@RenderThreadOnly public void render(NanoVGFrame frame) {
		
		frame
			.newPath()
			.strokeColor(color)
			.strokeWidth(thickness)
			.moveTo(rx, ty)
			.lineTo(rx, by)
			.lineTo(lx, by)
			.lineTo(lx, ty)
			.lineTo(rx, ty)
			.stroke()
			.closePath()
			;
		
	}
	
	/**
	 * Should be called as long as the control is pressed. 
	 *  
	 * @param cursorX cursor world x coordinate
	 * @param cursorY cursor world y coordinate
	 */
	public void update(float cursorX , float cursorY) {
		
		//compute positions of corners based on cursor position.
		//drag the corner of the bounder farthest from the inital position.
		
		if(frame == 0) {

			lx = rx = cursorStartX = cursorX;
			by = ty = cursorStartY = cursorY;
		
		} else if (frame == CURSOR_DRAG_FRAMES) {
			
			drugLeft = cursorX < cursorStartX;
			drugDown = cursorY < cursorStartY;
			
		} else if (frame > CURSOR_DRAG_FRAMES) {
			
			if(drugLeft && cursorX < rx) lx = cursorX;		
			else if(cursorX > lx) rx = cursorX;
			
			if(drugDown && cursorY < ty) by = cursorY;
			else if(cursorY > by) ty = cursorY;
		
		}
		
		frame++;
		if(frame == Integer.MAX_VALUE) frame = CURSOR_DRAG_FRAMES + 1;
		
	}

	/**
	 * Resets the state of this bounder so a new bounder can be drawn from it.
	 */
	public void reset() {
		
		frame = 0;
		
	}
	
	/**
	 * Returns whether this bounder has been updated at least once after a call to {@link #reset() reset}. 
	 * 
	 * @return Whether this bounder is running.
	 */
	public boolean running() {
		
		return frame != 0;
		
	}

	/**
	 * Returns the left x coordinate of this bounder.
	 * 
	 * @return Left x coordinate of this bounder.
	 */
	public int lx() {
		
		return (int)lx;
	
	}

	/**
	 * Sets the new value for the left x coordinate of this bounder.
	 * 
	 * @param lx new left x value
	 */
	public void lx(float lx) {
		
		this.lx = lx;
		
	}

	/**
	 * Returns the right x coordinate of this bounder.
	 * 
	 * @return Right x coordinate of this bounder.
	 */
	public int rx() {
		
		return (int)rx;
		
	}

	/**
	 * Sets the new value for the right x coordinate of this bounder.
	 * 
	 * @param rx new right x value
	 */
	public void rx(float rx) {
		
		this.rx = rx;
		
	}

	/**
	 * Returns the bottom y coordinate of this bounder.
	 * 
	 * @return Bottom y coordinate of this bounder.
	 */
	public int by() {
		
		return (int)by;
		
	}

	/**
	 * Sets the new value for the bottom y coordinate of this bounder.
	 * 
	 * @param by new bottom y value
	 */
	public void by(float by) {
		
		this.by = by;
		
	}

	/**
	 * Returns the top y coordinate of this bounder.
	 * 
	 * @return Top y coordinate of this bounder.
	 */
	public int ty() {
		
		return (int)ty;
		
	}

	/**
	 * Sets the new value for the top y coordinate of this bounder.
	 * 
	 * @param ty new top y value
	 */
	public void ty(float ty) {
		
		this.ty = ty;
		
	}
	
	/**
	 * Returns the width of this bounder.
	 * 
	 * @return Width of this bounder.
	 */
	public int width() {
		
		return rx() - lx();
		
	}

	/**
	 * Returns the height of this bounder.
	 * 
	 * @return Height of this bounder.
	 */
	public int height() {
		
		return ty() - by();
		
	}
	
	/**
	 * Returns an array containing the midpoint of this bounder.
	 *  
	 * @return Array containing the midpoint of this bounder.
	 */
	public int[] mid() {
		
		return new int[] {lx() + (width() / 2) , by() + (height() / 2)};
		
	}
	
	/**
	 * Casts each value of this bounder to {@code int}s by calling {@link Math#round(float) round} on each.
	 */
	public void castToInts() {
		
		lx = Math.round(lx);
		rx = Math.round(rx);
		by = Math.round(by);
		ty = Math.round(ty);
		
	}

	/**
	 * Forces the corners of this bounding box to be within the given coordinates in world space.
	 * 
	 * @param leftX — left x coordinate in world space
	 * @param rightX — right x coordinate in world space
	 * @param bottomY — bottom x coordinate in world space
	 * @param topY — top x coordinate in world space
	 */
	public void snapBounderToCoordinates(float leftX , float rightX , float bottomY , float topY) {

		if(lx < leftX || lx >= rightX) lx = leftX;
		if(rx > rightX || rx <= leftX) rx = rightX;
		if(by < bottomY || by >= topY) by = bottomY;
		if(ty > topY || ty <= bottomY) ty = topY;

		assert lx < rx : lx + " is an invalid left X. Right X: " + rx;
		assert by < ty : by + " is an invalid bottom Y. Top Y: " + ty;
			
	}
	
	/**
	 * Sets the values of this bounder.
	 * 
	 * @param leftX new left x value of this bounder
	 * @param rightX new right x value of this bounder
	 * @param bottomY new bottom y value of this bounder
	 * @param topY new top y value of this bounder
	 */
	public void set(float leftX , float rightX , float bottomY , float topY) {
		
		if(leftX > rightX) {
		
			float temp = leftX;
			leftX = rightX;
			rightX = temp;
			
		}
		
		if(bottomY > topY) {
			
			float temp = bottomY;
			bottomY = topY;
			topY = temp;
			
		}
		
		lx = leftX;
		rx = rightX;
		by = bottomY;
		ty = topY;
		
	}

	/**
	 * Returns the number of frame this bounder has had its {@link #update(float, float) update} method invoked between a call to 
	 * {@link #reset() reset}
	 * 
	 * @return Number of updates that have occured since the last call to {@code reset}.
	 */
	public int updates() {
		
		return frame;
		
	}
	
}
