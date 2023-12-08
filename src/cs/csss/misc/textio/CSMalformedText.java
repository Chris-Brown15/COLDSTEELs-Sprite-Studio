/**
 * 
 */
package cs.csss.misc.textio;

/**
 * Exception type notating that a text file is malformed for reading given the {@code get} method invoked when this exception type is thrown.
 */
public class CSMalformedText extends Exception {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 3107239079900321131L;

	CSMalformedText(String message) {
		
		super(message);
		
	}
	
}
