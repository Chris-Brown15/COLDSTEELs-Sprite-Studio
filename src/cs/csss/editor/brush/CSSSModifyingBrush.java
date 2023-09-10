package cs.csss.editor.brush;

/**
 * 
 * Base class for all brushes that have a radius of impact. This class is intended to unify all brushes that have some modifying behavior on
 * the artboard specifically. For example, {@code EyeDropperBrush} is not a modifying brush because it does not modify the artboard, whereas
 * {@code PencilBrush} is.
 * 
 * @author Chris Brown
 *
 */
public abstract class CSSSModifyingBrush extends CSSSBrush {

	//represents the radius of the brush
	protected int radius = 0;
	
	protected int[] values = new int[4];
	
	CSSSModifyingBrush(String tooltip , boolean stateful) {
		
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
	protected int[] centerAroundRadius(int xIndex , int yIndex , int artboardWidth , int artboardHeight) {
		
		xIndex -= radius;
		yIndex -= radius;
		int width = (radius * 2) + 1;
		int height = (radius * 2) + 1;

		//the indices are negative so we add them
		if(xIndex < 0) { 

			width += xIndex ; xIndex = 0;
			
		}
		
		if(yIndex < 0) { 
			
			height += yIndex ; yIndex = 0;
			
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
