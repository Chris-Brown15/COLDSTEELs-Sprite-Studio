package cs.sprites;

import java.util.Objects;

/**
 * Single Abstract Method modeling {@link java.util.function.IntConsumer IntConsumer}.
 */
@FunctionalInterface
public interface FloatBiConsumer {

    /**
     * Returns a composed {@code BiFloatConsumer} that performs, in sequence, this operation followed by the {@code after} operation. If 
     * performing either operation throws an exception, it is relayed to the caller of the composed operation.  If performing this operation 
     * throws an exception, the {@code after} operation will not be performed.
     *
     * @param after — the operation to perform after this operation
     * @return Composed {@code BiFloatConsumer} that performs in sequence this operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null.
     */
    default FloatBiConsumer andThen(FloatBiConsumer after) {
    	
        Objects.requireNonNull(after);
        
        return (f1 , f2) -> { 
        
        	accept(f1 , f2); 
        	after.accept(f1 , f2);
        	
        };
        
    }

    /**
     * Recieve a {@code float} value and operate with it. 
     * 
     * @param value — a float value
     */
	public void accept(float f1 , float f2);
	
}
