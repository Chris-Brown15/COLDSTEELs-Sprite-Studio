package cs.csss.editor.events;

import java.util.List;

import cs.coreext.python.CSJEP;
import cs.csss.editor.Editor;
import cs.csss.misc.files.CSFile;
import cs.csss.project.CSSSProject;

public class RunProjectScriptEvent extends CSSSEvent {

	private final CSFile file;
	private final CSSSProject project;
	private final Editor editor;
	private final List<String> arguments;
	
	public RunProjectScriptEvent(boolean isRenderEvent , CSFile file , CSSSProject project , Editor editor , List<String> arguments) {
		
		super(isRenderEvent);
		
		this.file = file;
		this.project = project;
		this.editor = editor;
		this.arguments = arguments;
		
	}

	@Override public void _do() {

		try(CSJEP interpreter = CSJEP.interpreter()) {
			
			interpreter.initializeCSPythonLibrary();		
			interpreter.run(file.getRealPath());
			if(arguments.size() > 0) interpreter.invoke("args", arguments);			
			interpreter.invoke("_do", project , editor);
			
		}
		
	}

	@Override public void undo() {

		try(CSJEP interpreter = CSJEP.interpreter()) {
			
			interpreter.initializeCSPythonLibrary();		
			interpreter.run(file.getRealPath());
			if(arguments.size() > 0) interpreter.invoke("args", arguments);			
			interpreter.invoke("undo", project , editor);
			
		}
		
	}

}
