package cs.csss.engine;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Interface for pixels that provide lookup values.
 */
public interface LookupPixel extends Pixel {
	
	/**
	 * Returns the x lookup coordinate of this lookup pixel
	 * 
	 * @return X lookup coordinate.
	 */
	byte lookupX();

	/**
	 * Returns the y lookup coordinate of this lookup pixel
	 * 
	 * @return Y lookup coordinate.
	 */
	byte lookupY();

	/**
	 * Returns the x lookup coordinate of this lookup pixel at a higher bit width so it will appear unsigned.
	 * 
	 * @return Unsigned x lookup coordinate.
	 */
	short unsignedLookupX();
	
	/**
	 * Returns the y lookup coordinate of this lookup pixel at a higher bit width so it will appear unsigned.
	 * 
	 * @return Unsigned y lookup coordinate.
	 */
	short unsignedLookupY();
	
	@Override default int compareTo(Pixel other) {
		
		if(other instanceof LookupPixel asLookup) {

			short thisX = unsignedLookupX();
			short thisY = unsignedLookupY();
			short otherX = asLookup.unsignedLookupX();
			short otherY = asLookup.unsignedLookupY();
			
			if(thisY > otherY) return 1;
			else if (thisY < otherY) return -1;
			else if(thisX > otherX) return 1;
			else if(thisX < otherX) return -1;
			else return 0;
			
		}
		
		return -1;
		
	}
		
	/**
	 * Stores the lookup values of this pixel as bytes in {@code buffer}.
	 * 
	 * @param buffer a buffer to write to
	 * @throws NullPointerException if {@code buffer} is <code>null</code>.
	 */
	default void buffer(ByteBuffer buffer) {
		
		Objects.requireNonNull(buffer);
		buffer.put(lookupX()).put(lookupY());
		
	}
	
}
