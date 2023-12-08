package cs.csss.editor.event;

import org.python.core.PyObject;

import cs.csss.editor.EventScriptMeta;
import cs.csss.engine.Engine;

/**
 * Event that runs a python script.
 */
public class RunScriptEvent2 extends CSSSEvent {

	private PyObject doCode;
	private PyObject undoCode;

	public RunScriptEvent2(EventScriptMeta meta , PyObject object) {

		super(meta.isRenderEvent() , meta.isTransientEvent());
		doCode = object.__getattr__("_do");

		try {
			
			undoCode = object.__getattr__("undo");
			
		} catch(Exception e) {
			
			if(!meta.isTransientEvent()) throw new IllegalStateException(e);
			
		}
		
	}

	@Override public void _do() {
		
		if(Engine.isDebug()) try {
			 
			doCode.__call__();
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		} else doCode.__call__();
		
	}

	@Override public void undo() {
		
		if(Engine.isDebug()) try {
			 
			undoCode.__call__();
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		} else undoCode.__call__();
		
	}

}
