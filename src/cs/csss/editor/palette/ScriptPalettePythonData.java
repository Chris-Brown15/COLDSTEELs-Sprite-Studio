/**
 * 
 */
package cs.csss.editor.palette;

import cs.core.utils.ShutDown;
import cs.csss.engine.ColorPixel;
import jep.python.PyCallable;
import jep.python.PyObject;

/**
 * Container for data for color palettes generated via Python scripts.
 */
class ScriptPalettePythonData implements ShutDown{

	private PyCallable setValueScale , generate , get;
	
	/**
	 * Creates a new container for palettes implemented in Python. 
	 * 
	 * @param source — Python object generated from loading a script
	 */
	public ScriptPalettePythonData(PyObject source) {

		setValueScale = (PyCallable) source.getAttr("setValueScale");
		generate = (PyCallable) source.getAttr("generate");
		get = (PyCallable) source.getAttr("get");
		
	}

	void setValueScale(int newScale) {
		
		setValueScale.call(newScale);
		
	}
	
	ColorPixel[] generate(ColorPixel source , int channels) {
		
		return (ColorPixel[]) generate.call(source, channels);				
		
	}

	ColorPixel[] get() {
		
		return (ColorPixel[]) get.call();
		
	}
	
	@Override public void shutDown() {

		if(isFreed()) return;
		
		setValueScale.close();
		generate.close();
		get.close();
				
		setValueScale = null;
		generate = null;
		get = null;
		
	}

	@Override public boolean isFreed() {

		return setValueScale == null && generate == null && get == null;
		
	}
	
}
