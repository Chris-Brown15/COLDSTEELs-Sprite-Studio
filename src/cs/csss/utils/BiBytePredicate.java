package cs.csss.utils;

/**
 * Takes two bytes and returns a boolean.
 */
public interface BiBytePredicate {

	/**
	 * Takes two bytes and returns the result of a test.
	 * 
	 * @param byte1 — a byte to use for a test
	 * @param byte2 — a byte to use for a test
	 * @return {@code true} or {@code false} depending upon the result of the test.
	 */
	public boolean test(byte byte1 , byte byte2);
	
}
