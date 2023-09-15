package cs.csss.editor.brush;

import java.util.Objects;

import cs.core.utils.ShutDown;
import cs.csss.editor.Editor;
import cs.csss.editor.NotEventTypeException;
import cs.csss.editor.event.CSSSEvent;
import cs.csss.editor.event.NOPEvent;
import cs.csss.editor.event.RunScriptEvent;
import cs.csss.engine.Engine;
import cs.csss.project.Artboard;
import jep.JepException;
import jep.python.PyCallable;
import jep.python.PyObject;

/**
 * Models an abstract brush who relies on Python code work. This class exists to avoid repeating oneself on the three different types of 
 * script brushes; they all rely on this.
 */
class ScriptBrushPythonData implements ShutDown {

	private PyObject brush;
	private PyCallable 
		pythonUse,
		pythonCanUse,
		pythonUpdate;
	
	private final boolean 
		isRenderEvent ,
		isTransientEvent;
	
	ScriptBrushPythonData(PyObject brushType , boolean stateful , boolean isRenderEvent , boolean isTransientEvent) {
		
		if(this.brush != null) throw new IllegalStateException("PyObject cannot be set more than once.");
		
		this.isRenderEvent = isRenderEvent;
		this.isTransientEvent = isTransientEvent;
			
		this.brush = brushType;
		pythonUse = (PyCallable) brushType.getAttr("use");
		pythonCanUse = (PyCallable) brushType.getAttr("canUse");
		if(stateful) try {
			
			pythonUpdate = (PyCallable) brushType.getAttr("update");
			
		} catch(JepException e) {
			
			if(stateful) throw new IllegalStateException("The update() function must be implemented for a brush who is stateful." , e);
			
		}
		
	}
	
	CSSSEvent use(Artboard artboard , Editor editor , int cursorX , int cursorY) {

		//if in debug mode, try to catch errors and handle them
		if(Engine.isDebug()) try {
			
			return useInternal(artboard , editor , cursorX , cursorY);
			
		} catch (Exception e) {

			e.printStackTrace();
			return new NOPEvent();
					
		} else return useInternal(artboard , editor , cursorX , cursorY);
		
	}

	boolean canUse(Artboard artboard , Editor editor , int cursorX , int cursorY) {
		
		return (boolean)pythonCanUse.call(artboard , editor,  cursorX , cursorY);
		
	}
	
	void update(Artboard artboard , Editor editor) {
		
		if(pythonUpdate != null) pythonUpdate.call(artboard , editor);
		
	}
	
	private CSSSEvent useInternal(Artboard artboard , Editor editor , int cursorX , int cursorY) {
		
		Object result = pythonUse.call(artboard , editor,  cursorX , cursorY);
		Objects.requireNonNull(result);
		if(!(result instanceof PyObject)) throw new NotEventTypeException(result); 
		PyObject pythonEvent = (PyObject) result;		
		RunScriptEvent event = new RunScriptEvent(isRenderEvent, isTransientEvent , pythonEvent);
		return event;
	
	}
	
	@Override public void shutDown() {

		if(!isFreed());
		
		//wrap in try because there may not be a shutdown method
		try(PyCallable shutDown = (PyCallable) brush.getAttr("shutDown")) {
			
			shutDown.call();
			
		} catch (JepException e) {}
		
		pythonUse.close();
		pythonCanUse.close();
		if(pythonUpdate != null) pythonUpdate.close();
		brush.close();
		brush = null;		
		
	}

	@Override public boolean isFreed() {

		return brush == null;
		
	}

}
