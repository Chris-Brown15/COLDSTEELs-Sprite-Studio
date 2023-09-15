package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.BrushScriptMeta;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.project.Artboard;
import jep.python.PyObject;

/**
 * Container brush for Python-implemented brushes.
 */
@RenderThreadOnly public class ScriptBrush extends CSSSBrush implements PythonBrush {

	private ScriptBrushPythonData scriptedBrush;
	
	/**
	 * Creates a new script brush from the given metadata.
	 * 
	 * @param meta — metadata used to create this brush
	 */
	public ScriptBrush(BrushScriptMeta meta) {
		
		super(meta.tooltip() , meta.isStateful());
		
	}
	
	@Override public void setScriptBrush(PyObject brush , BrushScriptMeta meta) {
		
		PythonBrush.standardCheck(scriptedBrush);
		scriptedBrush = new ScriptBrushPythonData(brush , stateful , meta.isRenderEvent() , meta.isTransientEvent());		
		
	}
	
	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return scriptedBrush.use(artboard, editor, xIndex, yIndex);
		
	}

	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return scriptedBrush.canUse(artboard, editor, xIndex, yIndex);
		
	}
	
	@Override public void update(Artboard artboard, Editor editor) {
		
		scriptedBrush.update(artboard , editor);
		
	}
	
	@Override public void shutDown() {
		
		scriptedBrush.shutDown();
		
	}

	@Override public boolean isFreed() {

		return scriptedBrush.isFreed();
		
	}

}
