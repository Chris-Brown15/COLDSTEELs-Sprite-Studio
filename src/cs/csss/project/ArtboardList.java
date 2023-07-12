package cs.csss.project;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Purpose-built storage for Artboards. Anything which wants to store artboards should use this. 
 * 
 * <p>
 * 	This class supports random access and and is a {@link java.util.List List}. This class supports concurrent appending of artboards,
 * 	thread-safe random index removal via a queue system, and grows in size flatly, similar to the {@link java.util.Vector Vector}. 
 * </p>
 * 
 * @author Chris Brown
 * 
 */
public class ArtboardList implements List<Artboard>, RandomAccess {

	private volatile Position[] artboards = new Position[10];
	/**
	 * Stores the next index to place an artboard at.
	 */
	private final AtomicInteger next = new AtomicInteger(0);
	
	public ArtboardList() {

	}
	
	

	public void updatePositions() {
		
	}

	@Override public int size() {

		return next.get();
	}

	@Override public boolean isEmpty() {

		return size() == 0;
	}

	@Override public boolean contains(Object o) {

		Iterator<Artboard> iterator = iterator();
		while(iterator.hasNext()) if(iterator.next() == o) return true;			
		return false;
		
	}

	@Override public Iterator<Artboard> iterator() {

		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(Artboard e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Artboard> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Artboard> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Artboard get(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Artboard set(int index, Artboard element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void add(int index, Artboard element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Artboard remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<Artboard> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<Artboard> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Artboard> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	private class Position {
		
		static final int 
			STATE_NORMAL = 0 ,
			TOMBSTONED = 1
		;
		
		Artboard artboard;		
		AtomicInteger state = new AtomicInteger(0);
		
		int state() {
			
			return state.get();
			
		}
		
		void markTombstoned() {
			
			state.set(TOMBSTONED);
			
		}
		
		void markNormal() {
			
			state.set(STATE_NORMAL);
			
		}
		
	}
	
	public class ArtboardIterator implements Iterator<Artboard> {

		private ArtboardIterator() {}
		
		AtomicInteger next = new AtomicInteger(0);
				
		@Override public void remove() {
			
		}
		
		@Override public boolean hasNext() {

			
			return false;
			
		}

		@Override public Artboard next() {

			return null;
			
		}
		
	}
	
}
