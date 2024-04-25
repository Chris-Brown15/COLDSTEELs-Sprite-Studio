package cs.csss.editor.event;

import java.util.Objects;

import cs.csss.editor.Editor;
import cs.csss.editor.line.Line;
import cs.csss.project.Artboard;

/**
 * Event for removing a line from an artboard and its associated layer.
 */
public class DeleteLineEvent extends CSSSEvent {

	private final Artboard owner;	
	private final Line line;
	private final Editor editor;
	
	/**
	 * Creates a new delete line event from the given parameters.
	 *  
	 * @param owner artboard owning {@code line}
	 * @param line a line to remove
	 * @param editor the editor
	 * @throws NullPointerException if any parameter is <code>null</code>.
	 */
	public DeleteLineEvent(Artboard owner , Line line , Editor editor) {

		super(true , false);
		this.editor = Objects.requireNonNull(editor);
		this.owner = Objects.requireNonNull(owner);
		this.line = Objects.requireNonNull(line);
				
	}

	@Override public void _do() {
		
		line.putModsInArtboard(owner);
		owner.removeLine(line);
		editor.activeLine(null);
		
	}

	@Override public void undo() {

		owner.activeLayer().addLine(line);
		line.reset(owner);
		editor.activeLine(line);
	}

}
