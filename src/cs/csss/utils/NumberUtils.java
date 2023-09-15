package cs.csss.utils;

import static cs.core.utils.CSUtils.specify;

/**
 * Container for utilties for numbers.
 */
public final class NumberUtils {
		
	/**
	 * Returns whether a number {@code number} is a power of two, meaning some {@code n} exists such that {@code (2 ^ n) == number} is 
	 * {@code true} 
	 * 
	 * @param number — an integer
	 * @return {@code true} if {@code number} is a power of two.
	 */
	public static boolean isPowerOfTwo(int number) {
		
		//number is odd
		if((number & 1) == 1) return false;
		
		int iter = 2;
		while(iter <= number) {
			
			if(iter == number) return true;
			iter <<= 1;
			
		}
		
		return false;
		
	}
	
	/**
	 * Returns the nearest power of two to the given number. The resulting number may or may not be greater to {@code nearestToThis}.
	 * 
	 * @param nearestToThis — a number whose nearest power of two is being queried
	 * @return The number nearest to {@code nearestToThis} given by {@code 2^n}.
	 */
	public static int nearestPowerOfTwo(int nearestToThis) {
		
		specify(nearestToThis >= 0 , nearestToThis + " is out of bounds.");		
		
		if(nearestToThis <= 2) return 2;
		
		int iter = 2;
		while(iter <= nearestToThis) {
			
			if(iter >= nearestToThis) break;
			iter <<= 1;
			
		}
		
		//if the iter is equal to nearestToThis, nearestToThis is a power of two, so return iter.
		//if nearestToThis is closer to the next greatest power of two than it is to the next lowest, return iter
		if(iter == nearestToThis || iter - nearestToThis < nearestToThis - (iter >> 1)) return iter;
		//otherwise return the next lowest
		else return iter >> 1;		
		
	}
	
	/**
	 * Returns the number nearest to {@code nearestToThis} that is a power of two. If {@code nearestToThis} is a power of two, it is 
	 * returned.
	 * 
	 * @param nearestToThis — a value whose nearest power of two is being returned
	 * @return A number given by {@code 2^n} nearest to or equal to {@code nearestToThis}.
	 */
	public static int nearestGreaterOrEqualPowerOfTwo(int nearestToThis) {

		specify(nearestToThis >= 0 , nearestToThis + " is out of bounds.");		
		
		if(nearestToThis <= 2) return 2;
		
		int iter = 2;
		while(iter < nearestToThis) iter <<= 1;
		
		return iter;
		
	}

	/**
	 * Returns the number nearest to {@code nearestToThis} that is a power of two. If {@code nearestToThis} is a power of two, it is 
	 * returned.
	 * 
	 * @param nearestToThis — a value whose nearest power of two is being returned
	 * @return A number given by {@code 2^n} nearest to or equal to {@code nearestToThis}.
	 */
	public static int nearestGreaterOrEqualPowerOfTwo(float nearestToThis) {

		specify(nearestToThis >= 0 , nearestToThis + " is out of bounds.");		
		
		if(nearestToThis <= 2) return 2;
		
		int iter = 2;
		while(iter < nearestToThis) iter <<= 1;
		
		return iter;
		
	}
	
	/**
	 * Computes the exponent 2 would be raised to in order to equal {@code powerOfTwo}. {@code powerOfTwo} must be a powr of two. 
	 * 
	 * @param powerOfTwo — a power of two number
	 * @return The exponent 2 would be raied to in order to equal {@code powerOfTo}.
	 */
	public static int powerOfTwoExponent(int powerOfTwo) {
		
		//ensures powerOfTwo is not odd
		specify((powerOfTwo & 1) != 1 , powerOfTwo + " is not a power of two.");
		
		int iter = 0; 
		while((1 << iter) < powerOfTwo) iter++;
		
		specify((1 << iter) == powerOfTwo , powerOfTwo + " is not a power of two.");
		
		return iter;
		
	}

	/**
	 * Computes the triangular number of {@code input}.
	 * 
	 * @param input — an input
	 * @return Triangular number of input
	 */
	public static int triangularNumber(int input) {
		
		specify(input >= 0 , "Triangular number inputs must be positive.");
		return (input * (input + 1)) / 2;
		
	}
	
	/**
	 * Implementation of Cantors Algorithm, a means for creating a unique natural number for two other naturals, with 0 being included in
	 * naturals. 
	 * 
	 * @param x — some natural number satisfying {@code x >= 0}
	 * @param y — some natural number satisfying {@code y >= 0}
	 * @return Natural number representing Cantors Algorithm on {@code x}, and {@code y}.
	 */
	public static int cantor(int x , int y) {
		
		specify(x >= 0 , "Cantor inputs must be positive.");
		specify(y >= 0 , "Cantor inputs must be positive.");
		int sum = x + y;
		return triangularNumber(sum) + y;
		
	}
	
	private NumberUtils() {}
	
}
