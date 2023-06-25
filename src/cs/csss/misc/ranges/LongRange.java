package cs.csss.misc.ranges;

import java.util.Iterator;
import java.util.function.Function;

public class LongRange implements Range, Iterable<Long> {

	private final long bound;
	private final Function<Long , Long> calculator;
	
	public LongRange(long bound) {
		
		this(bound , Long::longValue);
		
	}
	
	public LongRange(long bound , Function<Long , Long> calculator) {

		this.bound = bound;
		this.calculator = calculator;
		
	}

	@Override public Object toArray() {

		long[] longs = new long[(int)bound];
		for(long i = 0 ; i < longs.length ; i++) longs[(int)i] = calculator.apply(i);
		return longs;
		
	}

	@Override public Iterator<Long> iterator() {

		return new Iterator<>() {

			private final ThreadLocal<Long> next = ThreadLocal.withInitial(() -> 0L);
			
			@Override public boolean hasNext() {

				return next.get() < bound;
				
			}

			@Override public Long next() {

				long res = calculator.apply(next.get());
				next.set(next.get() + 1);				
				return res;
				
			}
			
		};
		
	}

}
