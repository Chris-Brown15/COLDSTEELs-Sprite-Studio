package cs.csss.editor.events;

import cs.csss.editor.Editor;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette.PalettePixel;

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
