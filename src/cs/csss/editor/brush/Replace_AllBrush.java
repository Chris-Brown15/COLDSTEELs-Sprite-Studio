package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.ModifyPaletteEvent;
import cs.csss.editor.event.NOPEvent;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.Pixel;
import cs.csss.project.Artboard;

/**
 * Brush used to replace a color within a palette to the selected color in the left hand side panel.
 */
@RenderThreadOnly public class Replace_AllBrush extends CSSSBrush {

	/**
	 * Creates a new replace all brush.
	 */
	public Replace_AllBrush() {

		super("Replaces all pixels of the value of the clicked pixel on the current artboard with the active color." , false);

	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		LookupPixel clickedPixel = artboard.getIndexPixelAtIndices(xIndex, yIndex);		
		Pixel editorSelected = editor.selectedColorValues();
		if(editorSelected instanceof LookupPixel) return new NOPEvent();
		return new ModifyPaletteEvent(artboard , clickedPixel.unsignedLookupX() , clickedPixel.unsignedLookupY() , (ColorPixel)editorSelected);

	}

}