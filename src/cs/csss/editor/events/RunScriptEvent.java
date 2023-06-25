package cs.csss.editor.events;

import cs.coreext.python.CSJEP;
import cs.csss.artboard.Artboard;
import cs.csss.editor.Editor;
import cs.csss.misc.files.CSFile;
import jep.JepException;

public class RunScriptEvent extends CSSSEvent {

	private final Artboard artboard;
	private final Editor editor;
	private final CSFile script;
	
	private boolean initialized = false;
	
	public RunScriptEvent(boolean isRenderEvent , CSFile script , Artboard artboard , Editor editor) {

		super(isRenderEvent);
		
		this.artboard = artboard;
		this.editor = editor;
		this.script = script;
		
	}

	@Override public void _do() {
		
		CSJEP python = CSJEP.interpreter();
		
		if(!initialized) {
		
			initialized = true;
			python.run(script.getRealPath());		
			
		}
		
		try {
			
			python.invoke("_do", artboard , editor);

		} catch(JepException e) {
			
			e.printStackTrace();
			
		}

	}

	@Override public void undo() {

		try {
			
			CSJEP python = CSJEP.interpreter();	
			python.invoke("undo" , artboard , editor);

		} catch(JepException e) {
			
			e.printStackTrace();
			
		}		
		
	}

}
