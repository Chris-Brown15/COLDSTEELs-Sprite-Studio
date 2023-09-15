package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.BrushScriptMeta;
import cs.csss.editor.Editor;
import cs.csss.editor.SelectionAreaBounder;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.project.Artboard;
import jep.python.PyObject;

/**
 * Container brush for Python-implemented selecting brushes.
 */
@RenderThreadOnly public class SelectingScriptBrush extends CSSSSelectingBrush implements PythonBrush {

	private ScriptBrushPythonData scriptBrush;

	/**
	 * Bounder for the selection area associated with this brush.
	 */
	public final SelectionAreaBounder bounder;
	
	/**
	 * Creates a new selecting brush from the given metadata.
	 * 
	 * @param meta — metadata for the given brush
	 */
	public SelectingScriptBrush(BrushScriptMeta meta) {

		super(meta.tooltip());
		this.bounder = selectionBounder;

	}

	/**
	 * Creates a new render for the selected region.
	 * 
	 * @param current — the current artboard
	 * @param editor — the editor
	 * @param cursor — int array containing the cursor's positions
	 */
	public void newRender(Artboard current , Editor editor , int[] cursor) {
		
		super.newRender(current, editor, cursor);
		
	}

	/**
	 * Creates a new render for the selected region.
	 * 
	 * @param current — the current artboard
	 * @param editor — the editor
	 */
	public void newRender(Artboard current , Editor editor) {
		
		float[] midpoint = selectionBounder.midpoint();
		int[] asInts = {(int)midpoint[0] , (int)midpoint[1]};
		newRender(current , editor , asInts);		
		
	}

	@Override public void setScriptBrush(PyObject brushCode, BrushScriptMeta meta) {

		PythonBrush.standardCheck(scriptBrush);
		scriptBrush = new ScriptBrushPythonData(brushCode , stateful , meta.isRenderEvent() , meta.isTransientEvent());
		
	}

	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		return scriptBrush.use(artboard, editor, xIndex, yIndex);
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return scriptBrush.canUse(artboard , editor , xIndex , yIndex);
		
	}
	
	@Override public void update(Artboard artboard, Editor editor) {
		
		scriptBrush.update(artboard, editor);
		
	}

	@Override public void shutDown() {

		scriptBrush.shutDown();
		
	}

	@Override public boolean isFreed() {

		return scriptBrush.isFreed();
		
	}

}
