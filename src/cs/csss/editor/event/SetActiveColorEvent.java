package cs.csss.editor.event;

import cs.csss.editor.Editor;
import cs.csss.engine.ColorPixel;
import cs.csss.project.Artboard;

/**
 * Sets the active color in the left hand side panel to some color.
 */
public class SetActiveColorEvent extends CSSSEvent {

	private final ColorPixel 
		pixel ,
		previous;
	
	private final Editor editor;
	
	/**
	 * Creates a set active color event.
	 * 
	 * @param editor — the edtior
	 * @param artboard — an artboard
	 * @param pixel — a new active color
	 */
	public SetActiveColorEvent(final Editor editor , Artboard artboard , final ColorPixel pixel) {

		super(true , false);

		this.pixel = pixel;
		this.previous = editor.selectedColors();
		this.editor = editor;
		
	}

	@Override public void _do() {

		editor.setLHSSelectedColor(pixel);
		
	}

	@Override public void undo() {
		
		editor.setLHSSelectedColor(previous);
		
	}

}
