package cs.csss.project;

import static cs.core.utils.CSUtils.specify;

/**
 * Store of information for producing layers.
 */
public class NonVisualLayerPrototype {

	public static final int MAX_SIZE_BYTES = 4;
	
	private final int sizeBytes;
	private final String name;
	
	public static boolean isValidName(final String potentialName) {
		
		return Layer.isValidName(potentialName);
				
	}
	
	NonVisualLayerPrototype(int sizeBytes , String name) {
		
		specify(sizeBytes > 0 && sizeBytes < 5 , sizeBytes + " is not a valid number of bytes per pixel. Must be between [1 , 4].");
		specify(name , "Must enter a name for a layer.");
		
		this.name = name;
		this.sizeBytes = sizeBytes;
		
	}

	public int sizeBytes() {
		
		return sizeBytes;
		
	}
	
	public String name() {
		
		return name;
		
	}
	
	@Override public String toString() {
		
		StringBuilder builder = new StringBuilder();
		builder
			.append("NonVisual Layer Prototype: ")
			.append(name)
			.append(", Size: ")
			.append(sizeBytes)
			.append(", Default Values: ")
		;
		
		return builder.toString();
	
	}
	
}