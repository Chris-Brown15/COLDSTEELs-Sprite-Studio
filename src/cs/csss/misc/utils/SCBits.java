package cs.csss.misc.utils;

/**
 * Backported from STEEL Core.
 * 
 * Contains helper methods for bit operations on the various integral types in Java.
 */
public class SCBits {
  
	/**
	 * Performs a bitwise AND operation and checks for the result to match {@code comparison2}.
	 * 
	 * @param operand an operand
	 * @param mask a mask
	 * @return {@code true} if {@code comparision1 & comparision2 == comparison2}.
	 */
	public static boolean has(byte operand , byte mask) {
		
		return (operand & mask) == mask;
		
	}
	
	/**
	 * Toggles the bits of the {@code operand} matching {@code bits} set bits. 
	 * 
	 * <p>
	 * 	Any set bits in {@code bits} mark bits to toggle in {@code operand}, if for example
	 * 	{@code bits == 0b001001} and {@code operand == 111111}, the result of this method is {@code 110110}
	 * </p>
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the toggle operation.
	 */
	public static byte toggle(byte operand , byte bits) {
		
		if((operand & bits) == bits) return unset(operand , bits);
		return set(operand , bits);
		
	}
	
	/**
	 * Sets any bits in {@code operand} which are set in {@code bits}. Equivalent to {@code operand |= bits}. 
	 * 
	 * @param operand an operand 
	 * @param bits a mask
	 * @return Result of the set operation.
	 */
	public static byte set(byte operand , byte bits) {
		
		return operand |= bits;
		
	}
	
	/**
	 * Unsets any bits in {@code operand} that are set in {@code bits}.
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the unset operation.
	 */
	public static byte unset(byte operand , byte bits) {
		
		return operand &= ~bits;
		
	}
	
	/**
	 * Returns whether the given value is odd or even by ANDing the value by {@code 1}.
	 * 
	 * @param query a value whose parity is being checked
	 * @return {@code true} if {@code query} is odd.
	 */
	public static boolean odd(byte query) {
		
		return (query & 1) == 1;
		
	}

	/**
	 * Performs a bitwise AND operation and checks for the result to match {@code comparison2}.
	 * 
	 * @param operand an operand
	 * @param mask a mask
	 * @return {@code true} if {@code comparision1 & comparision2 == comparison2}.
	 */
	public static boolean has(short operand , short mask) {
		
		return (operand & mask) == mask;
		
	}
	
	/**
	 * Toggles the bits of the {@code operand} matching {@code bits} set bits. 
	 * 
	 * <p>
	 * 	Any set bits in {@code bits} mark bits to toggle in {@code operand}, if for example
	 * 	{@code bits == 0b001001} and {@code operand == 111111}, the result of this method is {@code 110110}
	 * </p>
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the toggle operation.
	 */
	public static short toggle(short operand , short bits) {
		
		if((operand & bits) == bits) return unset(operand , bits);
		return set(operand , bits);
		
	}
	
	/**
	 * Sets any bits in {@code operand} which are set in {@code bits}. Equivalent to {@code operand |= bits}. 
	 * 
	 * @param operand an operand 
	 * @param bits a mask
	 * @return Result of the set operation.
	 */
	public static short set(short operand , short bits) {
		
		return operand |= bits;
		
	}
	
	/**
	 * Unsets any bits in {@code operand} that are set in {@code bits}.
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the unset operation.
	 */
	public static short unset(short operand , short bits) {
		
		return operand &= ~bits;
		
	}
	
	/**
	 * Returns whether the given value is odd or even by ANDing the value by {@code 1}.
	 * 
	 * @param query a value whose parity is being checked
	 * @return {@code true} if {@code query} is odd.
	 */
	public static boolean odd(short query) {
		
		return (query & 1) == 1;
		
	}
	
	/**
	 * Performs a bitwise AND operation and checks for the result to match {@code mask}.
	 * 
	 * @param operand an operand
	 * @param mask a mask
	 * @return {@code true} if {@code comparision1 & comparision2 == comparison2}.
	 */
	public static boolean has(int operand , int mask) {
		
		int AND = operand & mask;
		return AND == mask;
		
	}
	
	/**
	 * Toggles the bits of the {@code operand} matching {@code bits} set bits. 
	 * 
	 * <p>
	 * 	Any set bits in {@code bits} mark bits to toggle in {@code operand}, if for example
	 * 	{@code bits == 0b001001} and {@code operand == 111111}, the result of this method is {@code 110110}
	 * </p>
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the toggle operation.
	 */
	public static int toggle(int operand , int bits) {
		
		if((operand & bits) == bits) return unset(operand , bits);
		return set(operand , bits);
		
	}
	
