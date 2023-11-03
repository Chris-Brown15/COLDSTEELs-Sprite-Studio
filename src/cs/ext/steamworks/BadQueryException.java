/**
 * 
 */
package cs.ext.steamworks;

/**
 * 
 */
public class BadQueryException extends Exception {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = -7031288263247012916L;

	/**
	 * Creates a blank bad query exception.
	 */
	public BadQueryException() {}

	/**
	 * Creates a bad query exception. 
	 *  
	 * @param message — error message
	 */
	public BadQueryException(String message) {

		super(message);

	}

	/**
	 * Creates a bad query exception. 
	 *  
	 * @param cause — external cause for the error
	 */ 
	public BadQueryException(Throwable cause) {

		super(cause);

	}

	/**
	 * Creates a bad query exception. 
	 *  
	 * @param message — error message
	 * @param cause — external cause for the error
	 */
	public BadQueryException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * Creates a bad query exception. 
	 *  
	 * @param message — error message
	 * @param cause — external cause for the error
	 * @param enableSuppression — whether to enable suppression for this exception
	 * @param writableStackTrace — whether the stack trace should be writable
	 */
	public BadQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	
		super(message, cause, enableSuppression, writableStackTrace);
	
	}

}
