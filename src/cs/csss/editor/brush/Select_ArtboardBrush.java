/**
 * 
 */
package cs.csss.editor.brush;

import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.NOPEvent;
import cs.csss.project.Artboard;

/**
 * Brush which allows for selecting artboards to make them current.
 */
public class Select_ArtboardBrush extends CSSSBrush {

	/**
	 * Creates a select artboard brush.
	 */
	public Select_ArtboardBrush() {
		
		super("Used to select an artboard as the active artboard without modifying its image." , false);

	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		return new NOPEvent();
		
	}

	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return editor.currentArtboard() != artboard;
		
	}
	
}
