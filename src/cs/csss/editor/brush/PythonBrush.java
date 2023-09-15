package cs.csss.editor.brush;

import cs.core.utils.ShutDown;
import cs.csss.editor.BrushScriptMeta;
import jep.python.PyObject;

/**
 * Defines operations for brushes that use python for their implementation.
 */
public interface PythonBrush extends ShutDown {

	/**
	 * Called before assigning a script brush to ensure the operation is valid. An instance of {@code ScriptBrushPythonData} is only 
	 * assignable if it is null. I.e., it cannot be set more than once.
	 * 
	 * @param data — an instance of {@code scriptBrushPythonData}
	 * @throws IllegalStateException if {@code data != null}.
	 */
	static void standardCheck(ScriptBrushPythonData data) throws IllegalStateException {
		
		if(data != null) throw new IllegalStateException("The script for a brush cannot be set more than once.");
		
	}
	
	/**
	 * Defines a method who sets the implementation's instance of {@code ScriptBrushPythonData}.
	 * 
	 * @param brushCode — Python object containing methods for implementing a brush
	 * @param meta — metadata object for the script used for the brush 
	 * @throws IllegalStateException if this operation is called more than one time.
	 */
	void setScriptBrush(PyObject brushCode , BrushScriptMeta meta) throws IllegalStateException;
	
}
