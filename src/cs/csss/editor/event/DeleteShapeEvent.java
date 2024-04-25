/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;

import cs.core.utils.ShutDown;
import cs.csss.editor.Editor;
import cs.csss.editor.shape.Shape;
import cs.csss.project.Artboard;
import cs.csss.project.Layer;

/**
 * Event to remove a shape from an artboard. The shape should be in the artboard when the event is created.
 */
public class DeleteShapeEvent extends CSSSEvent implements ShutDown {

	private final Layer owningLayer;
	private final Shape shape;
	private final Editor editor;
	private final boolean wasActive;
	
	/**
	 * Event to remove a shape from the given artboard.
	 * 
	 * @param artboard the artboard to remove from
	 * @param shape the shape to remove from {@code artboard}
	 * @param editor the editor
	 */
	public DeleteShapeEvent(Artboard artboard , Shape shape , Editor editor) {

		super(true , false);
		this.owningLayer = Objects.requireNonNull(artboard).activeLayer();
		this.shape = Objects.requireNonNull(shape);
		this.editor = Objects.requireNonNull(editor);
		wasActive = editor.activeShape() == shape;
				
	}

	@Override public void _do() {

		owningLayer.removeShape(shape);
		if(wasActive) editor.activeShape(null);
		
	}

	@Override public void undo() {

		owningLayer.addShape(shape);
		if(wasActive) editor.activeShape(shape);
		
	}

	@Override public void shutDown() {
		
		shape.shutDown();
				
	}

	@Override public boolean isFreed() {

		return shape.isFreed();
		
	}

}
