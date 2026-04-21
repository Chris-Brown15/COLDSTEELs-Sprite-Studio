package cs.bringover.cs.core.utils.data;

/**
 * 
 * @see {@linkplain java.util.function.IntConsumer}.
 *
 */
@FunctionalInterface
public interface FloatConsumer {

	/**
	 * Recieves a {@code float} value.
	 * 
	 * @param acceptThis {@code float} value to accept
	 */
	public void accept(float acceptThis);
	
}
