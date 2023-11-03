/**
 * Licenced under MIT.
 */
package cs.sprites;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * Helper class used to write and read files from disk using {@code OutputStream} and {@code InputStream}.
 *  
 * @author Chris Brown
 *
 */
public final class CSFileOperations {
	
	/*
	 * Used for list prefix size calculations.
	 */		
	private static final int
		maxValue6Bits  = 0b111111 ,
		maxValue14Bits = 0b11111111111111 ,
		maxValue22Bits = 0b1111111111111111111111 ,
		maxValue30Bits = 0b111111111111111111111111111111
	;		

	/*
	 * Used as a temporary buffer of values being written or read from disk.
	 */
	private static final ThreadLocal<ByteBuffer> format = ThreadLocal.withInitial(() -> ByteBuffer.allocate(Long.BYTES));
	
	/**
	 * Sets the byte order to the given byte order
	 * 
	 * @param order — byte order of the formatter.get()
	 */
	public static void setByteOrder(ByteOrder order) {
		
		format.get().order(order);
		
	}
	
	/*
	 * 
	 * Default means of writing data to an output stream
	 * 
	 */
	
	/**
	 * Puts the given value in the writer.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putByte(byte object , W writer) throws IOException {
		
		writer.write(object);
		
	}

	/**
	 * Puts the given value in the writer with the byte order set by {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting
	 * to {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putShort(short object , W writer) throws IOException {
	
		ByteBuffer current = format.get(); 
		current.putShort(object);
		writer.write(format.get().array() , 0 , 2);
		current.rewind();
		
	}

	/**
	 * Puts the given value in the writer with the byte order set by {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting
	 * to {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putInt(int object , W writer) throws IOException {
	
		ByteBuffer current = format.get().putInt(object);
		writer.write(current.array() , 0 , 4);
		current.rewind();
		
	}

	/**
	 * Puts the given value in the writer with the byte order set by {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting
	 * to {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putLong(long object , W writer) throws IOException {

		ByteBuffer current = format.get().putLong(object);
		writer.write(current.array() , 0 , 8);
		current.rewind();
		
	}

	/**
	 * Puts the given value in the writer with the byte order set by {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting
	 * to {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putFloat(float object , W writer) throws IOException {
	
		ByteBuffer current = format.get().putFloat(object);
		writer.write(current.array() , 0 , 4);
		current.rewind();
		
	}

	/**
	 * Puts the given value in the writer with the byte order set by {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting
	 * to {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putDouble(double object , W writer) throws IOException {
	
		ByteBuffer current = format.get().putDouble(object);
		writer.write(current.array() , 0 , 8);
		current.rewind();
		
	}

	/**
	 * Puts the given value in the writer. The written value is 1 if {@code object} is {@code true}, 0 otherwise.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putBoolean(boolean object , W writer) throws IOException {
	
		writer.write(object ? 1 : 0);
			
	}

	/**
	 * Puts the given value in the writer with the byte order set by {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting
	 * to {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putChar(char object , W writer)throws IOException  {
	
		ByteBuffer current = format.get().putChar(object);
		writer.write(current.array() , 0 , 2);
		current.rewind();
		
	}

	/**
	 * Puts the given String in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the string are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putString(String stringValue , W writer) throws IOException {
		
		ByteBuffer current = format.get();
		writeSize(current , writer , stringValue.length());		
		
		IntStream chars = stringValue.chars();
		Iterator<Integer> iter = chars.iterator();
		while(iter.hasNext()) putChar((char) iter.next().shortValue() , writer);

		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN}.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putByteArray(byte[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);		
		writer.write(object);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putShortArray(short[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);		
		for(short x : object) putShort(x , writer);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putIntArray(int[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);
		for(int x : object) putInt(x , writer);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putLongArray(long[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);
		for(long x : object) putLong(x , writer);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putFloatArray(float[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);
		for(float x : object) putFloat(x , writer);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putDoubleArray(double[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);
		for(double x : object) putDouble(x , writer);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putBooleanArray(boolean[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);
		for(boolean x : object) putBoolean(x , writer);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putCharArray(char[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);
		for(char x : object) putChar(x , writer);
		
	}

	/**
	 * Puts the given array in the writer. The byte order of the size is {@link java.nio.ByteOrder#BIG_ENDIAN BIG_ENDIAN} and the values in
	 * the array are the current byte order.
	 * 
	 * @param <W> — Type of {@link java.io.OutputStream OutputStream} given.
	 * @param object — value to write 
	 * @param writer — writer to use
	 * @throws IOException if {@code writer} throws an exception.
	 */	
	public static final <W extends OutputStream> void putStringArray(String[] object , W writer) throws IOException {

		ByteBuffer current = format.get();
		writeSize(current , writer , object.length);
		for(String x : object) putString(x , writer);
		
	}
	
	/*
	 * 
	 * Default means of reading data from an input stream for the primitive classes and {@code String}, and arrays of those.
	 * 
	 */
	
