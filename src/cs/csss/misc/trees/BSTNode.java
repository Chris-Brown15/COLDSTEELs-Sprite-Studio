package cs.csss.misc.trees;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class BSTNode<T> implements Iterable<T> {
	
	protected Deque<T> data = new LinkedList<>();
	
	protected BSTNode<T> 
		left ,
		right , 
		parent
	;
	
	protected BSTNode(T data , BSTNode<T> left , BSTNode<T> right , BSTNode<T> parent) {
		
		this.data.add(data);
		this.left = left;
		this.right = right;
		this.parent = parent;			
		
	}
	
	@Override public String toString() {
					
		return data.getFirst().toString() + " Rate: " + data.size();
		
	}

	@Override public Iterator<T> iterator() {

		return data.iterator();
		
	}
	
}
