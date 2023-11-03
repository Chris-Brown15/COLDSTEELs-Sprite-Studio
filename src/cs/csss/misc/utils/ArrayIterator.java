/**
 * 
 */
package cs.csss.misc.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for arrays.
 */
class ArrayIterator<T> implements Iterator<T> {

	private int next = 0;
	private final T[] array;
	
	/**
	 * Creates an array iterator
	 */
	ArrayIterator(T[] array) {
	
		this.array = array;
		
	}

	@Override public boolean hasNext() {

		return array != null && next < array.length;
		
	}

	@Override public T next() {

		if(!hasNext()) throw new NoSuchElementException("This iterator has no next element.");
		return array[next++];
		
	}

}
