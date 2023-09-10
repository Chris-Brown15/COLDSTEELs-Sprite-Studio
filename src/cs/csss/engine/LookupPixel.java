package cs.csss.engine;

/**
 * Interface for pixels that provide lookup values.
 */
public interface LookupPixel {
	
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
	
}
