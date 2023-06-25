package cs.csss.editor;

import static cs.core.utils.CSUtils.specify;

import java.nio.file.Files;
import java.nio.file.Paths;

import cs.coreext.python.CSJEP;

/**
 * This class is responsible for
 * 
 * @author Chris Brown
 *
 */
public class EditorScript {

	private final CSJEP interpreter;	
	
	protected EditorScript(CSJEP interpreter , String scriptAbsPath) {
		
		specify(Files.exists(Paths.get(scriptAbsPath)) , scriptAbsPath + " does not point to a file.");

		this.interpreter = interpreter;
		this.interpreter.run(scriptAbsPath);
		
	}
	
}
