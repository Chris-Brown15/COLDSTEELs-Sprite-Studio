package cs.csss.misc.ranges;

import java.util.Iterator;
import java.util.function.Function;

public class IntRange implements Range , Iterable<Integer> {

	private final int bound;
	private final Function<Integer , Integer> calculator;
	
	public IntRange(int bound) {

		this(bound , Integer::intValue);
		
	}

	public IntRange(int bound , Function<Integer , Integer> calculator) {

		this.bound = bound;
		this.calculator = calculator;
		
	}

	public int at(int index) {
		
		return calculator.apply(index);
		
	}

	@Override public Object toArray() {

		int[] array = new int[bound];
		for(int i = 0 ; i < bound ; i++) array[(int) i] = calculator.apply(i);
		return array;
		
	}

	@Override public Iterator<Integer> iterator() {

		return new Iterator<>() {

			private ThreadLocal<Integer> next = ThreadLocal.withInitial(() -> 0);
			
			@Override public boolean hasNext() {

				return next.get() != bound;
			}

			@Override public Integer next() {

				int res = calculator.apply(next.get());
				next.set(next.get() + 1);
				return res;
				
			}
			
		};
		
	}

}