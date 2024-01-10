/**
 * 
 */
package cs.csss.editor.event;

import cs.csss.editor.Editor;

/**
 * Event which toggles whether the current palette is being modified directly.
 */
public class TogglePaletteReferenceModeEvent extends CSSSEvent {

	private final Editor editor;
	
	/**
	 * Creates a new toggle palette modification mode event.
	 *   
	 * @param editor — the editor
	 */
	public TogglePaletteReferenceModeEvent(Editor editor) {
		
		super(false , false);
		this.editor = editor;
				
	}

	@Override public void _do() {

		editor.toggleDirectPaletteAccess();
		editor.artboardPaletteUI().toggleVisible();

	}

	@Override public void undo() {
		
		editor.toggleDirectPaletteAccess();
		editor.artboardPaletteUI().toggleVisible();
		
	}

}
