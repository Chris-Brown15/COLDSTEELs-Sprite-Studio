/**
 * Licensed under MIT.
 */
package cs.sprites;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unmodifiable list (modification attempts except) representing a set of animation frames. 
 * 
 * @author Chris Brown
 */
public class CSFrameSet implements List<CSAnimationFrame> , RandomAccess , Comparable<CSFrameSet> , Cloneable {

	private final CSAnimationFrame[] frames;
	
	/**
	 * Constructs a {@code FrameSet} from an array of {@code FrameChunk}s. This constructor is useful for when a CTSA file is loaded. 
	 * 
	 * @param chunks — array of frame chunks
	 */
	protected CSFrameSet(CSFrameChunk[] chunks) {
		
		frames = new CSAnimationFrame[chunks.length];
		for(int i = 0 ; i < size() ; i++) frames[i] = new CSAnimationFrame(chunks[i].time() , chunks[i].updates() , chunks[i].swapType());
		
	}

	/**
	 * Constructs a {@code FrameSet} by deep copying an existing {@code FrameSet}. The resulting {@code FrameSet} will have its elements
	 * begin at index 0 and its elements will be a deep copy of {@code otherSet}'s elements from the given begin index to the given end
	 * index, inclusive.
	 * 
	 * @param otherSet — another frame set
	 * @param subListBeginIndex — index of {@code otherSet} to begin deep copying
	 * @param subListEndIndex — index of {@code otherSet} to end deep copying
	 */
	public CSFrameSet(CSFrameSet otherSet , int subListBeginIndex , int subListEndIndex) {
		
		Objects.checkFromToIndex(subListBeginIndex, subListEndIndex + 1 , otherSet.size());
		
		int numberElements = (subListEndIndex - subListBeginIndex) + 1;
		
		frames = new CSAnimationFrame[numberElements];
		for(int i = subListBeginIndex , j = 0 ; i < numberElements ; i++ , j++) { 

			CSAnimationFrame otherFrame = otherSet.frames[i];
			frames[j] = new CSAnimationFrame(otherFrame.time() , otherFrame.updates() , otherFrame.swapType());
			
		}
		
	}

	@Override public int size() {

		return frames.length;
		
	}

	@Override public boolean isEmpty() {

		return size() == 0;
		
	}

	@Override public boolean contains(Object otherFrame) {

		CSAnimationFrame asFrame = (CSAnimationFrame) otherFrame;
		for(CSAnimationFrame x : frames) if(x == asFrame) return true;		
		return false;
		
	}

	@Override public Iterator<CSAnimationFrame> iterator() {

		return new FrameSetIterator();
		
	}

	@Override public Object[] toArray() {

		throw new UnsupportedOperationException("Cannot retrieve this set's array.");
		
	}

	@Override public <T> T[] toArray(T[] a) {

		throw new UnsupportedOperationException("Cannot retrieve this set's array.");
		
	}

	@Override public boolean add(CSAnimationFrame e) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public boolean remove(Object o) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public boolean containsAll(Collection<?> c) {
		
		Collection<?> 
			shorterCollection = c.size() < size() ? c : this ,
			longerCollection = shorterCollection == c ? this : c;
		
		for(Object x : longerCollection) if(!shorterCollection.contains(x)) return false;
		return true;
		
	}

