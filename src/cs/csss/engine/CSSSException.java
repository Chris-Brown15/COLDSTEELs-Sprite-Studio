package cs.csss.engine;

import cs.csss.project.CSSSProject;

/**
 * Exception type using other {@link java.lang.Throwable Throwable}s which handles a few emergency tasks upon a crash.
 */
public class CSSSException extends RuntimeException {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 231054898968005590L;

	private static volatile Engine theEngine;
	
	/*
	 * TODO: Change this line to: 
	 private static boolean logTrace = Engine.isDebug(); 
	 * Once we have left beta.  
	 */
	private static boolean logTrace = true;
	
	private static boolean saveProject = true;
	
	static void registerTheEngine(Engine theEngine) {
		
		CSSSException.theEngine = theEngine;
		
	}

	/**
	 * Creates a new exception from the given message and throwable.
	 * 
	 * @param message — error message
	 * @param cause — cause for the exception
	 */
	public CSSSException(String message, Throwable cause) {
		
		super(message, cause);
		if(logTrace) {
			
			Logging.syserrln(message);
			logStackTrace(getStackTrace());
			
		}
		
		emergencySaveProject();
	
	}
	
	/**
	 * Creates a new exception from the given throwable.
	 * 
	 * @param cause — cause for the exception
	 */
	public CSSSException(Throwable cause) {
		
		this(cause.getClass().getSimpleName() , cause);
	
	}
	
	/**
	 * <b>Will throw errors; use {@link CSSSException#CSSSException(Throwable) CSSSException(Throwable)} or 
	 * {@link CSSSException#CSSSException(String, Throwable)} instead. </b> 
	 */
	public CSSSException() {

		throw new UnsupportedOperationException("Use other constructors that take throwable or throwable and message");
		
	}

	/**
	 * <b>Will throw errors; use {@link CSSSException#CSSSException(Throwable) CSSSException(Throwable)} or 
	 * {@link CSSSException#CSSSException(String, Throwable)} instead. </b> 
	 */
	public CSSSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {

		throw new UnsupportedOperationException("Use other constructors that take throwable or throwable and message");
		
	}

	/**
	 * <b>Will throw errors; use {@link CSSSException#CSSSException(Throwable) CSSSException(Throwable)} or 
	 * {@link CSSSException#CSSSException(String, Throwable)} instead. </b> 
	 */
	public CSSSException(String message) {

		throw new UnsupportedOperationException("Use other constructors that take throwable or throwable and message");
		
	}

	private void emergencySaveProject() {
		
		if(!saveProject) return;
		
		CSSSProject currentProject = theEngine.currentProject(); 
		if(currentProject != null) theEngine.saveProject(currentProject.name() + " crash save"); 
		
	}

	private void logStackTrace(StackTraceElement[] elements) {
		
		for(int i = 0 ; i < elements.length ; i++) Logging.syserrln("\t" + elements[i].toString());
		
	}
	
}
