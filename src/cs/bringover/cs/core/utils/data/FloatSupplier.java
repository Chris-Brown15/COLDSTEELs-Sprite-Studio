package cs.bringover.cs.core.utils.data;

/**
 * 
 * @see {@linkplain java.util.function.IntSupplier}.
 *
 */
@FunctionalInterface
public interface FloatSupplier {

	/**
	 * Returns a {@code float} value.
	 * 
	 * @return A {@code float}.
	 */
	public float getAsFloat();
	
}
