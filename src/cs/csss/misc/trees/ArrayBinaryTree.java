package cs.csss.misc.trees;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import cs.core.utils.CSRefInt;

/**
 * Array-based unsorted binary tree implementation.
 * 
 * @author Chris Brown
 *
 * @param <T> — Type of data this tree will store.
 */
public class ArrayBinaryTree<T> implements BinaryTree<T> {

	private Object[] tree;
	
	private int next = 1;
	private byte traverseType = TRAVERSE_PREORDER;
	
	private boolean resize = true;
	
	/**
	 * Initializes this tree.
	 * 
	 * @param size
	 */
	public ArrayBinaryTree(final int size) {
		
		tree = new Object[size + 1];
		
	}
	
	public void add(T element) {
		
		if(next == tree.length) resize();
		tree[next++] = element;		
		
	}

	@SuppressWarnings("unchecked") public T rightmost() {
		
		int index = 1;
		int iters = next >> 1;
		while(index < iters) index = (index << 1) + 1;
		return (T) tree[index];
		
	}
	
	public boolean remove(T object) {
		
		T removed = remove(indexOf(object));
		return removed != null;
		
	}
	
	public T remove(int index) {
		
		@SuppressWarnings("unchecked") T removed = (T) tree[index];
		tree[index] = tree[--next];
		return removed;
		
	}
	
	/**
	 * Computes the index of the element {@code object}, returning it.
	 * 
	 * @param object — an object whose index in this tree is being queried
	 * @return Index of {@code Object}, or {@code -1} if it is not found.
	 */
	public int indexOf(T object) {
		
		CSRefInt index = new CSRefInt(-1);
		
		forEachPreorderIndexed((element , elementsIndex) -> {
			
			if(index.intValue() != -1) return;
			
			if(element == object) index.set(elementsIndex);
			
		});
		
		return index.intValue();
		
	}
	
	/**
	 * Gets the parent of {@code someChild}.
	 * 
	 * @param someChild — an element of this tree
	 * @return Parent node of {@code someChild}.
	 */
	@SuppressWarnings("unchecked") public T getParentOf(T someChild) {
		
		if(someChild == tree[1]) return someChild;
		
		return (T)tree[indexOf(someChild) >> 1];
		
	}

	@Override public int getHeight() {
		
		return getHeight(1);
		
	}
	
	@Override public int getHeight(T element) {
		
		return getHeight(indexOf(element));
		
	}
	
	@SuppressWarnings("unchecked") @Override public T root() {
		
		return (T)tree[1];
		
	}

	@SuppressWarnings("unchecked") @Override public T leftmost() {

		int index = 1;
		Object current = tree[index];
		while(tree[index] != null) {
			
			current = tree[index];
			index <<= 1;
			//special case when we have reached the end of the tree or are at the last element
			if(index > tree.length) break;
			
		}
		
		return (T) current;
		
	}

	@Override public int traverseType() {
		
		return traverseType;
		
	}
	
	/**
	 * Returns the number of elements in this tree.
	 * 
	 * @return Number of elements in this tree.
	 */
	public int size() {
		
		return next - 1;
		
	}

	@Override public void forEachPreorder(Consumer<T> callback) {
		
		forEachPreorderInternal(callback , 1);
		
	}
	
	@Override public void forEachPostorder(Consumer<T> callback) {
		
		forEachPostorderInternal(callback , 1);
		
	}
	
	@Override public void forEachInorder(Consumer<T> callback) {
		
		forEachInorderInternal(callback , 1);
		
	}

	public void forEachPreorderIndexed(BiConsumer<T , Integer> callback) {
		
		forEachPreorderIndexedInternal(callback , 1);
		
	}
	
	public void forEachPostorderIndexed(BiConsumer<T , Integer> callback) {
		
		forEachPostorderIndexedInternal(callback , 1);
		
	}
	
	public void forEachInorderIndexed(BiConsumer<T , Integer> callback) {
		
		forEachInorderIndexedInternal(callback , 1);
		
	}

	@SuppressWarnings("unchecked") protected void forEachPreorderInternal(Consumer<T> callback , int rootIndex) {
		
		if(rootIndex >= next) return;
		callback.accept((T) tree[rootIndex]);
		forEachPreorderInternal(callback , 2 * rootIndex);
		forEachPreorderInternal(callback , (2 * rootIndex) + 1);
		
	}
	
	@SuppressWarnings("unchecked") protected void forEachPostorderInternal(Consumer<T> callback , int rootIndex) {

		if(rootIndex >= next) return;
		forEachPostorderInternal(callback , 2 * rootIndex);
		forEachPostorderInternal(callback , (2 * rootIndex) + 1);
		callback.accept((T) tree[rootIndex]);

	}
	
	@SuppressWarnings("unchecked") protected void forEachInorderInternal(Consumer<T> callback , int rootIndex) {

		if(rootIndex >= next);
		forEachInorderInternal(callback , 2 * rootIndex);
		callback.accept((T)tree[rootIndex]);
		forEachInorderInternal(callback , (2 * rootIndex) + 1);

	}
	
	@SuppressWarnings("unchecked") protected void forEachPreorderIndexedInternal(BiConsumer<T , Integer> callback , int rootIndex) {

		if(rootIndex >= next) return;
		callback.accept((T)tree[rootIndex] , rootIndex);
		forEachPreorderIndexedInternal(callback , 2 * rootIndex);
		forEachPreorderIndexedInternal(callback , (2 * rootIndex) + 1);

	}
	
	@SuppressWarnings("unchecked") protected void forEachPostorderIndexedInternal(BiConsumer<T , Integer> callback , int rootIndex) {

		if(rootIndex >= next) return;
		forEachPostorderIndexedInternal(callback , 2 * rootIndex);
		forEachPostorderIndexedInternal(callback , (2 * rootIndex) + 1);
		callback.accept((T) tree[rootIndex] , rootIndex);

	}
	
	@SuppressWarnings("unchecked") protected void forEachInorderIndexedInternal(BiConsumer<T , Integer> callback , int rootIndex) {

		if(rootIndex >= next) return;
		forEachInorderIndexedInternal(callback , 2 * rootIndex);
		callback.accept((T)tree[rootIndex] , rootIndex);
		forEachInorderIndexedInternal(callback , (2 * rootIndex) + 1);

	}
	
	protected void resize() {
		
		if(!resize) return;
		
		Object[] newTree = new Object[(tree.length - 1 << 1) + 1];
		System.arraycopy(tree, 1, newTree, 1, tree.length);
		tree = newTree;
		
	}
	
	protected int getHeight(int nodeIndex) {

		if(nodeIndex >= tree.length || tree[nodeIndex] == null) return 0;
		int 
			left = getHeight((nodeIndex << 1)) ,
			right = getHeight((nodeIndex << 1) + 1)
		;
		
		return left > right ? left + 1 : right + 1;
		
	}

}