package cs.csss.misc.trees;

import static cs.core.utils.CSUtils.specify;
import static cs.core.utils.CSUtils.require;

import java.util.function.BiFunction;

/**
 * Splay Tree implementation extending from {@link cs.csss.misc.trees.LinkedBST LinkedBST}. This class functions identically to 
 * {@code LinkedBST} with the additional optional property of splaying on various operations. A splay is an operation that rotates a node
 * up the tree until it is the root of the tree, while maintaining the binary search tree requirements. This class allows the user to only
 * splay on all, none, or some operations.
 * 
 * @author Chris Brown
 *
 * @param <T> — Type of data stored in the tree.
 */
public class SplayTree<T> extends LinkedBST<T> {

	public static short
		SPLAY_WHEN_REMOVING = 0b1 << 9 ,
		SPLAY_WHEN_FINDING = 0b1 << 10 ,
		SPLAY_WHEN_ADDING = 0b1 << 11 ,
		SPLAY_ON_COLD_READ = 0b1 << 12 ;

	private static void verifySplayCaseLiteral(short splayCases) {
		
		final int all = SPLAY_WHEN_REMOVING|SPLAY_WHEN_ADDING|SPLAY_WHEN_FINDING|SPLAY_ON_COLD_READ , AND = splayCases & all;		
		specify(AND >= SPLAY_WHEN_REMOVING && AND <= all , splayCases + " is not a valid splay case.");		
		
	}
	
	/**
	 * A cold read is considered to have occured when the path lenght to access an element is longer than this.
	 */
	private int coldReadThreshhold = 4;
	
	/**
	 * Combination of splay when literals
	 */
	private short splayWhen = (short) (SPLAY_WHEN_FINDING);
	
	/**
	 * This Constructor attempts to cast T to {@link Comparable<T>} and compare two operands using the 
	 * {@link Comparable<T>#compareTo() compareTo()}.
	 */
	@SuppressWarnings("unchecked") public SplayTree() {
		
		super((elem1 , elem2) -> ((Comparable<T>)elem1).compareTo(elem2) > 0 , PREORDER);
		
	}
	
	/**
	 * Creates a new splay tree with the given comparator, the iteration type set to {@code PREORDER}, and the splay cases set to 
	 * {@code FIND}.
	 * 
	 * @param comparator — a function that compares two objects of type {@code T} and returns a boolean
	 */
	public SplayTree(BiFunction<T , T , Boolean> comparator) {
		
		super(comparator , PREORDER);
		
	}
	
	/**
	 * Creates a new splay tree with the given comparator and splay case.
	 * 
	 * @param comparator — a function that compares two objects of type {@code T} and returns a boolean
	 * @param splayCases — one or a combination of splay cases bitwise ORed to make a single value
	 */
	public SplayTree(BiFunction<T, T, Boolean> comparator , short splayCases) {
		
		super(comparator);
		verifySplayCaseLiteral(splayCases);
		this.splayWhen = splayCases;
		
	}

	/**
	 * Creates a new splay tree with the given comparator, traversal type, and splay cases.
	 * 
	 * @param comparator — a function that compares two objects of type {@code T} and returns a boolean
	 * @param traversalType — a value allowing the user to choose what iterative approach to traversal this instance will take
	 * @param splayCases — one or a combination of splay cases bitwise ORed to make a single value
	 */
	public SplayTree(BiFunction<T, T, Boolean> comparator, byte traversalType , short splayCases) {
		
		super(comparator, traversalType);
		verifySplayCaseLiteral(splayCases);
		this.splayWhen = splayCases;
		
	}

	/**
	 * Creates a splay tree as a shallow copy and standalone subtree of another tree.
	 * 
	 * @param root — root of this tree, presumably a node of another tree
	 * @param comparator — comparator function
	 * @param traversalType — traversal type
	 * @param splayCases — splay cases
	 */
	protected SplayTree(BSTNode<T> root, BiFunction<T, T, Boolean> comparator, byte traversalType , short splayCases) {
		
		super(root, comparator, traversalType);
		verifySplayCaseLiteral(splayCases);
		this.splayWhen = splayCases;
		
	}
	
	/**
	 * Adds the given splay case to this splay tree's current splay cases.
	 * 
	 * @param _case — a splay case to add
	 */
	public void addSplayCase(short _case) {
		
		verifySplayCaseLiteral(_case);		
		splayWhen |= _case;
		
	}
	
	/**
	 * Removes the given splay case from this splay tree's splay cases. 
	 * 
	 * @param _case — a splay case to remove
	 */
	public void removeSplayCase(short _case) {

		verifySplayCaseLiteral(_case);		
		splayWhen &= ~_case;
		
	}
	
	@Override public boolean remove(T data) {
		
		BSTNode<T> node = getNode(data).node();		
		if(splayWhen(SPLAY_WHEN_REMOVING)) fullSplay(node);
		
		return super.remove(data);
		
	}

	/**
	 * Checks whether {@code data} exists in this tree, optionally splaying it to the top if {@code SPLAY_WHEN_FINDING} is set or if
	 * {@code SPLAY_ON_COLD_READ} is true and the path length to access the node housing {@code data} is longer than 
	 * {@code coldReadThreshhold}.
	 */
	@Override public boolean contains(T data) {
		
		NodeAccess<T> access = getNode(data);
		if(
			access.node() != null && 
			(splayWhen(SPLAY_WHEN_FINDING) || (splayWhen(SPLAY_ON_COLD_READ) && isColdRead(access.pathLength())))
		) {
			
			fullSplay(access.node());
			
		}
		
		return access.node() != null;
		
	}
	
