/**
 * 
 */
package cs.csss.misc.utils;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Container for miscellaneous utilities.
 */
public final class MiscUtils {
	
	/**
	 * Returns the number of objects from the given vararg that are nonnull.
	 * 
	 * @param objects a list of objects
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
	 * @param objects a list of objects
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
	 * @param destination destination array
	 * @param offset index to begin writing to in {@code destination}
	 * @param max maximum number of elements to write to {@code destination}
	 * @param objects vararg of elmeents to check and write
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
	 * @param array vararg of objects
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
	 * @param <T> type of element stored
	 * @param creator function for creating a destination array
	 * @param array vararg of objects
	 * @return Array containing the nonnull elements of {@code array}.
	 */
	@SafeVarargs public static <T> T[] fromNonNull(IntFunction<T[]> creator , T... array) {
		
		T[] nonNull = creator.apply(numberNonNull(array));
		fromNonNull(nonNull , 0 , nonNull.length , array);
		return nonNull;
		
	}
	
	/**
	 * Retrieves the first element from an array to satisfy {@code test}. 
	 * 
	 * @param <T> Type of element being stored.
	 * @param test test to apply to elements of {@code elements} 
	 * @param elements array of elements
	 * @return The first element to satisfy {@code test}, or {@code null} if none do.
	 */
	public static <T> T get(Predicate<T> test , T[] elements) {
		
		Objects.requireNonNull(elements);
		Objects.requireNonNull(test);
		
		T x = null;
		for(int i = 0 ; i < elements.length ; i++) if(test.test(x = elements[i])) return x;		
		return null;
		
	}
	
	/**
	 * Gets the first element in the list that satisfies the given {@code test}.
	 * 
	 * @param <T> Type of elements being searched.
	 * @param test test on each element
	 * @param elements list of elements
	 * @return First element that satisfies {@code test}, or {@code null} if none is found in {@code list}.
	 * @throws NullPointerException if either {@code test}, or {@code elements} is {@code null}.
	 */
	public static <T> T get(Predicate<T> test , List<T> elements) {
		
		Objects.requireNonNull(test);
		Objects.requireNonNull(test);
		if(elements instanceof RandomAccess) {
			
			int size = elements.size();
			T x = null;
			for(int i = 0 ; i < size ; i++) if(test.test(x = elements.get(i))) return x;
			
		} else for(T x : elements) if(test.test(x)) return x;

		return null;
		
	}
	
	/**
	 * Returns an iterator over an array.
	 * 
	 * @param <T> type of data stored in the array
	 * @param array array to iterate over
	 * @return Iterator over {@code array}.
	 */ 
	public static <T> ArrayIterator<T> iterator(T[] array) {
		
		return new ArrayIterator<>(array);
		
	}
	
	/**
	 * Creates and returns an array, setting each element to the result of {@code itemConstructor}. This is intended as a shorthand for creating an
	 * array with nonnull elements.
	 * 
	 * @param <T> Type of object to store
	 * @param arrayConstructor creator for the array
	 * @param itemConstructor creator for each item of the array
	 * @param items number of items to create
	 * @return Array of length {@code items} whose elements will be nonnull.
	 * @throws NullPointerException if any of {@code arrayConstructor} or {@code itemConstructor} are null.
	 * @throws NegativeArraySizeException if {@code items} is an invalid size for an array.
	 */
	public static <T> T[] initialize(IntFunction<T[]> arrayConstructor , Supplier<T> itemConstructor , int items) {
		
		Objects.requireNonNull(arrayConstructor);
		Objects.requireNonNull(itemConstructor);
		if(items <= 0) throw new NegativeArraySizeException();		
		
		T[] array = arrayConstructor.apply(items);
		for(int i = 0 ; i < array.length ; i++) array[i] = itemConstructor.get();
		return array;
		
	}
	
	/**
	 * Like {@link java.util.List#of() List.of()} but returns a modifyable list instead of an unmodifyable list.
	 * 
	 * @param <T> type of data to store
	 * @param items items to store in the new list
	 * @return Modifyable {@link List} containing the given elements in the same order they are encountered in {@code items}.
	 */
	public static <T> List<T> modifyableOf(@SuppressWarnings("unchecked") T...items) {
		
		ArrayList<T> list = new ArrayList<>(items.length);
		for(T x : items) list.add(x);
		return list;
		
	}
	
	/**
	 * Private Constructor.
	 */
	private MiscUtils() {}

}
