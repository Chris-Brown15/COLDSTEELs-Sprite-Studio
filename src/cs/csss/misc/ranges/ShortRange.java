package cs.csss.misc.ranges;

import java.util.Iterator;
import java.util.function.Function;

public class ShortRange implements Range, Iterable<Short> {

	private final int bound;
	private final Function<Integer , Short> calculator;
	
	public ShortRange(final int bound) {
		
		this(bound , (input) -> input.shortValue());
		
	}
	
	public ShortRange(final int bound , final Function<Integer , Short> calculator) {

		this.bound = bound;
		this.calculator = calculator;
		
	}

	@Override public Object toArray() {

		short[] array = new short[bound];
		for(int i = 0 ; i < array.length ; i++) array[i] = calculator.apply(i);
		return array;
		
	}

	@Override public Iterator<Short> iterator() {

		return new Iterator<>() {

			private final ThreadLocal<Integer> next = ThreadLocal.withInitial(() -> 0);
			
			@Override public boolean hasNext() {

				return next.get() != bound;
				
			}

			@Override public Short next() {

				short res = calculator.apply(next.get());
				next.set(next.get() + 1);				
				return res;
				
			}			
			
		};
		
	}

}