/**
 * 
 */
package cs.ext.misc.utils;

/**
 * Thrown when an object is already initialized and cannot be initialized again.
 */
public class AlreadyInitializedException extends RuntimeException {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 5348959227924447052L;

	public AlreadyInitializedException(String reason) {
	
		super(reason);
		
	}
	
}
