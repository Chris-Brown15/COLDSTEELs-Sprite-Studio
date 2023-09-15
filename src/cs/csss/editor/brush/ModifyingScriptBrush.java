package cs.csss.editor.brush;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.BrushScriptMeta;
import cs.csss.editor.Editor;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.project.Artboard;
import jep.python.PyObject;

/**
 * Brush for containing Python-implemented brushes that are also {@link cs.csss.editor.brush.CSSSModifyingBrush CSSSModifyingBrush}.
 */
@RenderThreadOnly public class ModifyingScriptBrush extends CSSSModifyingBrush implements PythonBrush {

	private ScriptBrushPythonData scriptBrush;

	/**
	 * Modifies {@code xIndex} and {@code yIndex} such that they locate the bottom left corner of a region. The third value of the array
	 * is a square size value.
	 * 
	 * @param xIndex — x index of a clicked pixel
 	 * @param yIndex — y index of a clicked pixel
 	 * @param artboardWidth — width of the artboard
 	 * @param artboardHeight — height of the artboard
	 * @return Array containing four values, {@code xIndex}, {@code yIndex}, width, and height. The indices point to the lower left pixel of
	 *  	   a region, and width and height are the number of pixels to extend this region.
	 */
	public int[] centerAroundRadius(int xIndex , int yIndex , int artboardWidth , int artboardHeight) {
		
		return super.centerAroundRadius(xIndex, yIndex, artboardWidth, artboardHeight);
		
	}
	
	/**
	 * Creates a script brush from the given metadata container. 
	 * 
	 * @param meta — metadata container
	 */
	public ModifyingScriptBrush(BrushScriptMeta meta) {
		
		super(meta.tooltip() , meta.isStateful());
				
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
