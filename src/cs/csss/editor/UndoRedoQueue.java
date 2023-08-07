package cs.csss.editor;

import cs.core.graphics.CSStandardRenderer;
import cs.core.utils.data.CSLinkedRingBuffer;
import cs.csss.editor.events.CSSSEvent;

/**
 * 
 * Used to model an undo/redo data structure.
 *
 */
public class UndoRedoQueue {

	protected CSLinkedRingBuffer<CSSSEvent> queue;
	
	public UndoRedoQueue(final int size) {
		
		queue = new CSLinkedRingBuffer<>(size);
	
	}
	
	protected void push(CSSSEvent event) {
		
		queue.put(event);
		
	}

	protected void undo(CSStandardRenderer renderer , UndoRedoQueue redo) {
		
		if(queue.empty()) return;
		
		CSSSEvent undone = queue.get();

		if(undone.isRenderEvent) renderer.post(undone::undo);
		else undone.undo();
		redo.push(undone);
	
	}

	protected void redo(CSStandardRenderer renderer , UndoRedoQueue redo) {
		
		if(queue.empty()) return;
		
		CSSSEvent undone = queue.get();

		if(undone.isRenderEvent) renderer.post(undone::_do);
		else undone._do();
		redo.push(undone);
	
	}

	public boolean isEmpty() {
		
		return queue.empty();
		
	}
	
	public int size() {
		
		return queue.size();
		
	}

	public int capacity() {
		
		return queue.capacity();
		
	}
	
}
