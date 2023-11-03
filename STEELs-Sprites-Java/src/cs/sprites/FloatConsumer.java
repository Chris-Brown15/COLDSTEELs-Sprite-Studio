/**
 * Licenced under MIT.
 */
package cs.sprites;

import java.util.Objects;

/**
 * Single Abstract Method modeling {@link java.util.function.IntConsumer IntConsumer}.
 */
@FunctionalInterface
public interface FloatConsumer {

    /**
     * Returns a composed {@code FloatConsumer} that performs, in sequence, this operation followed by the {@code after} operation. If 
     * performing either operation throws an exception, it is relayed to the caller of the composed operation.  If performing this operation 
     * throws an exception, the {@code after} operation will not be performed.
     *
     * @param after — the operation to perform after this operation
     * @return Composed {@code FloatConsumer} that performs in sequence this operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null.
     */
    default FloatConsumer andThen(FloatConsumer after) {
    	
        Objects.requireNonNull(after);
        
        return t -> { 
        
        	accept(t); 
        	after.accept(t);
        	
        };
        
    }
    
    /**
     * Recieve a {@code float} value and operate with it. 
     * 
     * @param value — a float value
     */
	public void accept(float value);
	
}
