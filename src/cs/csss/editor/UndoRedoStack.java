package cs.csss.editor;

import cs.core.graphics.CSStandardRenderer;
import cs.core.graphics.ThreadedRenderer;
import cs.core.utils.ShutDown;
import cs.core.utils.data.CSLinkedRingBuffer;
import cs.csss.editor.event.CSSSEvent;

/**
 * 
 * Used to model an undo/redo data structure. This is essentially a FIFO ring buffer. Old events are removed and new ones are put in their 
 * place.
 * 
 */
public class UndoRedoStack {

	protected CSLinkedRingBuffer<CSSSEvent> queue;
	
	/**
	 * Creates a stack of the given size. 
	 *  
	 * @param size — size of the stack
	 */
	public UndoRedoStack(final int size) {
		
		queue = new CSLinkedRingBuffer<>(size);
	
	}
	
	CSSSEvent push(CSSSEvent event) {
		
		return queue.put(event);
				
	}

	CSSSEvent undo(CSStandardRenderer renderer , UndoRedoStack redo) {
		
		if(queue.empty()) return null;
		
		CSSSEvent undone = queue.get();

		if(undone.isRenderEvent) renderer.post(undone::undo);
		else undone.undo();
		return redo.push(undone);
	
	}

	CSSSEvent redo(CSStandardRenderer renderer , UndoRedoStack redo) {
		
		if(queue.empty()) return null;
		
		CSSSEvent undone = queue.get();

		if(undone.isRenderEvent) renderer.post(undone::_do);
		else undone._do();
		return redo.push(undone);
	
	}

	/**
	 * Returns the capacity of this stack, i.e., how many elements it can hold.
	 * 
	 * @return Capacity of this stack.
	 */
	public int capacity() {
		
		return queue.capacity();
		
	}

	/**
	 * Returns the size of this stack, that is, the current number of elements in it.
	 * 
	 * @return Number of elements in this stack.
	 */
	public int size() {
		
		return queue.size();
		
	}
	
	/**
	 * Shuts down this stack by releasing references to the events within it and freeing any memory associated with any events still in it.
	 * @param renderer
	 */
	public void shutDown(ThreadedRenderer renderer) {

		queue.forEach(event -> {
			
			if(event instanceof ShutDown asShutDown) {
				
				if(event.isRenderEvent) renderer.post(asShutDown::shutDown);
				else asShutDown.shutDown();
				
			}
			
		});

		queue = null;
		
	}

}
