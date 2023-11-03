package cs.csss.editor.event;

import cs.csss.editor.Editor;
import cs.csss.engine.ColorPixel;

/**
 * Sets the active color in the left hand side panel to some color.
 */
public class SetActiveColorEvent extends CSSSEvent {

	private final byte r , g , b , a;
	private final ColorPixel previous;
	private final Editor editor;
	
	/**
	 * Creates a set active color event.
	 * 
	 * @param editor — the edtior
	 * @param pixel — a new active color
	 */
	public SetActiveColorEvent(final Editor editor , final ColorPixel pixel) {

		this(editor , pixel.r(), pixel.g(), pixel.b(), pixel.a());

	}

	/**
	 * Creates a new set active color event.
	 * 
	 * @param editor — the edtior
	 * @param r — red channel of the new color
	 * @param g — green channel of the new color
	 * @param b — blue channel of the new color
	 * @param a — alpha channel of the new color
	 */
	public SetActiveColorEvent(Editor editor , byte r , byte g, byte b , byte a) {

		super(true , false);

		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.previous = editor.selectedColors();
		this.editor = editor;
		
		
	}
	
	@Override public void _do() {

		editor.setSelectedColor(r , g , b , a);
		editor.setLHSSelectedColor(r , g , b , a);
		
	}

	@Override public void undo() {
		
		editor.setSelectedColor(previous);
		editor.setLHSSelectedColor(previous);
		
	}

}
