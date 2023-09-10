package cs.csss.editor;

import static cs.core.utils.CSUtils.specify;

import cs.coreext.nanovg.NanoVGFrame;

/**
 * Representation of a bounding box. This bounding box is used by selection brushes in order to select regions.
 */
public class SelectionAreaBounder {
	
	private volatile float
		LX = 100 ,
		RX = 200 ,
		BY = 100 ,
		TY = 200;

	public volatile int color = 0xffffffff;
	public volatile float thickness = 1f;
		
	public void render(NanoVGFrame frame) {
		
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

		specify(LX < RX , LX + " is an invalid left X. Right X: " + RX);
		specify(BY < TY , BY + " is an invalid bottom Y. Top Y: " + TY);
				
	}

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

		specify(LX < RX , LX + " is an invalid left X. Right X: " + RX);
		specify(BY < TY , BY + " is an invalid bottom Y. Top Y: " + TY);
				
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

		specify(LX < RX , LX + " is an invalid left X. Right X: " + RX);
		specify(BY < TY , BY + " is an invalid bottom Y. Top Y: " + TY);
				
	}
	
	public void snapBounderToCoordinates(int leftX , int rightX , int bottomY , int topY) {

		if(LX < leftX || LX >= rightX) LX = leftX;
		if(RX > rightX || RX <= leftX) RX = rightX;
		if(BY < bottomY || BY >= topY) BY = bottomY;
		if(TY > topY || TY <= bottomY) TY = topY;

		specify(LX < RX , LX + " is an invalid left X. Right X: " + RX);
		specify(BY < TY , BY + " is an invalid bottom Y. Top Y: " + TY);
				
	}
	
	public float[] midpoint() {
		
		return new float[] {LX + (width()) / 2 , BY + (height()) / 2};
		
	}
	
	public int width() {
		
		int width = (int) (RX - LX);
		
		specify(width > 0 , width + " is an invalid width.");
		
		return width;
				
	}
	
	public int height() {
		
		int height = (int) (TY - BY);

		specify(height > 0 , height + " is an invalid height.");
		
		return height;
		
	}

	public int LX() {

		specify(LX < RX , LX + " is an invalid left X. Right X: " + RX);
				
		return (int) LX;
		
	}

	public int RX() {

		specify(LX < RX , LX + " is an invalid left X. Right X: " + RX);
				
		return (int) RX;
		
	}

	public int BY() {

		specify(BY < TY , BY + " is an invalid bottom Y. Top Y: " + TY);
		
		return (int) BY;
		
	}

	public int TY() {

		specify(BY < TY , BY + " is an invalid bottom Y. Top Y: " + TY);
		
		return (int) TY;
		
	}
	
	public void LX(float lx) {
		
		specify(lx < RX , lx + " is less than RX (" + RX + ").");
		
		this.LX = lx;
		
	}

	public void RX(float rx) {
		
		specify(rx > LX , rx + " is less than LX (" + LX + ").");
		
		this.RX = rx;
		
	}

	public void BY(float by) {
		
		specify(by < TY , by + " is less than TY (" + TY + ").");
		
		this.BY = by;
		
	}

	public void TY(float ty) {
		
		specify(ty > BY , ty + " is less than BY (" + BY + ").");
		
		this.TY = ty;
		
	}
	
	public void positions(int leftX , int rightX , int bottomY , int topY) {
		
		LX = leftX;
		RX = rightX;
		BY = bottomY;
		TY = topY;
		
	}
	
}
