package cs.csss.editor.brush;

import cs.coreext.python.CSJEP;
import cs.csss.editor.Editor;
import cs.csss.editor.events.CSSSEvent;
import cs.csss.project.Artboard;

public class ScriptBrush extends CSSSModifyingBrush {

	private CSJEP interpreter = CSJEP.interpreter();	
	
	public ScriptBrush() {
		
		super("Invokes a script of the user's choice when used.");
		
	}
	
	public void setUseScript(String script) { 
		
		interpreter.run(script);
		
	}	
	
	@Override public CSSSEvent use(Artboard artboard, Editor editor, int xIndex, int yIndex) {

		try {
			
			return (CSSSEvent) interpreter.invoke("onUse", artboard , editor , xIndex , yIndex);
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		}		
		
		return null;
		
	}
	
	@Override public boolean canUse(Artboard artboard, Editor editor, int xIndex, int yIndex) {
		
		return true;
		
	}

}
