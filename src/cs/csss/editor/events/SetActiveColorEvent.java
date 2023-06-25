package cs.csss.editor.events;

import cs.csss.artboard.Artboard;
import cs.csss.artboard.ArtboardPalette.PalettePixel;
import cs.csss.editor.Editor;

public class SetActiveColorEvent extends CSSSEvent {

	private final PalettePixel 
		pixel ,
		previous
	;
	
	private final Editor editor;
	
	public SetActiveColorEvent(final Editor editor , Artboard artboard , final PalettePixel pixel) {

		super(true);

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
