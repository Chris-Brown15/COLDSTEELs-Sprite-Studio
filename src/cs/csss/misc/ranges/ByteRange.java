package cs.csss.misc.ranges;

import java.util.Iterator;
import java.util.function.Function;

public class ByteRange implements Range , Iterable<Byte> {

	private final int bound;

	private final Function<Integer , Byte> calculator;
	
	public ByteRange(final int bound) {

		this(bound , Integer::byteValue);
		
	}
	
	public ByteRange(final int bound , final Function<Integer , Byte> calculator) {

		this.bound = bound;
		this.calculator = calculator;
		
	}

	@Override public Iterator<Byte> iterator() {

		return new Iterator<>() {

			private final ThreadLocal<Integer> next = ThreadLocal.withInitial(() -> 0);
			
			@Override public boolean hasNext() {

				return next.get() != bound;
				
			}

			@Override public Byte next() {

				byte res = calculator.apply(next.get());
				next.set((next.get() + 1));
				return res;
				
			}
			
		};
		
	}

	@Override public Object toArray() {

		byte[] array = new byte[bound];
		for(int i = 0 ; i < bound ; i++) array[i] = calculator.apply(i);
		return array;
		
	}

}
