package cs.csss.editor.shape;

import java.nio.ByteBuffer;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.misc.utils.FlexableGraphic;
import cs.csss.project.Artboard;

/**
 * Ellipse shape type. 
 */
public class Ellipse extends Shape {

	/**
	 * Default number of iterations an ellipse will take to create itself.
	 */
	public static final float DEFAULT_ITERATIONS = 90f;
	
	private int xRadius , yRadius;
	
	private float iterations = DEFAULT_ITERATIONS;
	
	/**
	 * Creates an ellipse from the given parameters.
	 * 
	 * @param x x midpoint for the ellipse
	 * @param y y midpoint for the ellipse
	 * @param radiusX horizontal radius for the ellipse
	 * @param radiusY vertical radius for the ellipse
	 * @param initialColor color for the ellipse's border and fill
	 * @param channelsPerPixel number of channels per pixel the owning project has
	 * @param fill whether to initially fill the ellipse
	 * @param formatColors whether to reformat {@code borderColor} and {@code fillColor} according to the semantics of 
	 * 					   {@link Shape#formatColor(ColorPixel, ChannelBuffer, int)}
	 */
	@RenderThreadOnly public Ellipse(
		float x, 
		float y , 
		int radiusX , 
		int radiusY , 
		ColorPixel initialColor , 
		int channelsPerPixel , 
		boolean fill, 
		boolean formatColors
	) {

		this(x , y , radiusX , radiusY , initialColor , initialColor , channelsPerPixel , fill, formatColors);
		
	}
	
	/**
	 * Creates an ellipse from the given parameters with the fill color set independently of the border color.
	 * 
	 * @param x x midpoint for the ellipse
	 * @param y y midpoint for the ellipse
	 * @param radiusX horizontal radius for the ellipse
	 * @param radiusY vertical radius for the ellipse
	 * @param borderColor color for the ellipse's border 
	 * @param fillColor color for the ellipse's fill
	 * @param channelsPerPixel number of channels per pixel the owning project has
	 * @param fill whether to initially fill the ellipse
	 * @param formatColors whether to reformat {@code borderColor} and {@code fillColor} according to the semantics of 
	 * 					   {@link Shape#formatColor(ColorPixel, ChannelBuffer, int)}
	 */
	@RenderThreadOnly public Ellipse(
		float x , 
		float y , 
		int radiusX , 
		int radiusY , 
		ColorPixel borderColor , 
		ColorPixel fillColor , 
		int channelsPerPixel , 
		boolean fill, 
		boolean formatColors
	) {
		
		super(borderColor , fillColor , channelsPerPixel, formatColors);

		this.xRadius = radiusX;
		this.yRadius = radiusY;
			
		this.fill = fill;

		int xDiameter = 2 * xRadius;
		int yDiameter = 2 * yRadius;
		
		initializeRendererData((int)x, (int)y, xDiameter << 1 , yDiameter << 1);
		
		reset();
		
	}

	@RenderThreadOnly @Override public void reset() {

		int xDiameter = 2 * xRadius;
		int yDiameter = 2 * yRadius;
		
		int imageWidth = xDiameter << 1;
		int imageHeight = yDiameter << 1;
		
		byte z = (byte)0;
		FlexableGraphic graphic = new FlexableGraphic(imageWidth , imageHeight , channelsPerPixel , new ChannelBuffer(z , z , z  , z));
		
		ByteBuffer image = graphic.imageData();
		
		int midpoint = (yDiameter * imageWidth * channelsPerPixel) + (xDiameter * channelsPerPixel);

		/*
		 * Write the ellipse to the image
		 * 
		 * We use formula for getting position of a point on the elipse given angle and radii. We draw the outine of the shape and then fill in the 
		 * inside rows. We only iterate over one quadrant and reflect our drawing horizontally and vertically.
		 * 
		 */
		
		//iterate and fill in ellipse
		//increase and decrease the addend to get more pixels
		float addend = DEFAULT_ITERATIONS / iterations;
		for(float i = 0f ; i < 90.0f ; i += addend) {
			
			double radians = Math.toRadians(i);
			int xCoord = (int) (xRadius * Math.cos(radians));
			int yCoord = (int) (yRadius * Math.sin(radians));

			int yOffset = yCoord * imageWidth * channelsPerPixel;
			int xOffset = xCoord * channelsPerPixel;
			
			int rightOffset = midpoint + yOffset + xOffset;
			int leftOffset = midpoint + yOffset - xOffset;
			
			putColor(image , rightOffset , borderColor); 
			putColor(image , leftOffset , borderColor);
			
			if(fill) {
				
				//fill in row
				leftOffset += channelsPerPixel;
				while(leftOffset < rightOffset) {
					
					putColor(image , leftOffset , fillColor);
					leftOffset += channelsPerPixel;
					
				}
			
			}
			
			rightOffset = midpoint - yOffset + xOffset;
			leftOffset = midpoint - yOffset - xOffset;

			putColor(image , rightOffset , borderColor); 
			putColor(image , leftOffset , borderColor);
			
			if(fill) {
				
				//fill in lower row
				leftOffset += channelsPerPixel;
				while(leftOffset < rightOffset) {

					putColor(image , leftOffset , fillColor);
					leftOffset += channelsPerPixel;
					
				}
			
			}
						
		}
		
		defaultReset(graphic, imageWidth, imageHeight);
				
	}

