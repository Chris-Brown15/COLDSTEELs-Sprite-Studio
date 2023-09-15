package cs.csss.editor.event;

import cs.core.utils.ShutDown;
import jep.JepException;
import jep.python.PyCallable;
import jep.python.PyObject;

/**
 * Event that runs a python script.
 */
public class RunScriptEvent extends CSSSEvent implements ShutDown {

	private volatile PyObject pythonEvent;
	
	/**
	 * Creates a python script event.
	 * 
	 * @param isRenderEvent — {@code true} if this event is in the render thread
	 * @param isTransientEvent — {@code true} if this is a transient event
	 * @param pythonEvent — Python object containing the code for the event
	 */
	public RunScriptEvent(boolean isRenderEvent , boolean isTransientEvent , PyObject pythonEvent) {

		super(isRenderEvent , isTransientEvent);
		this.pythonEvent = pythonEvent;
		
	}

	@Override public void _do() {

		try(PyCallable doFunction = (PyCallable) pythonEvent.getAttr("_do")){
		
			doFunction.call();
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void undo() {

		try(PyCallable doFunction = (PyCallable) pythonEvent.getAttr("undo")){
		
			doFunction.call();
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override public void shutDown() {
		
		if(isFreed()) return;
		
		try(PyCallable shutDownFunction = (PyCallable) pythonEvent.getAttr("shutDown")){

			shutDownFunction.call();
			
		} catch(JepException e) {} finally {
			
			pythonEvent.close();
			pythonEvent = null;
		
		}
		
	}

	@Override public boolean isFreed() {

		return pythonEvent == null;
		
	}

}
