/**
 * 
 */
package cs.csss.editor.palette;

import cs.csss.engine.ColorPixel;
import jep.python.PyObject;

/**
 * Color palette whose generate method is defined in Python scripts.
 */
public class ScriptColorPalette extends ColorPalette {

	private ScriptPalettePythonData python;
	
	/**
	 * Creates a script color palette.
	 * 
	 * @param name — name of a color palette
	 * @param initialValueScale — initial value scale for the color palette
	 */
	public ScriptColorPalette(String name) {

		super(name);

	}

	/**
	 * Sets the Python function container this script color palette will use when its methods are called.
	 * 
	 * @param python — container for Python functions this object will invoke for its implementations of 
	 * {@link ColorPalette#setValueScale(int) setValueScale} and {@link ColorPalette#generate(ColorPixel, int) generate}.
	 */
	public void setScriptData(PyObject paletteObject) {
		
		this.python = new ScriptPalettePythonData(paletteObject);
		
	}
	
	@Override public void setValueScale(int valueScale) {

		python.setValueScale(valueScale);

	}

	@Override public ColorPixel[] generate(ColorPixel source, int channels) {

		return python.generate(source, channels);
		
	}

	@Override public ColorPixel[] get() {
		
		return python.get();
		
	}
	
}