	/**
	 * Sets any bits in {@code operand} which are set in {@code bits}. Equivalent to {@code operand |= bits}. 
	 * 
	 * @param operand an operand 
	 * @param bits a mask
	 * @return Result of the set operation.
	 */
	public static int set(int operand , int bits) {
		
		return operand |= bits;
		
	}
	
	/**
	 * Unsets any bits in {@code operand} that are set in {@code bits}.
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the unset operation.
	 */
	public static int unset(int operand , int bits) {
		
		return operand &= ~bits;
		
	}
	
	/**
	 * Returns whether the given value is odd or even by ANDing the value by {@code 1}.
	 * 
	 * @param query a value whose parity is being checked
	 * @return {@code true} if {@code query} is odd.
	 */
	public static boolean odd(int query) {
		
		return (query & 1) == 1;
		
	}

	/**
	 * Performs a bitwise AND operation and checks for the result to match {@code comparison2}.
	 * 
	 * @param operand an operand
	 * @param mask a mask
	 * @return {@code true} if {@code comparision1 & comparision2 == comparison2}.
	 */
	public static boolean has(long operand , long mask) {
		
		return (operand & mask) == mask;
		
	}
	
	/**
	 * Toggles the bits of the {@code operand} matching {@code bits} set bits. 
	 * 
	 * <p>
	 * 	Any set bits in {@code bits} mark bits to toggle in {@code operand}, if for example
	 * 	{@code bits == 0b001001} and {@code operand == 111111}, the result of this method is {@code 110110}
	 * </p>
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the toggle operation.
	 */
	public static long toggle(long operand , long bits) {
		
		if((operand & bits) == bits) return unset(operand , bits);
		return set(operand , bits);
		
	}
	
	/**
	 * Sets any bits in {@code operand} which are set in {@code bits}. Equivalent to {@code operand |= bits}. 
	 * 
	 * @param operand an operand 
	 * @param bits a mask
	 * @return Result of the set operation.
	 */
	public static long set(long operand , long bits) {
		
		return operand |= bits;
		
	}
	
	/**
	 * Unsets any bits in {@code operand} that are set in {@code bits}.
	 * 
	 * @param operand an operand
	 * @param bits a mask
	 * @return Result of the unset operation.
	 */
	public static long unset(long operand , long bits) {
		
		return operand &= ~bits;
		
	}
	
	/**
	 * Returns whether the given value is odd or even by ANDing the value by {@code 1}.
	 * 
	 * @param query a value whose parity is being checked
	 * @return {@code true} if {@code query} is odd.
	 */
	public static boolean odd(long query) {
		
		return (query & 1) == 1;
		
	}
	
	/**
	 * Byte version of {@link SCBits#hasAny(int, int) hasAny(int , int)}. Returns whether the given operand has at least one bit in
	 * common with {@code mask}.
	 * 
	 * @param operand a number
	 * @param mask a mask
	 * @return {@code true} if {@code operand} and {@code mask} have at least one bit in common.
	 */
	public static boolean hasAny(byte operand , byte mask) {
		
		return (operand * mask) != 0;
		
	}
	
	/**
	 * Short version of {@link SCBits#hasAny(int, int) hasAny(int , int)}. Returns whether the given operand has at least one bit in
	 * common with {@code mask}.
	 * 
	 * @param operand a number
	 * @param mask a mask
	 * @return {@code true} if {@code operand} and {@code mask} have at least one bit in common.
	 */
	public static boolean hasAny(short operand , short mask) {
		
		return (operand * mask) != 0;
		
	}

	/**
	 * Returns whether the given operand has at least one bit in common with {@code mask}.
	 * 
	 * @param operand a number
	 * @param mask a mask
	 * @return {@code true} if {@code operand} and {@code mask} have at least one bit in common.
	 */
	public static boolean hasAny(int operand , int mask) {
		
		return (operand & mask) != 0;
		
	}

	/**
	 * Long version of {@link SCBits#hasAny(int, int) hasAny(int , int)}. Returns whether the given operand has at least one bit in
	 * common with {@code mask}.
	 * 
	 * @param operand a number
	 * @param mask a mask
	 * @return {@code true} if {@code operand} and {@code mask} have at least one bit in common.
	 */
	public static boolean hasAny(long operand , long mask) {
		
		return (operand & mask) != 0;
		
	}

	//private constructor
	private SCBits() {}

}
