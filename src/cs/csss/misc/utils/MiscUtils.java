/**
 * 
 */
package cs.csss.misc.utils;

import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Container for miscellaneous utilities.
 */
public final class MiscUtils {

	/**
	 * Returns the number of objects from the given vararg that are nonnull.
	 * 
	 * @param objects — a list of objects
	 * @return Number of those objects that are not null.
	 */
	public static int numberNonNull(Object...objects) {
		
		int numNonNull = 0;
		for(Object x : objects) if(!isNull(x)) numNonNull++;
		return numNonNull;
		
	}

	/**
	 * Returns the number of objects from the given vararg that are null.
	 * 
	 * @param objects — a list of objects
	 * @return Number of those objects that are null.
	 */
	public static int numberNull(Object...objects) {
		
		int numNull = 0;
		for(Object x : objects) if(isNull(x)) numNull++;
		return numNull;
		
	}
	
	/* Arrays */
	
	/**
	 * Stores the nonnull elements of {@code objects} into {@code destination} starting at {@code offset} and only writing a maximum of {@code max}
	 * elements.
	 * 
	 * @param destination — destination array
	 * @param offset — index to begin writing to in {@code destination}
	 * @param max — maximum number of elements to write to {@code destination}
	 * @param objects — vararg of elmeents to check and write
	 */
	public static void fromNonNull(Object[] destination , int offset , int max , Object...objects) {
		
		Objects.requireNonNull(destination);
		Objects.checkFromIndexSize(offset, max, destination.length);
		Objects.requireNonNull(objects);
		
		if(objects.length == 0) return;
		
		for(int destIndex = offset , objectsIndex = 0 ; (destIndex - offset) < max && objectsIndex < objects.length ; objectsIndex++) { 
			
			Object x = objects[objectsIndex];
			if(!isNull(x)) destination[destIndex++] = x;
			
		}		
		
	}
		
	/**
	 * Returns an array of object created from the nonnull elements of {@code toArray}.
	 * 
	 * @param array — vararg of objects
	 * @return Array containing the nonnull elements of {@code array}.
	 */
	public static Object[] fromNonNull(Object...array) {
		
		Object[] nonNull = new Object[numberNonNull(array)];
		fromNonNull(nonNull , 0 , nonNull.length, array);
		return nonNull;
		
	}
		
	/**
	 * Returns an array of {@code T} created from the nonnull elements of {@code toArray}.
	 * 
	 * @param <T> — type of element stored
	 * @param creator — function for creating a destination array
	 * @param array — vararg of objects
	 * @return Array containing the nonnull elements of {@code array}.
	 */
	@SafeVarargs public static <T> T[] fromNonNull(IntFunction<T[]> creator , T... array) {
		
		T[] nonNull = creator.apply(numberNonNull(array));
		fromNonNull(nonNull , 0 , nonNull.length , array);
		return nonNull;
		
	}
	
	/**
	 * Private Constructor.
	 */
	private MiscUtils() {}

}
