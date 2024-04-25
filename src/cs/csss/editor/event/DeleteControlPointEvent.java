/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;

import org.joml.Vector2f;

import cs.csss.editor.line.BezierLine;
import cs.csss.project.Artboard;

/**
 * Event for deleting a new control point from a bezier line.
 */
public class DeleteControlPointEvent extends CSSSEvent {

	private final Artboard artboard;
	private final BezierLine line;
	private final int index;
	private final Vector2f point;
	
	/**
	 * Creates a new delete control point event.
	 * 
	 * @param artboard artboard {@code line} belongs to
	 * @param line a line whose control point is to be removed
	 * @param index index of the control point to remove
	 * @throws NullPointerException if either {@code artboard} or {@code line} is <code>null</code>.
	 * @throws IndexOutOfBoundsException if {@code index} is invalid as a control point.
	 */
	public DeleteControlPointEvent(Artboard artboard , BezierLine line , int index) {
		
		super(true , false);
		this.artboard = Objects.requireNonNull(artboard);
		this.line = Objects.requireNonNull(line);
		this.index = index;		
		point = line.controlPoint(index);		
		
	}

	@Override public void _do() {

		line.removePoint(index);
		line.reset(artboard);

	}

	@Override public void undo() {
	
		line.addPoint(artboard , index , point);
		line.reset(artboard);
		
	}

}
