package cs.csss.misc.trees;

import static cs.core.utils.CSUtils.specify;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import cs.core.utils.CSRefInt;

/**
 * Linked Binary Search Tree implementation using a {@code BiFunction} for comparison and linked deques for elements who appear to be 
 * equal to one another by the comparator. Supports adding, removing, querying, and preorder, postorder, and inorder traversal.
 * 
 * @author Chris Brown
 *
 * @param <T> — Type of data stored in the tree.
 */
public class LinkedBST<T> implements BinaryTree<T> {

	public static final byte
		PREORDER  = 0b100 ,
		POSTORDER = 0b110 ,
		INORDER   = 0b111 
	;
	
	protected BSTNode<T> root;
	
	protected int size = 0;
	
	protected byte traversalType = PREORDER;
	
	protected final BiFunction<T , T , Boolean> comparator;
		
	/**
	 * Creates a linked binary search tree in which elements are compared to one another using the given {@code BiFunction} comparator.
	 * <br><br>
	 * Let {@code elem1}, and {@code elem2} be two elements of type {@code T}. If {@code comparator.apply(elem1 , elem2)} returns 
	 * {@code true}, {@code elem1} is considered greater than {@code elem2} and moves rightward.
	 * 
	 * @param comparator — a function that compares two objects of type {@code T} and returns a boolean
	 */
	public LinkedBST(BiFunction<T , T , Boolean> comparator) {

		this(comparator , PREORDER);
		
	}

	/**
	 * Creates a linked binary search tree in which elements are compared to one another using the given {@code BiFunction} comparator.
	 * <br><br>
	 * Let {@code elem1}, and {@code elem2} be two elements of type {@code T}. If {@code comparator.apply(elem1 , elem2)} returns 
	 * {@code true}, {@code elem1} is considered greater than {@code elem2} and moves rightward.
	 * <br><br>
	 * This class offers recursive and iterative traversal approaches. In this constructor, the {@code traversalType} parameter allows the
	 * user to choose how this instance's {@link LinkedBST#iterator() iterator()} traverses.
	 * 
	 * @param comparator — a function that compares two objects of type {@code T} and returns a boolean
	 * @param traversalType — a value allowing the user to choose what iterative approach to traversal this instance will take
	 */
	public LinkedBST(BiFunction<T, T, Boolean> comparator , byte traversalType) {
		
		specify(traversalType >= PREORDER && traversalType <= INORDER , "Invalid traversal type.");
		this.comparator = comparator;
		this.traversalType = traversalType;
		
	}
	
	/**
	 * Used internally to create a new tree that is a subtree of this tree.
	 * 
	 * @param root — new root node
	 * @param comparator — comparator function
	 * @param traversalType — traversal type
	 */
	protected LinkedBST(BSTNode<T> root , BiFunction<T , T , Boolean> comparator , byte traversalType) {
		
		this.root = root;
		this.comparator = comparator;
		this.traversalType = traversalType;
		CSRefInt sizeCounter = new CSRefInt(0);
		for(@SuppressWarnings("unused") var x : this) sizeCounter.inc();
		size = sizeCounter.intValue();
		
	}
	
	/**
	 * Returns a shallow copy {@code LinkedBST} whose root node is the node containing {@code elementInThis}.
	 * 
	 * @param elementInThis — an element in {@code this}
	 * @return New LinkedBST representing a subtree of {@code this} .
	 */
	public LinkedBST<T> subTreeWithRoot(T elementInThis) {
		
		BSTNode<T> node = getNode(elementInThis).node;
		specify(node , elementInThis + " is not in this tree.");
		return new LinkedBST<>(node , comparator , traversalType);
		
	}
	
	/**
	 * Adds an element to the tree. 
	 *  
	 * @param data — data to add to the tree
	 */
	public void add(T data) {
		
		BSTNode<T> newNode = new BSTNode<>(data , null , null , null);
		
		if(root == null) root = newNode;
		else {
			
			boolean placed = false;
			BSTNode<T> iter = root;
	
			while(!placed) {
			
				if(goLeft(iter , newNode)) { 
					
					if(iter.left == null) { 
						
						makeLeftChild(iter, newNode);
						placed = true;
						
					} else iter = iter.left;
					
				} else if(goRight(iter , newNode)){ 
					
					if(iter.right == null) { 
						
						makeRightChild(iter , newNode);
						placed = true;
						
					} else iter = iter.right;
										
				} else {
					
					iter.data.add(data);
					placed = true;
					
				}
					
			}
			
		}
		
		size++;
		
	}
	