	/**
	 * Gets a byte from the reader.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code byte} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> byte getByte(R reader) throws IOException {
		
		return (byte) reader.read();
		
	}

	/**
	 * Gets a value from the reader. The value is read as the current byte order set by 
	 * {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting to {@link java.nio.ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code short} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> short getShort(R reader) throws IOException {
		
		ByteBuffer current = format.get();
		reader.readNBytes(current.array(), 0, Short.BYTES);
		short value = current.getShort();
		current.rewind();
		return value;
		
	}

	/**
	 * Gets a value from the reader. The value is read as the current byte order set by 
	 * {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting to {@link java.nio.ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code int} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> int getInt(R reader) throws IOException {
		
		ByteBuffer current = format.get();
		reader.readNBytes(current.array(), 0, Integer.BYTES);
		int value = current.getInt();
		current.rewind();
		
		return value;
		
	}

	/**
	 * Gets a value from the reader. The value is read as the current byte order set by 
	 * {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting to {@link java.nio.ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code long} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> long getLong(R reader) throws IOException {

		ByteBuffer current = format.get();
		reader.readNBytes(current.array(), 0, Long.BYTES);
		long value = current.getLong();
		current.rewind();
		return value;
		
	}

	/**
	 * Gets a value from the reader. The value is read as the current byte order set by 
	 * {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting to {@link java.nio.ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code float} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> float getFloat(R reader) throws IOException {
		
		ByteBuffer current = format.get();
		reader.readNBytes(current.array(), 0, Float.BYTES);
		float value = current.getFloat();
		current.rewind();
		return value;
		
	}

	/**
	 * Gets a value from the reader. The value is read as the current byte order set by 
	 * {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting to {@link java.nio.ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code double} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> double getDouble(R reader) throws IOException {
		
		ByteBuffer current = format.get();
		reader.readNBytes(current.array(), 0, Double.BYTES);
		double value = current.getDouble();
		current.rewind();
		return value;
		
	}

	/**
	 * Gets a value from the reader. The result is {@code true} if the next byte read by the reader is not zero.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code boolean} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> boolean getBoolean(R reader) throws IOException {
		
		return (byte)reader.read() != 0;
		
	}

	/**
	 * Gets a value from the reader. The value is read as the current byte order set by 
	 * {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}, defaulting to {@link java.nio.ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code char} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> char getChar(R reader) throws IOException {
		
		ByteBuffer current = format.get();
		reader.readNBytes(current.array(), 0, Character.BYTES);
		char value = current.getChar();
		current.rewind();
		return value;
		
	}
	
	/**
	 * Gets a String from the reader. The characters from the String are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code String} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> String getString(R reader) throws IOException {

		char[] chars = new char[readSize(reader)];
		for(int i = 0 ; i < chars.length ; i++) chars[i] = getChar(reader);
		return new String(chars);
				
	}

	/**
	 * Gets an array from the reader. 
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code String} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> byte[] getByteArray(R reader) throws IOException {

		byte[] array = new byte[readSize(reader)];
		reader.read(array , 0 , array.length);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code short[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> short[] getShortArray(R reader) throws IOException {

		short[] array = new short[readSize(reader)];
		for(int i = 0 ; i < array.length ; i++) array[i] = getShort(reader);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code int[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> int[] getIntArray(R reader) throws IOException {

		int[] array = new int[readSize(reader)];
		for(int i = 0 ; i < array.length ; i++) array[i] = getInt(reader);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code long[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> long[] getLongArray(R reader) throws IOException {

		long[] array = new long[readSize(reader)];
		for(int i = 0 ; i < array.length ; i++) array[i] = getLong(reader);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code float[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> float[] getFloatArray(R reader) throws IOException {

		float[] array = new float[readSize(reader)];
		for(int i = 0 ; i < array.length ; i++) array[i] = getFloat(reader);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code double[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> double[] getDoubleArray(R reader) throws IOException {

		double[] array = new double[readSize(reader)];
		for(int i = 0 ; i < array.length ; i++) array[i] = getDouble(reader);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code boolean[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> boolean[] getBooleanArray(R reader) throws IOException {

		boolean[] array = new boolean[readSize(reader)];
		for(int i = 0 ; i < array.length ; i++) array[i] = getBoolean(reader);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code char[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> char[] getCharArray(R reader) throws IOException {

		char[] array = new char[readSize(reader)];		
		for(int i = 0 ; i < array.length ; i++) array[i] = getChar(reader);
		return array;
		
	}

	/**
	 * Gets an array from the reader. The values from the array are read as the current byte order.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return {@code String[]} value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static final <R extends InputStream> String[] getStringArray(R reader) throws IOException {

		String[] array = new String[readSize(reader)];		
		for(int i = 0 ; i < array.length ; i++) array[i] = getString(reader);
		return array;
		
	}

	/*
	 * The following 3 methods are used to compute the number of bytes to precede a list notating the list's size. File Composition stores
	 * list lengths preceding their contents in files. Instead of using a flat 4 byte size value, we choose a size as a function of the 
	 * number of elements in the list. This means the number of bytes preceding a list, which contains that list's sizesize is [1 , 4]. 
	 * This also means that we lose out on a significant number of elements a single list can store because the highest two bits are used 
	 * to store the number of bytes the size will take up.
	 * 
	 * With two bits, the greatest value we can store is 3. So at least one byte is always used to store the list size. The last two bits
	 * of this number contain the amount of bytes to read forward and compose into a list size in elements. Since the user will know the
	 * types of elements in an array, it is acceptable to store the elements of the list rather than the bytes of the list.
	 *
	 * The method below the following 3 reads the list size prefix from a buffer and returns the size in elements of the list.
	 *
	 */
	
