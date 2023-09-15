package cs.csss.editor.event;

import cs.csss.editor.Editor;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette.PalettePixel;

/**
 * Sets the active color in the left hand side panel to some color.
 */
public class SetActiveColorEvent extends CSSSEvent {

	private final PalettePixel 
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
	public SetActiveColorEvent(final Editor editor , Artboard artboard , final PalettePixel pixel) {

		super(true , false);

		this.pixel = pixel;
		this.previous = editor.selectedColors(artboard);
		this.editor = editor;
		
	}

	@Override public void _do() {

		editor.setSelectedColor(pixel);
		
	}

	@Override public void undo() {
		
		editor.setSelectedColor(previous);
		
	}

}
