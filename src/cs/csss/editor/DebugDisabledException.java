package cs.csss.editor;

public class DebugDisabledException extends Exception {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 4548646702924380645L;

	public DebugDisabledException(Editor editor) {
		
		super("A Debug Method was invoked while the application is not in debug mode." , null , false , true);
		editor.exit();
		
	}

}