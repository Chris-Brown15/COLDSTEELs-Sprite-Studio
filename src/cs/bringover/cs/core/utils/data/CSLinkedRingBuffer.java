package cs.bringover.cs.core.utils.data;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import sc.core.utils.SCIntReferencer;

public class CSLinkedRingBuffer <T> {

	public final int capacity;
	
	private LRBNode head = null;
	private LRBNode tail = null;
		
	public CSLinkedRingBuffer(final int size) {	
	
		if(size <= 1) throw new IllegalArgumentException(size + " invalid as a size.");
		this.capacity = size;				
		for(int i = 0 ; i < size ; i ++) newNode();	
		
	}
	
	public T put(T elem) {
		
		LRBNode oldTail = tail;
		T oldVal = oldTail.val;
		oldTail.val = elem;
	
		head.prev = oldTail;
		oldTail.next = head;
		
		tail = oldTail.prev;
		head = oldTail;
				
		return oldVal;
		
	}
		
	public T get() {
		
		LRBNode oldHead = head;
		T gotten = oldHead.val;
		oldHead.val = null;
		
		head = oldHead.next;
		tail = oldHead;
		
		return gotten;
		
	}
	
	public T getAndPut() {
		
		T element = get();
		put(element);
		return element;
		
	}
	
	private void newNode() {

		if(head == null) head = new LRBNode();
		else if (tail == null) {
			
			tail = new LRBNode();
			tail.next = head;
			tail.prev = head;
			head.prev = tail;
			head.next = tail;
			
		} else {
			
			LRBNode node = new LRBNode();
			tail.next = node;
			node.prev = tail;
			
			head.prev = node;
			node.next = head;
			
			tail = node;
			
		}
		
	}
		
	public void forEach(Consumer<T> callback) {
	
		LRBNode iter = head;
		for(int i = 0 ; i < capacity ; i++ , iter = iter.next) if(iter.val != null) callback.accept(iter.val);
		
	}

	public void forEachIndexed(BiConsumer<Integer , T> callback) {
	
		LRBNode iter = head;
		for(int i = 0 ; i < capacity ; i++ , iter = iter.next) if(iter.val != null) callback.accept(i , iter.val);
		
	}

	/**
	 * Like {@code forEach} but null elements are passed to {@code callback}.
	 * 
	 * @param callback
	 */
	public void forEachUnsafe(Consumer<T> callback) {
	
		LRBNode iter = head;
		for(int i = 0 ; i < capacity ; i++ , iter = iter.next) callback.accept(iter.val);
		
	}
	
	public int capacity() {
		
		return capacity;
		
	}
	
	public int size() { 
		
		SCIntReferencer size = new SCIntReferencer(0);
		
		forEachUnsafe(elem -> {
			
			if(elem != null) size.mutate(prev -> prev + 1);
			
		});
		
		return size.get();
		
	}
	
	public boolean empty() {
		
		return size() == 0;
		
	}
	
	private class LRBNode {
		
		LRBNode next;
		LRBNode prev;
		T val;
				
	}
	
}
