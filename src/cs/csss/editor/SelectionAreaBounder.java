package cs.csss.editor;

import cs.coreext.nanovg.NanoVGFrame;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Bounding box. This bounding box is used by selection brushes in order to select regions.
 */
public class SelectionAreaBounder {
	
	private volatile float
		LX = 100 ,
		RX = 200 ,
		BY = 100 ,
		TY = 200;

	public volatile int color = 0xffffffff;
	public volatile float thickness = 1f;
		
	/**
	 * Renders this bounding box in the given {@code NanoVGFrame}.
	 * 
	 * @param frame — a nanovg frame
	 */
	@RenderThreadOnly public void render(NanoVGFrame frame) {
		
		frame.newPath()
			.strokeColor(color)
			.strokeWidth(thickness)
			.moveTo(RX , TY)
			.lineTo(RX, BY)
			.lineTo(LX, BY)
			.lineTo(LX, TY)
			.lineTo(RX, TY)
			.stroke();
		
	}
	
	/**
	 * Moves a corner of this bounding box based on which corner is closest to the coordinates given.  
	 * 
	 * @param cursorX — {@code integer} x coordinate in world space of the cursor
	 * @param cursorY — {@code integer} y coordinate in world space of the cursor
	 */
	public void moveCorner(int cursorX , int cursorY) {
		
		boolean left = false;
		if(Math.abs(LX - cursorX) < Math.abs(RX - cursorX)) left = true;
		
		boolean bottom = false;
		if(Math.abs(BY - cursorY) < Math.abs(TY - cursorY)) bottom = true;
		
		if(left) LX = cursorX;
		else RX = cursorX;
		
		if(bottom) BY = cursorY;
		else TY = cursorY;
		
		//correct the values if they are opposite
		if(LX >= RX) LX = RX - 1;
		if(BY >= TY) BY = TY - 1;

		assert LX < RX : LX + " is an invalid left X. Right X: " + RX;
		assert BY < TY : BY + " is an invalid bottom Y. Top Y: " + TY;
				
	}

	/**
	 * Moves a corner of this bounding box based on which corner is closest to the coordinates given.  
	 * 
	 * @param cursorX — {@code float} x coordinate in world space of the cursor
	 * @param cursorY — {@code float} y coordinate in world space of the cursor
	 */
	public void moveCorner(float cursorX , float cursorY) {
		
		boolean left = false;
		if(Math.abs(LX - cursorX) < Math.abs(RX - cursorX)) left = true;
		
		boolean bottom = false;
		if(Math.abs(BY - cursorY) < Math.abs(TY - cursorY)) bottom = true;
		
		if(left) LX = cursorX;
		else RX = cursorX;
		
		if(bottom) BY = cursorY;
		else TY = cursorY;
		
		//correct the values if they are opposite
		if(LX >= RX) LX = RX - 1;
		if(BY >= TY) BY = TY - 1;

		assert LX < RX : LX + " is an invalid left X. Right X: " + RX;
		assert BY < TY : BY + " is an invalid bottom Y. Top Y: " + TY;
				
	}
	
	/**
	 * Moves this bounder to the given world coordinates.
	 * 
	 * @param moveToX — x coordinate to move to
	 * @param moveToY — y coordinate to move to
	 */
	public void moveTo(float moveToX , float moveToY) {
		
		float halfWidth = (RX - LX) / 2f;
		float halfHeight = (TY - BY) / 2f;
		
		LX = moveToX - halfWidth;
		RX = moveToX + halfWidth;
		
		TY = moveToY + halfHeight;
		BY = moveToY - halfHeight;

		assert LX < RX : LX + " is an invalid left X. Right X: " + RX;
		assert BY < TY : BY + " is an invalid bottom Y. Top Y: " + TY;
			
	}
	