 	@Override public void rasterize(Artboard target) {

 		defaultRasterize(target, textureWidth, textureHeight);
 		 		
 	}

	@SuppressWarnings("unchecked") @Override public Ellipse copy() {

		Ellipse newEllipse = new Ellipse(midX() , midY() , xRadius , yRadius , borderColor , channelsPerPixel , fill, false);
		newEllipse.fillColor(fillColor);
		newEllipse.borderColor(borderColor);
		newEllipse.iterations = this.iterations;
		return newEllipse;
		
	}
	
	/**
	 * Returns the current x radius.
	 * 
	 * @return Current x radius.
	 */
	public int xRadius() {
		
		return xRadius; 
	
	}
	
	/**
	 * Sets the new x radius for this ellipse, but does not {@link #reset()} it.
	 * 
	 * @param newXRadius new x radius for this ellipse
	 * @return {@code this}.
	 */
	public Ellipse xRadius(int newXRadius) {
		
		assert newXRadius > 0 : "New X Radius is not positive";
		
		this.xRadius = newXRadius;
		return this;
		
	}
	
	/**
	 * Sets the new y radius for this ellipse, but does not {@link #reset()} it.
	 * 
	 * @param newYRadius new y radius for this ellipse
	 * @return {@code this}.
	 */
	public Ellipse yRadius(int newYRadius) {
		
		assert newYRadius > 0 : "New X Radius is not positive";
		
		this.yRadius = newYRadius;
		return this;
		
	}

	/**
	 * Returns the current y radius.
	 * 
	 * @return Current y radius.
	 */
	public int yRadius() {
		
		return yRadius; 
	
	}
	
	/**
	 * Sets the number of iterations. This value can be used to fill holes in large ellipses.
	 * 
	 * @param iterations new number of iterations; defaults to 90.
	 */
	public void iterations(float iterations) {
		
		if(iterations <= 0) iterations = DEFAULT_ITERATIONS;
		this.iterations = iterations;
		
	}

	/**
	 * Returns the number of iterations this ellipse does when it's being created.
	 * 
	 * @return Number of iterations for this ellipse.
	 */
	public float iterations() {
		
		return iterations;
		
	}
		
	private void putColor(ByteBuffer destination , int offset , ColorPixel color) { 
	
		for(int i = 0 ; i < channelsPerPixel ; i++) destination.put(offset + i , color.i(i));
		
	}

	@Override public void dimensions(int width, int height) {

		shapeWidth(width);
		shapeHeight(height);
		
	}

	@Override public Shape shapeWidth(int width) {

		assert width > 0;
		xRadius = Math.max(width >> 1, 1);
		return this;
		
	}

	@Override public Shape shapeHeight(int height) {

		assert height > 0;
		yRadius = Math.max(height >> 1, 1);
		return this;
		
	}
	
	@Override public int shapeWidth() {
		
		return xRadius << 1;
		
	}
	
	@Override public int shapeHeight() {
		
		return yRadius << 1;
		
	}
	
	@Override public String toString() {
		
		return String.format("Ellipse at %f, %f)", midX() , midY());
				
	}

}