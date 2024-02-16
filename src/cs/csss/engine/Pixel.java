/**
 * 
 */
package cs.csss.engine;

/**
 * Root interface for all pixels. Extending interfaces of this interface, {@link LookupPixel} and {@link ColorPixel}, provide more useful information
 * about their semantics.
 */
public interface Pixel extends Comparable<Pixel> , Cloneable {

	public Pixel clone();

}
