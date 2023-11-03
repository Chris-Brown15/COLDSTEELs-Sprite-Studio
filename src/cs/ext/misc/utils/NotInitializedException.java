/**
 * 
 */
package cs.ext.misc.utils;

/**
 * Notates an object was not initialized when some operation on it which requires it to be initialized was invoked.
 */
public class NotInitializedException extends RuntimeException {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 1339445616964462096L;

	public NotInitializedException(String message) {
		
		super(message);
		
	}
	
}
