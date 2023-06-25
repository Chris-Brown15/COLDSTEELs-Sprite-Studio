package cs.csss.misc.trees;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Standard interface for binary tree data structures. 
 * 
 * @author Chris Brown
 *
 * @param <T> — Data type this tree stores.
 */
public interface BinaryTree<T> extends Iterable<T> {

	public static final byte 
		TRAVERSE_PREORDER = 0b1,
		TRAVERSE_POSTORDER = 0b11 ,
		TRAVERSE_INORDER = 0b111
	;
	
	/**
	 * Adds {@code T} according to the semantics of the implementor.
	 * 
	 * @param element — data to add
	 */
	public void add(T element);
	
	/**
	 * Removes {@code T} according to the semantics of the implementor.
	 * 
	 * @param element — element to remove
	 * @return {@code true} if the removal succeeded.
	 */
	public boolean remove(T element);
	
	/**
	 * Computes or returns the height of this tree.
	 * 
	 * @return Height of this tree.
	 */
	public int getHeight();
	
	/**
	 * Computes or returns the height of {@code element} within the tree.
	 * 
	 * @param element — element of this tree
	 * @return Height of this element.
	 */
	public int getHeight(T element);
	
	/**
	 * Gets the root element of this tree.
	 * 
	 * @return Root of this tree.
	 */
	public T root();
	
	/**
	 * Gets the leftmost element of this tree.
	 * 
	 * @return Leftmost element of this tree.
	 */
	public T leftmost();
	
	/**
	 * Gets the rightmost element of this tree.
	 * 
	 * @return Rightmost element of this tree.
	 */
	public T rightmost();
	
	/**
	 * Iterates over the elements of this tree in the preorder traversal approach.
	 * 
	 * @param callback — code to invoke on each element of this tree
	 */
	public void forEachPreorder(Consumer<T> callback);

	/**
	 * Iterates over the elements of this tree in the postorder traversal approach.
	 * 
	 * @param callback — code to invoke on each element of this tree
	 */
	public void forEachPostorder(Consumer<T> callback);

	/**
	 * Iterates over the elements of this tree in the inorder traversal approach.
	 * 
	 * @param callback — code to invoke on each element of this tree
	 */
	public void forEachInorder(Consumer<T> callback);

	/**
	 * Returns the traversal type of this tree. This is an optional operation and may not do anything.
	 * 
	 * @return Traversal type selector of this tree, one of:
	 * <ul> 
	 * 	<li> 
	 * 		{@code TRAVERSAL_PREORDER} 
	 * 	</li>
	 * 	<li> 
	 * 		{@code TRAVERSAL_POSTORDER}
	 * 	</li>
	 *  <li> 
	 * 		{@code TRAVERSAL_INORDER}
	 * 	</li>
	 * </ul> 
	 */
	public default int traverseType() {
		
		throw new UnsupportedOperationException("This tree data structure does not support traversal types.");
		
	}
		
	/**
	 * Returns a default iterator for use in the {@code for(T x : this)} structure. This iterator copies the elements of this tree into a
	 * queue and dequeues them from the queue in an order according to the {@code traversalType()} of the implementor of this interface.
	 * Note that if {@code traversalType()} throws an exception, this will too. 
	 */
	public default Iterator<T> iterator() {
		
		return new Iterator<T>() {

			private Queue<T> order = new LinkedList<T>();
			
			{				
				switch(traverseType()) {
					case TRAVERSE_PREORDER -> forEachPreorder(order::add);
					case TRAVERSE_POSTORDER -> forEachPostorder(order::add);
					case TRAVERSE_INORDER -> forEachInorder(order::add);
				}	
			}
			
			@Override public boolean hasNext() {

				return !order.isEmpty();
				
			}

			@Override public T next() {

				return order.poll();
				
			}
			
		};
		
	}
	
}