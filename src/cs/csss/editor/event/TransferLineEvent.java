/**
 * 
 */
package cs.csss.editor.event;

import java.util.Objects;

import cs.csss.editor.line.Line;
import cs.csss.project.Artboard;

/**
 * Event used to transfer a line to solely be owned by the given artboard.
 */
public class TransferLineEvent extends CSSSEvent {

	private final Artboard artboard;
	private final Line line;
	private boolean present = true;
	
	/**
	 * Creates a new transfer line event from the given parameters
	 * 
	 * @param artboard artboard to transfer line to
	 * @param line line to transfer
	 * @throws NullPointerException if any parameter is <code>null</code>.
	 */
	public TransferLineEvent(Artboard artboard , Line line) {

		super(true , false);
		this.artboard = Objects.requireNonNull(artboard);
		this.line = Objects.requireNonNull(line);
		present = artboard.layerOwningLine(line) != null;
		
	}

	@Override public void _do() {

		if(!present) {
			
			artboard.addLine(line);
			line.reset(artboard);
			
		}

		present = true;
		
	}

	@Override public void undo() {

		if(present) {
			
			artboard.removeLine(line);
			line.putModsInArtboard(artboard);
			
		}
		
		present = false;

	}

}
