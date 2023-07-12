package cs.csss.scripting;

import static cs.core.utils.CSUtils.specify;

public final class PrimitiveUtils {

	public static byte toByte(Object object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object;
		return asDouble.byteValue();
		
	}

	public static short toShort(Object object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object;
		return asDouble.shortValue();
		
	}

	public static int toInt(Object object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object;
		return asDouble.intValue();
		
	}
	
	private PrimitiveUtils() {}
	
}
