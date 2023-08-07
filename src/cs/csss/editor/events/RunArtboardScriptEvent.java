package cs.csss.editor.events;

import java.util.List;

import cs.coreext.python.CSJEP;
import cs.csss.editor.Editor;
import cs.csss.misc.files.CSFile;
import cs.csss.project.Artboard;
import jep.JepException;

public class RunArtboardScriptEvent extends CSSSEvent {

	private final Artboard artboard;
	private final Editor editor;
	private final CSFile script;

	private final List<String> arguments;
	
	public RunArtboardScriptEvent(boolean isRenderEvent , CSFile script , Artboard artboard , Editor editor , List<String> args) {

		super(isRenderEvent , false);
		
		this.artboard = artboard;
		this.editor = editor;
		this.script = script;
		
		arguments = args;
		
	}

	@Override public void _do() {
		
		try(CSJEP python = CSJEP.interpreter()){
			
			python.initializeCSPythonLibrary();			
			python.run(script.getRealPath());
			if(arguments.size() > 0) python.invoke("args", arguments);			
			python.invoke("_do", artboard , editor);
		
		} catch(JepException e) {
			
			e.printStackTrace();
			
		}

	}

	@Override public void undo() {

		try (CSJEP python = CSJEP.interpreter()) {
			
			python.initializeCSPythonLibrary();
			python.run(script.getRealPath());
			if(arguments.size() > 0) python.invoke("args", arguments);
			python.invoke("undo" , artboard , editor);

		} catch(JepException e) {
			
			e.printStackTrace();
			
		}		
		
	}

}