	/**
	 * Computes the number of additional bytes a list size prefix would be for a list of size {@code length}.
	 * 
	 * @param length — arbitrary size of a list
	 * @return Number of additional bytes beyond the first a list size prefix would be for a list of size {@code length}.
	 */
	private static int additionalListSizePrefixBytes(int length) {

		if(length <= maxValue6Bits) return 0;
		else if(length <= maxValue14Bits) return 1;
		else if(length <= maxValue22Bits) return 2;
		else if(length <= maxValue30Bits) return 3;
		else throw new RuntimeException("List exceeds possible length, break into two or more.");
		
	}
		
	/**
	 * Writes a list size into a buffer, then an {@code OutputStream}, returning the buffer after writing. The resulting buffer's position
	 * is 0 and its byte order is the same as it was when passed into this method, however size value of the list is always big endian.
	 * 
	 * @param <W> — {@code OutputStream} implementation
	 * @param buffer — a buffer to format with
	 * @param writer — an {@code OutputStream} to write to disk with
	 * @param listSize — number of elements in some list
	 * @return {@code buffer} after writing to disk.
	 * @throws IOException if the {@code writer} throws an exception.
	 */
	public static <W extends OutputStream> ByteBuffer writeSize(ByteBuffer buffer , W writer , int listSize) throws IOException {
		
		ByteOrder currentOrder = buffer.order();
		boolean doChangeOrder = currentOrder != ByteOrder.BIG_ENDIAN;
		if(doChangeOrder) buffer.order(ByteOrder.BIG_ENDIAN);
		
		int listSizePrefixSize = additionalListSizePrefixBytes(listSize);
		//creates the integer used to store the size of the list and the two bit ending.
		//the listSizePrefixSize is the two bit part. It's shifted to the end of the integer, then the rest of the size is ORed on.
		int sizeComposed = (((listSizePrefixSize << 6 + (8 * listSizePrefixSize)) | listSize));
		
		switch(listSizePrefixSize) {
			case 0 -> buffer.put((byte)sizeComposed);
			case 1 -> buffer.putShort((short)sizeComposed);
			case 2 -> buffer.put((byte)(sizeComposed >> 16)).putShort((short)sizeComposed);
			case 3 -> buffer.putInt(sizeComposed);
			default -> throw new IllegalArgumentException(listSize + " is too large a list size.");
		};
		
		//plus 1 because the listSizePrefixSize value stores the number additional bytes beyond the first the size will take up
		writer.write(buffer.array() , 0 , listSizePrefixSize + 1);
		
		//reset states
		if(doChangeOrder) buffer.order(currentOrder);		
		buffer.rewind();
		
		return buffer;
	
	}
	
	/**
	 * Reads the size of a list from the reader, returning the result. This does not interfere with the current byte order set by 
	 * {@link CSFileOperations#setByteOrder(ByteOrder) setByteOrder}.
	 * 
	 * @param <R> — Type of {@link java.io.InputStream InputStream} given.
	 * @param reader — reader to read from
	 * @return List size value read from {@code reader}. 
	 * @throws IOException if {@code reader} throws an exception.
	 */
	public static <R extends InputStream> int readSize(R reader) throws IOException {
		
		ByteBuffer current = format.get();
		ByteOrder currentOrder = current.order();
		boolean doChangeOrder = currentOrder != ByteOrder.BIG_ENDIAN;
		if(doChangeOrder) current.order(ByteOrder.BIG_ENDIAN);
		
		byte first = (byte) reader.read();
		//remaining gets the highest two bits of the first byte. The bits store the number of additional bytes to read to make a list size 
		byte remaining = (byte) ((255 & first) >> 6);
		
		//permanently remove the highest two bits
		first &= maxValue6Bits;
		
		//get an integer of the size
		int size = switch(remaining) {
			case 0 -> first;
			case 1 -> current.put(first).put((byte)reader.read()).rewind().getShort();
			case 2 -> current.put((byte)0).put(first).put((byte)reader.read()).put((byte)reader.read()).rewind().getInt();
			case 3 -> current.put(first).put((byte)reader.read()).put((byte)reader.read()).put((byte)reader.read()).rewind().getInt();
			default -> throw new IllegalArgumentException("Failed to read list size.");			
		};
		
		current.rewind();
		if(doChangeOrder) current.order(currentOrder);
		
		return size;
		
	}

	private CSFileOperations() {}
	
}