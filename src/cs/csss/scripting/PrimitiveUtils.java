package cs.csss.scripting;

import static cs.core.utils.CSUtils.specify;

/**
 * Class Python scripts can import which facilitate conversion to java primitive types from {@code Object} which Python typically treats 
 * everything as.
 */
public final class PrimitiveUtils {

	/**
	 * Attemtps to convert {@code object} to a {@code byte}.
	 * 
	 * @param object — an object
	 * @return Byte representation of {@code object}.
	 */
	public static byte toByte(Object object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object;
		return asDouble.byteValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to a {@code short}.
	 * 
	 * @param object — an object
	 * @return Short representation of {@code object}.
	 */
	public static short toShort(Object object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object;
		return asDouble.shortValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to an {@code int}.
	 * 
	 * @param object — an object
	 * @return Int representation of {@code object}.
	 */
	public static int toInt(Object object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object;
		return asDouble.intValue();
		
	}
	
	private PrimitiveUtils() {}
	
}