	/**
	 * Forces the corners of this bounding box to be within the given coordinates in world space.
	 * 
	 * @param leftX — left x coordinate in world space
	 * @param rightX — right x coordinate in world space
	 * @param bottomY — bottom x coordinate in world space
	 * @param topY — top x coordinate in world space
	 */
	public void snapBounderToCoordinates(int leftX , int rightX , int bottomY , int topY) {

		if(LX < leftX || LX >= rightX) LX = leftX;
		if(RX > rightX || RX <= leftX) RX = rightX;
		if(BY < bottomY || BY >= topY) BY = bottomY;
		if(TY > topY || TY <= bottomY) TY = topY;

		assert LX < RX : LX + " is an invalid left X. Right X: " + RX;
		assert BY < TY : BY + " is an invalid bottom Y. Top Y: " + TY;
			
	}
	
	/**
	 * Gets and returns the midpoint of this bounder.
	 * 
	 * @return Midpoint of this bounder.
	 */
	public float[] midpoint() {
		
		return new float[] {LX + (width()) / 2 , BY + (height()) / 2};
		
	}
	
	/**
	 * Gets and returns the width of this bounder.
	 * 
	 * @return Width of this bounder.
	 */	
	public int width() {
		
		int width = (int) (RX - LX);
		
		assert width > 0 : width + " is an invalid width.";
		
		return width;
				
	}

	/**
	 * Gets and returns the height of this bounder.
	 * 
	 * @return Height of this bounder.
	 */	
	public int height() {
		
		int height = (int) (TY - BY);

		assert height > 0 : height + " is an invalid height.";
		
		return height;
		
	}

	/**
	 * Returns the left x coordinate of this bounder.
	 * 
	 * @return Left x coordinate of this bounder.
	 */
	public int LX() {

		assert LX < RX : LX + " is an invalid left X. Right X: " + RX;
				
		return (int) LX;
		
	}

	/**
	 * Returns the right x coordinate of this bounder.
	 * 
	 * @return Right x coordinate of this bounder.
	 */
	public int RX() {

		assert LX < RX : LX + " is an invalid left X. Right X: " + RX;
				
		return (int) RX;
		
	}

	/**
	 * Returns the bottom y coordinate of this bounder.
	 * 
	 * @return Bottom y coordinate of this bounder.
	 */
	public int BY() {

		assert BY < TY : BY + " is an invalid bottom Y. Top Y: " + TY;
		
		return (int) BY;
		
	}

	/**
	 * Returns the top y coordinate of this bounder.
	 * 
	 * @return Top y coordinate of this bounder.
	 */
	public int TY() {

		assert BY < TY : BY + " is an invalid bottom Y. Top Y: " + TY;
		
		return (int) TY;
		
	}
	
	/**
	 * Sets the left x coordinate of this bounder.
	 * 
	 * @param lx — new left x coordinate
	 */
	public void LX(float lx) {
		
		assert lx < RX : lx + " is less than RX (" + RX + ").";
		
		this.LX = lx;
		
	}

	/**
	 * Sets the right x coordinate of this bounder.
	 * 
	 * @param rx — new right x coordinate
	 */
	public void RX(float rx) {
		
		assert rx > LX : rx + " is less than LX (" + LX + ").";
		
		this.RX = rx;
		
	}

	/**
	 * Sets the bottom y coordinate of this bounder.
	 * 
	 * @param by — new bottom y coordinate  
	 */
	public void BY(float by) {
		
		assert by < TY : by + " is less than TY (" + TY + ").";
		
		this.BY = by;
		
	}

	/**
	 * Sets the top y coordinate of this bounder.
	 * 
	 * @param by — new top y coordinate  
	 */
	public void TY(float ty) {
		
		assert ty > BY : ty + " is less than BY (" + BY + ").";
		
		this.TY = ty;
		
	}
	
	/**
	 * Sets all four positions of this bounder to the given ones.
	 * 
	 * @param leftX — new left x coordinate 
	 * @param rightX — new right x coordinate 
	 * @param bottomY — new bottom y coordinate
	 * @param topY — new top y coordinate
	 */
	public void positions(int leftX , int rightX , int bottomY , int topY) {
		
		LX = leftX;
		RX = rightX;
		BY = bottomY;
		TY = topY;
		
	}
	
}
