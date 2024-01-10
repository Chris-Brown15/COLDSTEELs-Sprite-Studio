package cs.csss.editor.event;

import cs.csss.editor.Editor;
import cs.csss.engine.Pixel;

/**
 * Sets the active color in the left hand side panel to some color.
 */
public class SetActiveColorEvent extends CSSSEvent {

	private final Pixel previous , newCurrent;
	private final Editor editor;
	
	/**
	 * Creates a set active color event.
	 * 
	 * @param editor — the edtior
	 * @param pixel — a new active color
	 */
	public SetActiveColorEvent(final Editor editor , Pixel pixel) {

		super(true , false);
		previous = editor.currentColor();
		newCurrent = pixel;
		this.editor = editor;

	}

	@Override public void _do() {

		editor.setSelectedColor2(newCurrent);
		
	}

	@Override public void undo() {
		
		editor.setSelectedColor2(previous);		
		
	}

}