	@Override public boolean addAll(Collection<? extends CSAnimationFrame> c) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public boolean retainAll(Collection<?> c) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public boolean removeAll(Collection<?> c) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public void clear() {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public boolean addAll(int index, Collection<? extends CSAnimationFrame> c) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public CSAnimationFrame get(int index) {

		Objects.checkIndex(index, size());
		
		return frames[index];
		
	}

	@Override public CSAnimationFrame set(int index, CSAnimationFrame element) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public void add(int index, CSAnimationFrame element) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public CSAnimationFrame remove(int index) {

		throw new UnsupportedOperationException("Cannot modify this set's contents.");
		
	}

	@Override public int indexOf(Object o) {
		
		for(int i = 0 , size = size() ; i < size ; i++) if(frames[i].hashCode() == o.hashCode() && frames[i].equals(o)) return i;		
		throw new NoSuchElementException(o + " was not found in this animation.");
		
	}

	@Override public int lastIndexOf(Object o) {
		
		for(int i = size() - 1 ; i >= 0 ; i--) if(frames[i].hashCode() == o.hashCode() && frames[i].equals(o)) return i;		
		throw new NoSuchElementException(o + " was not found in this animation.");
		
	}

	@Override public ListIterator<CSAnimationFrame> listIterator() {

		return new FrameSetIterator();
		
	}

	@Override public ListIterator<CSAnimationFrame> listIterator(int index) {
		
		return new FrameSetIterator(index);
		
	}

	/**
	 * It is true that this method returns a view of an underlying list, but the resulting list is deep copied. Therefore the specification
	 * of the {@link java.util.List#subList(int, int) subList} method is satisfied, but in actuality this method works differently than 
	 * other implementations.
	 */
	@Override public List<CSAnimationFrame> subList(int fromIndex, int toIndex) {

		return new CSFrameSet(this, fromIndex, toIndex);
		
	}
	
	/**
	 * Compares two frame sets. A set is greater if the total time taken to complete it raised to the power of the number of elements
	 * 
	 * 
	 * @param other — another frame set
	 * @return Result of comparing sets, will be greater than zero if the caller is 'greater than' other, zero if they are 'equal,' and 
	 * 		   negative if other is greater.
	 */
	@Override public int compareTo(CSFrameSet other) {

		return hashCode() - other.hashCode();
		
	}
	
	/**
	 * The hash code of a frame set is given by the total time in milliseconds to complete it (or a best guess of the time), raised to the 
	 * power of the number of elements.
	 * 
	 * <p>
	 * 	Animation frame data is currently completely immutable so this method satisfies its contracts.
	 * </p>
	 * 
	 */
	@Override public int hashCode() {
	
		var iter = iterator();
		float timeAccum = 0;
		while(iter.hasNext()) {
			
			var x = iter.next();
			if(x.swapType() == CSAnimationFrame.SWAP_BY_TIME) timeAccum += x.time();
			//16.667 is the number of milliseconds one frame would take if a user were targeting 60 fps.
			else timeAccum += (x.updates() * 16.667);
						
		}
		
		timeAccum = (float) Math.pow(timeAccum, size());
		return (int) timeAccum;
		
	}
	
	/**
	 * Returns whether {@code other} is a {@code CSFrameSet} and both require the same exact amount of time to complete and has the same 
	 * number of frames. 
	 */
	@Override public boolean equals(Object other) {
	
		return other instanceof CSFrameSet && other.hashCode() == this.hashCode();
		
	}
	
	/**
	 * Returns a deep copy of {@code this}. Theoretically, a deep copy is unnecessary becaues all state of this class is immutable, so a 
	 * deep copy is indistinguishable from a shallow copy. This method will still deep copy just in case in the future, code is modified
	 * such that this class is no long completely immutable.
	 * 
	 * @return Deep copy of {@code this}.
	 */
	@Override public CSFrameSet clone() { 
		
		return new CSFrameSet(this , 0 , size() - 1);
		
	}
	
	/**
	 * List iterator for this frame set. Capable of traversal up and down but not structural modification.
	 */
	private class FrameSetIterator implements ListIterator<CSAnimationFrame> {

		private AtomicInteger next = new AtomicInteger(0);
		
		FrameSetIterator(int startPosition) {
			
			Objects.checkIndex(startPosition, size());
			next.set(startPosition);
			
		}

		FrameSetIterator() {}
		
		@Override public boolean hasNext() {

			return next.get() < size();
			
		}

		@Override public CSAnimationFrame next() {

			return frames[next.getAndIncrement()];
			
		}

		@Override public boolean hasPrevious() {

			return next.get() > 0;
			
		}

		@Override public CSAnimationFrame previous() {

			return frames[next.getAndDecrement()];
			
		}

		@Override public int nextIndex() {

			return next.get();
			
		}

		@Override public int previousIndex() {

			return next.get() - 1;
			
		}

		@Override public void remove() {

			throw new UnsupportedOperationException("Cannot modify this set's contents.");
			
		}

		@Override public void set(CSAnimationFrame e) {

			throw new UnsupportedOperationException("Cannot modify this set's contents.");
			
		}

		@Override public void add(CSAnimationFrame e) {

			throw new UnsupportedOperationException("Cannot modify this set's contents.");
			
		}
		
	}
	
}
