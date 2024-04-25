/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;

import cs.csss.editor.line.BezierLine;
import cs.csss.project.Artboard;

/**
 * Event used to reorder control points on a bezier line.
 */
public class ReorderControlPointEvent extends CSSSEvent {

	private final Artboard artboard;
	private final BezierLine line;
	private final int originalIndex , moveToIndex;
	
	/**
	 * Creates a new reorder control point event.
	 * 
	 * @param artboard artboard {@code line} belongs to
	 * @param line bezier line whose control point is being reordered
	 * @param originalIndex index of the control point to reorder
	 * @param moveToIndex index to move the reordered control point
	 * @throws NullPointerException if {@code artboard} or {@code line} is <code>null</code>.
	 * @throws IndexOutOfBoundsException if either {@code originalIndex} or {@code moveToIndex} is out of bounds as a control point index.
	 */
	public ReorderControlPointEvent(Artboard artboard , BezierLine line , int originalIndex , int moveToIndex) {

		super(true , false);
		this.artboard = Objects.requireNonNull(artboard);
		this.line = Objects.requireNonNull(line);
		
		assert originalIndex != moveToIndex;
		int numberControlPoints = line.numberControlPoints();
		Objects.checkIndex(originalIndex, numberControlPoints);
		Objects.checkIndex(moveToIndex, numberControlPoints);
		
		this.originalIndex = originalIndex;
		this.moveToIndex = moveToIndex;
		
	}

	@Override public void _do() {

		line.movePoint(originalIndex , moveToIndex);
		line.reset(artboard);
		
	}

	@Override public void undo() {

		line.movePoint(moveToIndex, originalIndex);
		line.reset(artboard);

	}

}
