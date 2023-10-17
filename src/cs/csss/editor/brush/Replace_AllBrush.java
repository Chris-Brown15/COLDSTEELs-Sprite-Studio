package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.ModifyPaletteEvent;
import cs.csss.project.Artboard;
import cs.csss.project.IndexPixel;

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

		IndexPixel clickedPixel = artboard.getIndexPixelAtIndices(xIndex, yIndex);		
		return new ModifyPaletteEvent(artboard , clickedPixel.xIndex , clickedPixel.yIndex , editor.selectedColors());

	}

}