	/**
	 * Returns whether {@code data} is in this tree.
	 * 
	 * @param data — data whose presence is being queried
	 * @return {@code true} if {@code data} is in this tree, {@code false} otherwise.
	 */
	public boolean contains(T data) {
		
		return getNode(data) != null;
		
	}
		
	/**
	 * Removes an element from the tree. If this tree contains two elements whose comparator results are identical, that is to say, any of
	 * the elements will return the same boolean when used as the same parameter against any other element this method returns the most 
	 * recently added.
	 * 
	 * @param data — data to remove
	 * @return A removed element from the tree.
	 */
	public boolean remove(T data) {
		
		BSTNode<T> node = getNode(data).node;
		
		specify(node , data + " is not in this tree.");
		
		if(node.data.size() > 1) { 
			
			size--;
			return true;
			
		}
		
		if(node.right == null && node.left == null) unlink(node);
		//one of two children is null
		else if((node.right != null && node.left == null) || (node.left != null && node.right == null)) {
			
			BSTNode<T> oneChild = node.left != null ? node.left : node.right;
			
			//the removed node was the right child
			if(node.parent.right == node) node.parent.right = oneChild;
			//the removed node was the left child
			else if (node.parent.left == node) node.parent.left = oneChild;
						
		} else {
						
			/*
			 * This works by replacing the inorder successor with the node we are removing.
			 */
			
			BSTNode<T> successor = inorderSuccessor(node);
			
			if(node == root) {
				
				//detach a successor from its parent if possible
				if(successor.parent != root) { 
					
					//save the sucessor's right side if it needs to be saved
					if(successor.right != null) successor.parent.left = successor.right;
					else successor.parent.left = null;
					
				}
				
				successor.parent = null;
				
				//make the root's left and right children look to the successor instead of root
				if(root.left != null && root.left != successor) { 
					
					successor.left = root.left;
					root.left.parent = successor;
					
				}
				
				if(root.right != null && root.right != successor) {
					
					successor.right = root.right;					
					root.right.parent = successor;
					
				}
				
				root = successor;
				
			} else if(successor.parent == node) {

				//links the successor to its new parent
				if(node.parent.right == node) makeRightChild(node.parent , successor);
				else if (node.parent.left == node) makeLeftChild(node.parent , successor);
				
				//links the previous node's children to the successor
				if(node.left != null && node.left != successor) makeLeftChild(successor , node.left);
				if(node.right != null && node.right != successor) makeRightChild(successor , node.right);
				
			} else {
				
				//save the successor's child by making the removed node's right child's left child hold it
				if(successor.right != null) makeLeftChild(successor.parent , successor.right);
				//makes the successors parent not have a pointer to the successor anymore, that would cause a loop
				else successor.parent.left = null;
				
				if(node != root) {
					
					//links the successor to its new parent				
					if(node.parent.right == node) makeRightChild(node.parent , successor);
					else if (node.parent.left == node) makeLeftChild(node.parent , successor);
				
				} else root = successor;
				
				//links the previous node's children to the successor
				if(node.left != null) makeLeftChild(successor , node.left);
				if(node.right != null) makeRightChild(successor , node.right);
			
			}
						
		} 

		size--;
		return true;
		
	}
	
	/**
	 * Invokes {@code callback} for each element of this tree in the preorder traversal approach. 
	 * 
	 * @param callback — code to invoke on all elements of this tree
	 */
	public void forEachPreorder(Consumer<T> callback) {
		
		forEachPreorderInternal(callback , root);
		
	}

	/**
	 * Invokes {@code callback} for each element of this tree in the postorder traversal approach.
	 * 
	 * @param callback — code to invoke on all elements of this tree
	 */
	public void forEachPostorder(Consumer<T> callback) {
		
		forEachPostorderInternal(callback , root);
		
	}

	/**
	 * Invokes {@code callback} for each element of this tree in the inorder traversal approach.
	 * 
	 * @param callback — code to invoke on all elements of this tree
	 */
	public void forEachInorder(Consumer<T> callback) {
		
		forEachInorderInternal(callback , root);
		
	}
	
	/**
	 * Returns the root of the tree, or more specifically, the most-recently added element to the root node.
	 * 
	 * @return Data in the root node.
	 */
	public T getRoot() {
		
		if(root == null) return null;
		return root.data.getLast();
		
	}
		
	/**
	 * Computes the height of this tree.
	 * 
	 * @return Height of this tree.
	 */
	@Override public int getHeight() {
				
		return height(root);
		
	}
	
