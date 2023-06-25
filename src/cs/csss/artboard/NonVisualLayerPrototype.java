package cs.csss.artboard;

import static cs.core.utils.CSUtils.specify;

/**
 * Store of information for producing layers.
 */
public record NonVisualLayerPrototype(int sizeBytes , String name){

	public static final int MAX_SIZE_BYTES = 4;
	
	public static boolean isValidName(final String potentialName) {
		
		return Layer.isValidName(potentialName);
				
	}
	
	public NonVisualLayerPrototype(int sizeBytes , String name) {
		
		specify(sizeBytes > 0 && sizeBytes < 5 , sizeBytes + " is not a valid number of bytes per pixel. Must be between [1 , 4].");
		specify(name , "Must enter a name for a layer.");
		
		this.name = name;
		this.sizeBytes = sizeBytes;
		
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