	@SuppressWarnings("unchecked") public T get(T data) {
		
		NodeAccess<T> access = getNode(data);
		if(
			access.node() != null && 
			(splayWhen(SPLAY_WHEN_FINDING) || (splayWhen(SPLAY_ON_COLD_READ) && isColdRead(access.pathLength())))
		) {
			
			fullSplay(access.node());
			
		}
		
		return (T) access.node().data;

	}
	
	/**
	 * Adds {@code data} to this tree, optionally splaying it to the root if {@code SPLAY_WHEN_ADDING} is set.
	 */
	@Override public void add(T data) {
		
		super.add(data);
		if(splayWhen(SPLAY_WHEN_ADDING)) { 
			
			NodeAccess<T> node = getNode(data);
			fullSplay(node.node());
			
		}
		
	}
		
	/**
	 * Returns the cold read threshhold.
	 * 
	 * @return Cold read threshold, the minimum length an access path can be before splaying may occur.
	 */
	public int coldReadThreshhold() {
		
		return coldReadThreshhold;
		
	}
	
	/**
	 * Sets the cold read threshhold of this tree. Only has any effect if {@code SPLAY_ON_COLD_READ} is set. Must be positive.
	 * 
	 * @param threshhold — new cold read threshhold, the minimum length an access path can be before splaying may occur
	 */
	public void coldReadThreshhold(int threshhold) {
		
		specify(threshhold > 0 , threshhold + " is not a valid cold read threshhold. Must be positive.");
		this.coldReadThreshhold = threshhold;
		
	}
	
	/**
	 * Zig operation is a right rotation. {@code node} will occupy the spot of its parent and its parent will rotate rightward.
	 * 
	 * @param node — a node to rotate right
	 */
	protected void zig(BSTNode<T> node) {
		
		require(node.parent != null);
		require(node.parent.left == node);
		
		BSTNode<T> parent = node.parent;
		
		//saves the node's right
		parent.left = node.right;
		if(node.right != null) node.right.parent = parent;
		
		//puts the node in its parent's place
		node.right = parent;		
		node.parent = parent.parent;
		
		//set up grandparents
		if(parent.parent != null) {
			
			//makes the grandparent point to the node
			if(parent.parent.right == parent) parent.parent.right = node;
			else parent.parent.left = node;
			
		}
		//if the parent is the root we reset it
		else if(parent == root) root = node;
			
		parent.parent = node;
		
	}
	
	/**
	 * Zag operation is a left rotation. {@code node} will occupy the spot of its parent and its parent will rotate leftward. This operation
	 * is symmetrical to {@code zig}, so no comments are included here, see {@link SplayTree#zig(BSTNode) zig()} for explanations on what
	 * each line does.
	 * 
	 * @param node — a node to rotate left
	 */
	protected void zag(BSTNode<T> node) {
		
		require(node.parent != null);
		require(node.parent.right == node);
		
		BSTNode<T> parent = node.parent;
		
		parent.right = node.left;
		if(node.left != null) node.left.parent = parent;
		node.left = parent;
		node.parent = parent.parent;

		if(parent.parent != null) {
			
			if(parent.parent.right == parent) parent.parent.right = node;
			else parent.parent.left = node;
			
		} else if (parent == root) root = node;

		parent.parent = node;
		
	}
	
	/**
	 * Single splay operation. After this, node will either be the root or closer to the root by one or two heights.
	 * 
	 * @param node — node to splay
	 */
	protected void splay(BSTNode<T>  node) {
		
		if(root.left == node) zig(node);
		else if (root.right == node) zag(node);
		//line of left nodes
		else if(node.parent.left == node && node.parent.parent.left == node.parent) {
			
			zig(node);
			zig(node);
			
		}
		//line of left nodes
		else if (node.parent.right == node && node.parent.parent.right == node.parent) {
			
			zag(node);
			zag(node);
			
		}
		//left-right-left structure
		else if (node.parent.left == node && node.parent.parent.right == node.parent) {
			
			zig(node);
			zag(node);
			
		}
		//right-left-right structure
		else if(node.parent.right == node && node.parent.parent.left == node.parent) {
			
			zag(node);
			zig(node);
			
		}
		
	}

	/**
	 * Continually splays until {@code node} is the root of this tree.
	 * 
	 * @param node — node to splay
	 */
	protected void fullSplay(BSTNode<T> node) {
		
		while(node != root) splay(node);
		
	}
	
	/**
	 * Checks whether we can splay based on the splay option being present in the splayWhen value.
	 * 
	 * @param splayOption — a splay option, one of static constants of this class
	 * @return {@code true} if the splay option is present in {@code splayWhen}.
	 */
	protected boolean splayWhen(int splayOption) {
		
		return (splayWhen & splayOption) == splayOption;
		
	}
	
	/**
	 * Returns {@code true} if an access path of length {@code pathLength} would indicate a cold read.
	 * 
	 * @param pathLength — length of an access path
	 * @return {@code true} if the access path of length {@code pathLength} surpasses the cold read threshhold.
	 */
	protected boolean isColdRead(int pathLength) {
		
		return pathLength >= 4;
		
	}

}