	@Override public int getHeight(T element) {
		
		return height(getNode(element).node);
		
	}

	@SuppressWarnings("unchecked") @Override public T root() {

		return (T)root.data;
		
	}

	@SuppressWarnings("unchecked") @Override public T leftmost() {

		BSTNode<T> iter = root;
		while(iter.left != null) iter = iter.left;
		return (T) iter.data;
		
	}

	@SuppressWarnings("unchecked") @Override public T rightmost() {

		BSTNode<T> iter = root;
		while(iter.right != null) iter = iter.right;
		return (T) iter.data;
	}

	@Override public int traverseType() {

		return traversalType;
		
	}	
	
	protected int height(BSTNode<T> node) {
		
		if(node == null) return 0;
		int leftHeight = height(node.left);
		int rightHeight = height(node.right);
		return leftHeight > rightHeight ? leftHeight + 1 : rightHeight + 1;
		
	}
	
	protected void forEachPreorderInternal(Consumer<T> callback , BSTNode<T> iter) {
		
		if(iter == null) return;
		
		iter.forEach(callback);
		forEachPreorderInternal(callback , iter.left);
		forEachPreorderInternal(callback , iter.right);
		
	}
	
	protected void forEachPostorderInternal(Consumer<T> callback , BSTNode<T> iter) {
		
		if(iter == null) return;
				
		forEachPostorderInternal(callback , iter.left);
		forEachPostorderInternal(callback , iter.right);
		iter.forEach(callback);
		
	}
	
	protected void forEachInorderInternal(Consumer<T> callback , BSTNode<T> iter) {
		
		if(iter == null) return;
		
		forEachInorderInternal(callback , iter.left);
		iter.forEach(callback);
		forEachInorderInternal(callback , iter.right);
		
	}
	
	protected BSTNode<T> inorderSuccessor(BSTNode<T> sucessorTo) {
		
		BSTNode<T> inorderSuccessor = null;
		BSTNode<T> iter = sucessorTo.right;
		while(inorderSuccessor == null) {
			
			if(iter.left == null) inorderSuccessor = iter;
			else iter = iter.left;
			
		}
		
		return inorderSuccessor;
		
	}
		
	protected NodeAccess<T> getNode(T data) {
		
		BSTNode<T> iter = root;
				
		int length = 0;
		
		while(iter != null) {
			
			if(goLeft(iter , data)) iter = iter.left;
			else if (goRight(iter , data)) iter = iter.right;
			else break;		
			
			length++;
			
		}
		
		return new NodeAccess<T>(iter , length);
		
	}
	
	protected void makeLeftChild(BSTNode<T> parent , BSTNode<T> left) {	
		
		parent.left = left;
		left.parent = parent;
		
	}
	
	protected void makeRightChild(BSTNode<T> parent , BSTNode<T> right) {
		
		parent.right = right;
		right.parent = parent;
		
	}
	
	protected boolean goLeft(BSTNode<T> parent , BSTNode<T> question) {
		
		return comparator.apply(parent.data.getFirst(), question.data.getFirst());
		
	}
	
	protected boolean goLeft(BSTNode<T> parent , T data) {
		
		return comparator.apply(parent.data.getFirst(), data);
		
	}
	
	protected boolean goRight(BSTNode<T> parent , BSTNode<T> question) {
		
		return comparator.apply(question.data.getFirst(), parent.data.getFirst());
		
	}

	protected boolean goRight(BSTNode<T> parent , T data) {
		
		return comparator.apply(data , parent.data.getFirst());
		
	}
	
	protected void unlink(BSTNode<T> node) { 
		
		node.right = null;
		node.left = null;
		if(node.parent.left == node) node.parent.left = null;
		else if (node.parent.right == node) node.parent.right = null;
		
	}	

	@Override public Iterator<T> iterator() {

		return new Iterator<>() {
			
			private final Queue<T> returnOrder = new LinkedList<>();
			
			{
				switch(traversalType) {
				
					case PREORDER -> forEachPreorder(returnOrder::add);
					case POSTORDER -> forEachPostorder(returnOrder::add);
					case INORDER -> forEachInorder(returnOrder::add);
										
				};
				
			}
			
			@Override public boolean hasNext() {

				return !returnOrder.isEmpty();
				
			}

			@Override public T next() {
				
				return returnOrder.poll();
				
			}
			
		};
		
	}

	protected record NodeAccess<T>(BSTNode<T> node , int pathLength) {}

}