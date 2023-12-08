package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;

/**
 * 
 * Base class for all brushes that have a radius of impact. This class is intended to unify all brushes that have some modifying behavior on
 * the artboard specifically. For example, {@link cs.csss.editor.brush.EyeDropperBrush EyeDropperBrush} is not a modifying brush because it 
 * does not modify the artboard, whereas {@link cs.csss.editor.brush.PencilBrush PencilBrush} is.
 * 
 * @author Chris Brown
 *
 */
@RenderThreadOnly public abstract class CSSSModifyingBrush extends CSSSBrush {

	//Fields of this class are public for the benefit of Jython. In Jython, Python classes that extend Java classes cannot access protected fields
	//seemingly, only public ones.
	
	//represents the radius of the brush
	public int radius = 0;
	
	public int[] values = new int[4];
	
	/**
	 * Creates a new modifying brush.
	 * 
	 * @param tooltip — tooltip for this brush
	 * @param stateful — whether this brush is stateful
	 */
	public CSSSModifyingBrush(String tooltip , boolean stateful) {
		
		super(tooltip , stateful);
	
	}
	
	/**
	 * Modifies {@code xIndex} and {@code yIndex} such that they locate the bottom left corner of a region. The third value of the array
	 * is a square size value.
	 * 
	 * @param xIndex — x index of a clicked pixel
 	 * @param yIndex — y index of a clicked pixel
 	 * @param artboardWidth — width of the artboard
 	 * @param artboardHeight — height of the artboard
	 * @return Array containing four values, {@code xIndex}, {@code yIndex}, width, and height. The indices point to the lower left pixel of
	 *  	   a region, and width and height are the number of pixels to extend this region.
	 */
	public int[] centerAroundRadius(int xIndex , int yIndex , int artboardWidth , int artboardHeight) {
		
		xIndex -= radius;
		yIndex -= radius;
		int width = (radius * 2) + 1;
		int height = (radius * 2) + 1;

		//the indices are negative so we add them
		if(xIndex < 0) { 

			width += xIndex; 
			xIndex = 0;
			
		}
		
		if(yIndex < 0) { 
			
			height += yIndex; 
			yIndex = 0;
			
		}
		
		if(xIndex + width > artboardWidth) width = artboardWidth - xIndex;
		if(yIndex + height > artboardHeight) height = artboardHeight - yIndex;
		
		values[0] = xIndex;
		values[1] = yIndex;
		values[2] = width;
		values[3] = height;
		
		return values;
		
	}
	
	/**
	 * Gets the radius of this brush.
	 * 
	 * @return Radius of this brush.
	 */
	public int radius() {
		
		return radius;
		
	}
	
	/**
	 * Sets the radius of this brush.
	 * 
	 * @param radius — new radius of this brush
	 */
	public void radius(int radius) {
		
		this.radius = radius;
		
	}

}
