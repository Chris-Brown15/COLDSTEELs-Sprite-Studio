/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;

import cs.core.utils.ShutDown;
import cs.csss.editor.shape.Shape;
import cs.csss.project.Artboard;
import cs.csss.project.Layer;

/**
 * Event representing transferring a shape from the shape brush to the active layer of the current artboard.
 */
public class TransferShapeEvent extends CSSSEvent implements ShutDown {

	private final Layer destination;
	private final Shape shape;
	private boolean in = true;
	
	/**
	 * Creates a new transfer shape event.
	 * @param transferTo the artboard to transfer into
	 * @param shape the shape to transfer
	 */
	public TransferShapeEvent(Artboard transferTo , Shape shape) {
		
		//marked as render event so shutDown() works correctly
		super(true , false);
		Objects.requireNonNull(transferTo);
		Objects.requireNonNull(shape);
		destination = transferTo.activeLayer();
		this.shape = shape;
		
	}

	@Override public void _do() {

		if(!in) destination.addShape(shape);
		in = true;
		
	}

	@Override public void undo() {

		if(in) destination.removeShape(shape);
		in = false;

	}

	@Override public void shutDown() {

		if(!in) shape.shutDown();
		
		
	}

	@Override public boolean isFreed() {

		return shape.isFreed();
		
	}

}
