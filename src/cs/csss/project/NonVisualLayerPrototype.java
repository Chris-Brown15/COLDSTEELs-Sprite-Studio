package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

/**
 * Store of information for producing nonvisual layers.
 */
public class NonVisualLayerPrototype {

	public static final int MAX_SIZE_BYTES = 4;
	
	private final int sizeBytes;
	private final String name;
	
	NonVisualLayerPrototype(int sizeBytes , String name) {
		
		specify(sizeBytes > 0 && sizeBytes < 5 , sizeBytes + " is not a valid number of bytes per pixel. Must be between [1 , 4].");
		specify(name , "Must enter a name for a layer.");
		
		this.name = name;
		this.sizeBytes = sizeBytes;
		
	}

	/**
	 * Returns the size in bytes of this nonvisual layer prototype.
	 * 
	 * @return Size in bytes of this prototype.
	 */
	public int sizeBytes() {
		
		return sizeBytes;
		
	}
	
	/**
	 * Returns the name of this nonvisual layer prototype.
	 * 
	 * @return Name of this nonvisual layer prototype.s
	 */
	public String name() {
		
		return name;
		
	}
	
	/**
	 * Displays the UI string for this layer.
	 * 
	 * @return UI friendly string for this record.
	 */
	public String UIString() {
		
		return new StringBuilder().append(name).append(", Size: ").append(sizeBytes).toString();
	
	}
	
}