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
	
	protected int[] values = new int[3];
	
	CSSSModifyingBrush(String tooltip) {
		
		super(tooltip);
	
	}
	
	/**
	 * Modifies {@code xIndex} and {@code yIndex} such that they locate the bottom left corner of a region. The third value of the array
	 * is a square size value.
	 * 
	 * @param xIndex — x index of a clicked pixel
 	 * @param yIndex — y index of a clicked pixel
	 * @return Array containing three values, {@code xIndex}, {@code yIndex}, and a size. The indices point to the lower left pixel of a 
	 * 		   region, and size is the number of pixels to extend this region.
	 */
	protected int[] centerAroundRadius(int xIndex , int yIndex) {
		
		xIndex -= radius;
		yIndex -= radius;
		int size = (radius * 2) + 1; 

		if(xIndex < 0) xIndex = 0;
		if(yIndex < 0) yIndex = 0;
		
		values[0] = xIndex;
		values[1] = yIndex;
		values[2] = size;
		
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
