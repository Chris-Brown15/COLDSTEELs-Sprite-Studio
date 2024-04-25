/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;

import org.joml.Vector2f;

import cs.csss.editor.line.BezierLine;
import cs.csss.project.Artboard;

/**
 * Event for adding a new control point to a bezier line.
 */
public class NewControlPointEvent extends CSSSEvent {

	private final Artboard artboard;
	private final BezierLine line;
	private final int x , y;
	private Vector2f vector;
	
	/**
	 * Creates a new new control point event.
	 * 
	 * @param artboard artboard {@code line} belongs to
	 * @param line line to add a control point to 
	 * @param x x axis artboard coordinate for the new control point 
	 * @param y y axis artboard coordinate for the new control point
	 * @throws NullPointerException if {@code artboard} or {@code line} is <code>null</code>.
	 */
	public NewControlPointEvent(Artboard artboard , BezierLine line , int x , int y) {

		super(true , false);
		this.artboard = Objects.requireNonNull(artboard);
		this.line = Objects.requireNonNull(line);
		this.x = x;
		this.y = y;

	}

	@Override public void _do() {

		if(vector == null) line.controlPoint(artboard, x, y);
		else line.addPoint(artboard, vector);
		
		line.reset(artboard);
		
	}

	@Override public void undo() {

		vector = line.removePoint(line.numberControlPoints() - 1);
		line.reset(artboard);
		
	}

}
