package cs.csss.scripting;

/**
 * Class Python scripts can import, used to create and write to Java primitve arrays.
 *
 */
public final class ArrayUtils {

	/**
	 * Creates an array of bytes.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static byte[] bytes(int size) {
		
		return new byte[size];
		
	}

	/**
	 * Creates an array of shorts.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static short[] shorts(int size) {
		
		return new short[size];
		
	}

	/**
	 * Creates an array of ints.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static int[] ints(int size) {
		
		return new int[size];
		
	}

	/**
	 * Creates an array of longs.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static long[] longs(int size) {
		
		return new long[size];
		
	}

	/**
	 * Creates an array of floats.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static float[] floats(int size) {
		
		return new float[size];
		
	}

	/**
	 * Creates an array of doubles.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static double[] doubles(int size) {
		
		return new double[size];
		
	}

	/**
	 * Creates an array of booleans.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static boolean[] booleans(int size) {
		
		return new boolean[size];
		
	}

	/**
	 * Creates an array of strings.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static String[] strings(int size) {
		
		return new String[size];
		
	}
	
	private ArrayUtils() {}

}
