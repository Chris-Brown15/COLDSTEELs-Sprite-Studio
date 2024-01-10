/**
 * 
 */
package cs.csss.utils;

import static cs.core.utils.CSUtils.specify;

import java.util.Objects;

import org.python.core.PyObject;

import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.Color;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.LookupPixel;
import cs.csss.engine.Pixel;

/**
 * Contains utilities for scripts.
 */
public final class ScriptingUtils2 {

	/**
	 * Synchronizes on {@code object} to run {@code code}.
	 * 
	 * @param object — any nonnull object
	 * @param code — code to invoke
	 */
	public static final void synchronizedOn(Object object , Runnable code) {
		
		Objects.requireNonNull(object);
		
		synchronized(object) {
			
			code.run();
			
		}
		
	}
	
	/**
	 * Returns whether the given object is a lookup pixel.
	 * 
	 * @param object — an object who is being checked for being a lookup pixel
	 * @return Whether the given object is a lookup pixel.
	 */
	public static boolean isLookupPixel(Object object) {
		
		return object instanceof Pixel asPixel && asPixel instanceof LookupPixel;
		
	}

	/**
	 * Returns whether the given object is a color pixel.
	 * 
	 * @param object — an object who is being checked for being a color pixel
	 * @return Whether the given object is a color pixel.
	 */
	public static boolean isColorPixel(Object object) {
		
		return object instanceof Pixel asPixel && asPixel instanceof ColorPixel;
		
	}
	
	/**
	 * Attemtps to convert {@code object} to a {@code byte}.
	 * 
	 * @param object — an object
	 * @return Byte representation of {@code object}.
	 */
	public static byte doubleToByte(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object.__tojava__(Double.TYPE);
		return asDouble.byteValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to a {@code short}.
	 * 
	 * @param object — an object
	 * @return Short representation of {@code object}.
	 */
	public static short doubleToShort(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object.__tojava__(Double.TYPE);
		return asDouble.shortValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to an {@code int}.
	 * 
	 * @param object — an object
	 * @return Int representation of {@code object}.
	 */
	public static int doubleToInt(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Double asDouble = (Double)object.__tojava__(Double.TYPE);
		return asDouble.intValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to a {@code byte}.
	 * 
	 * @param object — an object
	 * @return Byte representation of {@code object}.
	 */
	public static byte floatToByte(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Float asfloat = (Float)object.__tojava__(Float.TYPE);
		return asfloat.byteValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to a {@code short}.
	 * 
	 * @param object — an object
	 * @return Short representation of {@code object}.
	 */
	public static short floatToShort(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Float asfloat = (Float)object.__tojava__(Float.TYPE);
		return asfloat.shortValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to an {@code int}.
	 * 
	 * @param object — an object
	 * @return Int representation of {@code object}.
	 */
	public static int floatToInt(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Float asfloat = (Float)object.__tojava__(Float.TYPE);
		return asfloat.intValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to a {@code byte}.
	 * 
	 * @param object — an object
	 * @return Byte representation of {@code object}.
	 */
	public static byte longToByte(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Long asDouble = (Long)object.__tojava__(Long.TYPE);
		return asDouble.byteValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to a {@code short}.
	 * 
	 * @param object — an object
	 * @return Short representation of {@code object}.
	 */
	public static short longToShort(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Long asDouble = (Long)object.__tojava__(Long.TYPE);
		return asDouble.shortValue();
		
	}

	/**
	 * Attemtps to convert {@code object} to an {@code int}.
	 * 
	 * @param object — an object
	 * @return Int representation of {@code object}.
	 */
	public static int longToInt(PyObject object) {
		
		specify(!object.getClass().isPrimitive() , object + " is not a primitive");
		Long asDouble = (Long)object.__tojava__(Long.TYPE);
		return asDouble.intValue();
		
	}

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

	/**
	 * Creates an array of objects.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static Object[] array(int size) {
		
		return new Object[size];
		
	}
	
	/**
	 * Creates an array of {@link cs.csss.engine.ChannelBuffer ChannelBuffer}s.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static ChannelBuffer[] channelBuffers(int size) {
		
		return new ChannelBuffer[size];
		
	}

	/**
	 * Creates an array of {@link cs.csss.engine.Color Color}s.
	 * 
	 * @param size — number of elements the resulting array can contain
	 * @return Newly created array.
	 */
	public static Color[] colors(int size) {

		return new Color[size];
		
	}
	
	/**
	 * Private Constructor 
	 */
	private ScriptingUtils2() {}

}
