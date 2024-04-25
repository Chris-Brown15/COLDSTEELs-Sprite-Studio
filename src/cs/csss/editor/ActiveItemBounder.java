package cs.csss.editor;

import static cs.csss.utils.CollisionUtils.between;

import cs.coreext.nanovg.NanoVGFrame;

/**
 * NanoVG Bounder drawn on the active shape in the editor. This bounder surrounds the active shape, and has clickable boxes for dragging the active 
 * shape's borders and moving it.
 */
public class ActiveItemBounder {

	private float lx , by , rx , ty;
	
	/**
	 * Color of the bounder.
	 */
	public int mainBorderColor = 0xffffffff;
	
	/**
	 * Thickness of the bounder.
	 */
	public float mainBorderThickness = 3f;
	
	/**
	 * Color of the boxes used to resize the bounder horizontally.
	 */
	public int sideMoverColor = 0xff0000ff;
	
	/**
	 * Thickness of the boxes used to resize the bounder horizontally.
	 */
	public float sideMoverThickness = mainBorderThickness / 1;

	/**
	 * Color of the boxes used to resize the bounder vertically.
	 */
	public int cornerMoverColor = 0xffff00ff;

	/**
	 * Thickness of the boxes used to resize the bounder vertically.
	 */
	public float cornerMoverThickness = mainBorderThickness / 1;
	
	public boolean showLeftSideMover = true , showRightSideMover = true , showTopSideMover = true , showBottomSideMover = true , showMiddleMover = true;
	
	private int leftSideMoverColor = sideMoverColor;
	private float leftSideMoverThickness = sideMoverThickness;
	
	private int rightSideMoverColor = sideMoverColor;
	private float rightSideMoverThickness = sideMoverThickness;
	
	private int topSideMoverColor = sideMoverColor;
	private float topSideMoverThickness = sideMoverThickness;

	private int bottomSideMoverColor = sideMoverColor;
	private float bottomSideMoverThickness = sideMoverThickness;	
	
	/**
	 * Renders this active shape bounder with the given NanoVG frame.
	 * 
	 * @param frame the frame to render with
	 */
	public void render(NanoVGFrame frame) {
		
		frame
			.newPath()
			.strokeColor(mainBorderColor)
			.strokeWidth(mainBorderThickness);		
		
		box(frame , lx , by , rx , ty)
			.stroke()
			.closePath();

		if(showLeftSideMover) renderHorizontalSideMover(frame, true);
		if(showRightSideMover) renderHorizontalSideMover(frame, false);		
		if(showBottomSideMover) renderVerticalSideMover(frame , true);
		if(showTopSideMover) renderVerticalSideMover(frame , false);

		if(showMiddleMover) renderBox(frame , xMid() , yMid() , mainBorderColor , mainBorderThickness);
		
	}

	private void renderHorizontalSideMover(NanoVGFrame frame , boolean left) {
		
		int x = Math.round(left ? lx - 1 : rx);
		int y = yMid();
		
		renderBox(frame , x , y , left ? leftSideMoverColor : rightSideMoverColor , left ? leftSideMoverThickness : rightSideMoverThickness);
		
	}

	private void renderVerticalSideMover(NanoVGFrame frame , boolean bottom) {
		
		int x = xMid();
		int y = Math.round(bottom ? by - 1 : ty);

		renderBox(frame , x , y , bottom ? bottomSideMoverColor : topSideMoverColor , bottom ? bottomSideMoverThickness : topSideMoverThickness);
				
	}

	private void renderBox(NanoVGFrame frame , float lx , float by , int color , float thickness) {

		frame
			.newPath()
			.strokeColor(color)
			.strokeWidth(thickness);
		
		box(frame , lx , by , lx + 1f , by + 1f)
			.stroke()
			.closePath();
			
	}
	
	private NanoVGFrame box(NanoVGFrame frame , float lx , float by , float rx , float ty) {
		
		return frame.moveTo(lx , by).lineTo(lx , ty).lineTo(rx , ty).lineTo(rx , by).lineTo(lx , by);
		
	}
	
	/**
	 * Sets the position and bounds of the bounder.
	 * 
	 * @param lx left x coordinate of the bounder in world space.
	 * @param by bottom y coordinate of the bounder in world space
	 * @param width width of the bounder in world space
	 * @param height height of the bounder in world space
	 */
	public void set(float lx , float by , int width , int height) {
		
		this.lx = lx;
		this.by = by;
		this.rx = lx + width;
		this.ty = by + height;
				
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
	 * Returns whether the point given by {@code x , y} is within the left side mover widget.
	 * 
	 * @param x an x coordinate of a point in world space
	 * @param y a y coordinate of a point in world space
	 * @return Whether a point is in the left side mover.
	 */
	public boolean inLeftSideMover(float x , float y) {

		return between(x , lx - 1 , lx) && between(y , yMid() , yMid() + 1);
		
	}

	/**
	 * Returns whether the point given by {@code x , y} is within the right side mover widget.
	 * 
	 * @param x an x coordinate of a point in world space
	 * @param y a y coordinate of a point in world space
	 * @return Whether a point is in the right side mover.
	 */
	public boolean inRightSideMover(float x , float y) {
	
		return between(x , rx , rx + 1) && between(y , yMid() , yMid() + 1);
		
	}

	/**
	 * Returns whether the point given by {@code x , y} is within the bottom side mover widget.
	 * 
	 * @param x an x coordinate of a point in world space
	 * @param y a y coordinate of a point in world space
	 * @return Whether a point is in the bottom side mover.
	 */
	public boolean inBottomSideMover(float x , float y) {
		
		return between(x , xMid() , xMid() + 1) && between(y , by - 1 , by);
		
	}
	
	/**
	 * Returns whether the point given by {@code x , y} is within the top side mover widget.
	 * 
	 * @param x an x coordinate of a point in world space
	 * @param y a y coordinate of a point in world space
	 * @return Whether a point is in the top side mover.
	 */
	public boolean inTopSideMover(float x , float y) {
		
		return between(x , xMid() , xMid() + 1) && between(y , ty , ty  + 1);
		
	}

	/**
	 * Returns whether the point given by {@code x , y} is within the middle mover widget.
	 * 
	 * @param x an x coordinate of a point in world space
	 * @param y a y coordinate of a point in world space
	 * @return Whether a point is in the middle mover.
	 */
	public boolean inMiddleMover(float x , float y) {
		
		return between(x , xMid() , xMid() + 1) && between(y , yMid() , yMid() + 1);
		
	}
	
	public float lx() {
		
		return lx;
		
	}

	public float by() {
		
		return by;
		
	}
	
	public float rx() { 
		
		return rx;
		
	}
	
	public float ty() {
		
		return ty;
		
	}
	
	private int yMid() {
		
		return (int) Math.floor(by +  ((ty - by) / 2));
		
	}

	private int xMid() {
		
		return (int) Math.floor(lx + ((rx - lx) / 2));
		
	}
	
}
