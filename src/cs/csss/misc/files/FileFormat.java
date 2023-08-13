package cs.csss.misc.files;

import static cs.core.utils.CSUtils.require;
import static cs.core.utils.CSUtils.specify;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import cs.core.utils.ByteSupplier;
import cs.core.utils.CharSupplier;
import cs.core.utils.DoubleSupplier;
import cs.core.utils.FloatSupplier;
import cs.core.utils.LongSupplier;
import cs.core.utils.ShortSupplier;

/**
 * Used to define a file format which can be written to or read in according to the specification defined by the {@code add} methods.
 * <p>
 * Instances of this class are made of up of entries. These entries contain all that is needed to write an object to a file as binary.
 * {@code FileComposition}s can be used for writing files, but they can also be used to read files that were written with a 
 * {@code FileComposition}. This is why you can add entries to instances of this class without providing any other data in some cases. 
 * Depending on the type of data wrapped by the entry, it can be written or read automatically because this class provides standard ways to
 * write primitive types, the {@code String} type, and arrays of the primitive types and {@code String} type.
 * </p>
 *  
 * @author Chris Brown
 *
 */
public class FileFormat<Writer extends OutputStream , Reader extends InputStream> implements Iterable<FileEntry2>  {
	
	/*
	 * Used for list prefix size calculations.
	 */		
	private static final int
		maxValue6Bits  = 0b111111 ,
		maxValue14Bits = 0b11111111111111 ,
		maxValue22Bits = 0b1111111111111111111111 ,
		maxValue30Bits = 0b111111111111111111111111111111
	;		

	/**
	 * Default means of writing data to a byte buffer for the primitive classes and {@code String}.
	 */
	public static final void putByte(Object object , ByteBuffer buffer) {
		
		buffer.put((byte)object);
		
	}
	
	public static final void putShort(Object object , ByteBuffer buffer) {
	
		buffer.putShort((short)object);
		
	}

	public static final void putInt(Object object , ByteBuffer buffer) {
	
		buffer.putInt((int)object);
		
	}

	public static final void putLong(Object object , ByteBuffer buffer) {
	
		buffer.putLong((long)object);
		
	}

	public static final void putFloat(Object object , ByteBuffer buffer) {
	
		buffer.putFloat((float)object);
		
	}

	public static final void putDouble(Object object , ByteBuffer buffer) {
	
		buffer.putDouble((double)object);
		
	}

	public static final void putBoolean(Object object , ByteBuffer buffer) {
	
		buffer.put((boolean)object ? (byte) 1 : 0);
		
	}

	public static final void putChar(Object object , ByteBuffer buffer) {
	
		buffer.putChar((char)object);
		
	}
	
	public static final void putString(Object stringValue , ByteBuffer buffer) {
		
		byte[] bytes = ((String)stringValue).getBytes();
		writeSize(buffer , (bytes.length)).put(bytes);
		
	}

	public static final void putByteArray(Object object , ByteBuffer buffer) {
		
		byte[] asByteArray = (byte[])object;
		writeSize(buffer , asByteArray.length).put(asByteArray);
					
	}

	public static final void putShortArray(Object object , ByteBuffer buffer) {
		
		short[] array = (short[])object;
		writeSize(buffer , array.length);
		for(short x : array) putShort(x , buffer);
					
	}

	public static final void putIntArray(Object object , ByteBuffer buffer) {
		
		int[] array = (int[])object;
		writeSize(buffer , array.length);
		for(int x : array) putInt(x , buffer);
					
	}

	public static final void putLongArray(Object object , ByteBuffer buffer) {
		
		long[] array = (long[])object;
		writeSize(buffer , array.length);
		for(long x : array) putLong(x , buffer);
					
	}

	public static final void putFloatArray(Object object , ByteBuffer buffer) {
		
		float[] array = (float[])object;
		writeSize(buffer , array.length);
		for(float x : array) putFloat(x , buffer);
					
	}

	public static final void putDoubleArray(Object object , ByteBuffer buffer) {
		
		double[] array = (double[])object;
		writeSize(buffer , array.length);
		for(double x : array) putDouble(x , buffer);
					
	}

	public static final void putBooleanArray(Object object , ByteBuffer buffer) {
		
		boolean[] array = (boolean[])object;
		writeSize(buffer , array.length);
		for(boolean x : array) putBoolean(x , buffer);
					
	}

	public static final void putCharArray(Object object , ByteBuffer buffer) {
		
		char[] array = (char[])object;
		writeSize(buffer , array.length);
		for(char x : array) putChar(x , buffer);
					
	}

	public static final void putStringArray(Object object , ByteBuffer buffer) {
		
		String[] array = (String[])object;
		writeSize(buffer , array.length);
		for(String x : array) putString(x , buffer);
					
	}
	
	/**
	 * Default means of reading data from a byte buffer for the primitive classes and {@code String}.
	 */	
	public static final Object getByte(ByteBuffer buffer) {
		
		return buffer.get();
		
	}

	public static final Object getShort(ByteBuffer buffer) {
		
		return buffer.getShort();
		
	}

	public static final Object getInt(ByteBuffer buffer) {
		
		return buffer.getInt();
		
	}

	public static final Object getLong(ByteBuffer buffer) {
		
		return buffer.getLong();
		
	}

	public static final Object getFloat(ByteBuffer buffer) {
		
		return buffer.getFloat();
		
	}

	public static final Object getDouble(ByteBuffer buffer) {
		
		return buffer.getDouble();
		
	}

	public static final Object getBoolean(ByteBuffer buffer) {
		
		return buffer.get() != 0;
		
	}

	public static final Object getChar(ByteBuffer buffer) {
		
		return buffer.getChar();
		
	}
	
	public static final Object getString(ByteBuffer buffer) {
		
		byte[] bytes = new byte[readSize(buffer)];
		for(int i = 0 ; i < bytes.length ; i++) bytes[i] = buffer.get();
		return new String(bytes);
				
	}

	public static final Object getByteArray(ByteBuffer buffer) {
		
		byte[] array = new byte[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.get();
		return array;
		
	}
	
	public static final Object getShortArray(ByteBuffer buffer) {
		
		short[] array = new short[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getShort();
		return array;
		
	}
	
	public static final Object getIntArray(ByteBuffer buffer) {
		
		int[] array = new int[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getInt();
		return array;
		
	}

	public static final Object getLongArray(ByteBuffer buffer) {
		
		long[] array = new long[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getLong();
		return array;
		
	}

	public static final Object getFloatArray(ByteBuffer buffer) {
		
		float[] array = new float[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getFloat();
		return array;
		
	}

	public static final Object getDoubleArray(ByteBuffer buffer) {
		
		double[] array = new double[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getDouble();
		return array;
		
	}
	
	public static final Object getBooleanArray(ByteBuffer buffer) {
		
		boolean[] array = new boolean[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.get() != 0;
		return array;
		
	}

	public static final Object getCharArray(ByteBuffer buffer) {
		
		char[] array = new char[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getChar();
		return array;
		
	}

	public static final Object getStringArray(ByteBuffer buffer) {
		
		String[] array = new String[readSize(buffer)];
		for(int i = 0 ; i < array.length ; i++) array[i] = (String) getString(buffer);
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
	 * Returns the total number of bytes a list prefix will be for a list of size {@code listLength}.
	 * 
	 * @param listLength — arbitrary size of a list
	 * @return Number of bytes a prefix to a list of size {@code listLength} would be. 
	 */
	public static int listSizePrefixSize(int listLength) {
		
		return additionalListSizePrefixBytes(listLength) + 1;
		
	}
	
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
	 * Writes a size of a list in a {@code ByteBuffer}, returning the buffer after writing. 
	 * 
	 * @param buffer — a buffer to write to
	 * @param listSize — number of elements in a list
	 * @return {@code ByteBuffer} containing a size of elements of a list.
	 */
	public static ByteBuffer writeSize(ByteBuffer buffer , int listSize) {
				
		int listSizePrefixSize = additionalListSizePrefixBytes(listSize);
		//creates the integer used to store the size of the list and the two bit ending.
		//the listSizePrefixSize is the two bit part. It's shifted to the end of the integer, then the rest of the size is ORed on.
		int sizeComposed = (((listSizePrefixSize << 6 + (8 * listSizePrefixSize)) | listSize));
		
		return switch(listSizePrefixSize) {
			case 0 -> buffer.put((byte)sizeComposed);
			case 1 -> buffer.putShort((short)sizeComposed);
			case 2 -> buffer.put((byte)(sizeComposed >> 16)).putShort((short)sizeComposed);
			case 3 -> buffer.putInt(sizeComposed);
			default -> throw new IllegalArgumentException(listSize + " is too large a list size.");
		};
		
	}
	
	/**
	 * Reads the prefix of a list and returns the size in elements of the list.
	 * 
	 * @param buffer — {@code ByteBuffer} containing data
	 * @return Size of a list.
	 */
	public static int readSize(ByteBuffer buffer) {
		
		byte 
			first = buffer.get(buffer.position()),
			remaining = (byte) ((255 & first) >> 6)
		;
			
		return switch(remaining) {
			case 0 -> buffer.get(); //the two bits are 0 so no extra bitwise operations are needed
			case 1 -> buffer.getShort() & maxValue14Bits;
			case 2 -> (buffer.get() << 16 | (0xffff & buffer.getShort())) & maxValue22Bits;
			case 3 -> buffer.getInt() & maxValue30Bits;
			default -> throw new IllegalArgumentException("Failure to read list size.");
		};
				
	}
	
	private final LinkedList<FileEntry2> data = new LinkedList<>(); 
	
	private int fileSizeBytes = 0;
	
	private final Supplier<Writer> writer;
	private final Supplier<Reader> reader;
	
	protected FileFormat(FileFormat<Writer , Reader> source , Supplier<Writer> writer , Supplier<Reader> reader) {
		
		this(writer , reader);
		source.data.forEach(entry -> data.add(new FileEntry2(entry)));
		this.fileSizeBytes = source.fileSizeBytes;
		
	}
	
	public FileFormat(Supplier<Writer> writer , Supplier<Reader> reader) {
		
		this.writer = writer;
		this.reader = reader;
		
	}
	
	/**
	 * Adds a byte and binds its value for writing.
	 * 
	 * @param name — name of the byte entry
	 * @param value — value of the byte
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addByte(String name , byte value) { 
		
		verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry2(value , 1 , name , FileFormat::putByte , FileFormat::getByte));	
		return this;
		
	}
	
	/**
	 * Adds a byte and relies on a supplier for its value.
	 * 
	 * @param name — name of the byte entry
	 * @param byteGetter — getter
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addByte(String name , ByteSupplier byteGetter) { 
		
		verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry2(1 , () -> byteGetter.getAsByte() , name , FileFormat::putByte , FileFormat::getByte));	
		return this;
		
	}
	
	/**
	 * Adds a byte entry with no bound value.
	 * 
	 * @param name — name of the byte entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addByte(String name) {

		verifyName(name);		
		data.add(new FileEntry2(name , FileFormat::putByte, FileFormat::getByte));		
		return this;
		
	}

	/**
	 * Adds a short and binds its value for writing.
	 * 
	 * @param name — name of the short entry
	 * @param value — value of the short
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addShort(String name , short value) {

		verifyName(name);		
		fileSizeBytes += 2;		
		data.add(new FileEntry2(value , 2 , name , FileFormat::putShort , FileFormat::getShort));
		return this;
	
	}

	/**
	 * Adds a short and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param shortGetter — getter
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addShort(String name , ShortSupplier shortGetter) {

		verifyName(name);		
		fileSizeBytes += 2;		
		data.add(new FileEntry2(2 , () -> shortGetter.getAsShort() , name , FileFormat::putShort , FileFormat::getShort));
		return this;
	
	}

	/**
	 * Adds a short entry with no bound value.
	 * 
	 * @param name — name of the short entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addShort(String name) {

		verifyName(name);	
		data.add(new FileEntry2(name , FileFormat::putShort , FileFormat::getShort));		
		return this;
		
	}

	/**
	 * Adds an int and binds its value for writing.
	 * 
	 * @param name — name of the int entry
	 * @param value — value of the int
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addInt(String name , int value) {
		
		verifyName(name);		
		fileSizeBytes += 4;		
		data.add(new FileEntry2(value , 4 , name , FileFormat::putInt , FileFormat::getInt));
		return this;
	
	}

	/**
	 * Adds an int and relies on a supplier for its value.
	 * 
	 * @param name — name of the int entry
	 * @param intGetter — getter
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addInt(String name , IntSupplier intGetter) {
		
		verifyName(name);		
		fileSizeBytes += 4;		
		data.add(new FileEntry2(4 , () -> intGetter.getAsInt() , name , FileFormat::putInt , FileFormat::getInt));
		return this;
	
	}

	/**
	 * Adds an int entry with no bound value.
	 * 
	 * @param name — name of the int entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addInt(String name) {
		
		verifyName(name);		
		data.add(new FileEntry2(name , FileFormat::putInt , FileFormat::getInt));		
		return this;
		
	}

	/**
	 * Adds a long and binds its value for writing.
	 * 
	 * @param name — name of the long entry
	 * @param value — value of the long
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addLong(String name , long value) {

		verifyName(name);
		fileSizeBytes += 8;
		data.add(new FileEntry2(value , 8 , name , FileFormat::putLong , FileFormat::getLong));
		return this;
	
	}

	/**
	 * Adds a long and relies on a supplier for its value.
	 * 
	 * @param name — name of the long entry
	 * @param longGetter — getter
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addLong(String name , LongSupplier longGetter) {

		verifyName(name);
		fileSizeBytes += 8;
		data.add(new FileEntry2(8 , () -> longGetter.getAsLong() , name , FileFormat::putLong , FileFormat::getLong));
		return this;
	
	}

	/**
	 * Adds a long entry with no bound value.
	 * 
	 * @param name — name of the long entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addLong(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putLong , FileFormat::getLong));
		return this;
		
	}

	/**
	 * Adds a float and binds its value for writing.
	 * 
	 * @param name — name of the float entry
	 * @param value — value of the float
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addFloat(String name , float value) {
		
		verifyName(name);		
		fileSizeBytes += 4;				
		data.add(new FileEntry2(value , 4 , name , FileFormat::putFloat , FileFormat::getFloat));
		return this;
	
	}

	/**
	 * Adds a float and relies on a supplier for its value.
	 * 
	 * @param name — name of the float entry
	 * @param value — value of the float
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addFloat(String name , FloatSupplier floatGetter) {
		
		verifyName(name);		
		fileSizeBytes += 4;				
		data.add(new FileEntry2(4 , () -> floatGetter.getAsFloat() , name , FileFormat::putFloat , FileFormat::getFloat));
		return this;
	
	}

	/**
	 * Adds a float entry with no bound value.
	 * 
	 * @param name — name of the float entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addFloat(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putFloat , FileFormat::getFloat));
		return this;
		
	}

	/**
	 * Adds a double and binds its value for writing.
	 * 
	 * @param name — name of the double entry
	 * @param value — value of the double
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addDouble(String name , double value) {
		
		verifyName(name);
		fileSizeBytes += 8;				
		data.add(new FileEntry2(value , 8 , name , FileFormat::putDouble , FileFormat::getDouble));
		return this;
	
	}

	/**
	 * Adds a double and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param doubleGetter — getter
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addDouble(String name , DoubleSupplier doubleGetter) {
		
		verifyName(name);
		fileSizeBytes += 8;				
		data.add(new FileEntry2(8 , () -> doubleGetter.getAsDouble() , name , FileFormat::putDouble , FileFormat::getDouble));
		return this;
	
	}

	/**
	 * Adds a double entry with no bound value.
	 * 
	 * @param name — name of the double entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addDouble(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putDouble , FileFormat::getDouble));
		return this;
	
	}

	/**
	 * Adds a boolean and binds its value for writing.
	 * 
	 * @param name — name of the boolean entry
	 * @param value — value of the boolean
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addBoolean(String name , boolean value) {
		
		verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry2(value , 1 , name , FileFormat::putBoolean , FileFormat::getBoolean));		
		return this;
		
	}

	/**
	 * Adds a boolean and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param booleanGetter — getter
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addBoolean(String name , BooleanSupplier booleanGetter) {
		
		verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry2(1 , () -> booleanGetter.getAsBoolean() , name , FileFormat::putBoolean , FileFormat::getBoolean));		
		return this;
		
	}

	/**
	 * Adds a boolean entry with no bound value.
	 * 
	 * @param name — name of the boolean entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addBoolean(String name) {
		
		verifyName(name);		
		data.add(new FileEntry2(name , FileFormat::putBoolean , FileFormat::getBoolean));		
		return this;
		
	}

	/**
	 * Adds a character and binds its value for writing.
	 * 
	 * @param name — name of the character entry
	 * @param value — value of the character
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addCharacter(String name , char value) {
		
		verifyName(name);		
		fileSizeBytes += 2;				
		data.add(new FileEntry2(value , 2 , name , FileFormat::putChar , FileFormat::getChar));		
		return this;
		
	}

	/**
	 * Adds a character and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param characterGetter — getter
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addCharacter(String name , CharSupplier charGetter) {
		
		verifyName(name);		
		fileSizeBytes += 2;				
		data.add(new FileEntry2(2 , () -> charGetter.getAsChar() , name , FileFormat::putChar , FileFormat::getChar));		
		return this;
		
	}

	/**
	 * Adds a character entry with no bound value.
	 * 
	 * @param name — name of the character entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addCharacter(String name) {
		
		verifyName(name);		
		data.add(new FileEntry2(name , FileFormat::putChar , FileFormat::getChar));		
		return this;
		
	}

	/**
	 * Adds a String and binds its reference for writing.
	 * 
	 * @param name — name of the String entry
	 * @param value — reference of the String
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addString(String name , String value) {
		
		verifyName(name);	
		int prefixSize = listSizePrefixSize(value.length());
		int dataSize = (value.length() + prefixSize); 		
		fileSizeBytes += dataSize;		
		data.add(new FileEntry2(value , dataSize , name , FileFormat::putString , FileFormat::getString));
		return this;
	
	}

	/**
	 * Adds a String entry with no bound reference.
	 * 
	 * @param name — name of the String entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addString(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putString , FileFormat::getString));
		return this;
		
	}

	/**
	 * Adds a composable entry and binds its reference for reading or writing.
	 * 
	 * @param name — name of the Composable entry
	 * @param composable — instance of {@code Composable}
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addData(String name , Composable composable) {
		
		verifyName(name);		
		data.add(new FileEntry2(name , composable));	
		return this;
		
	}

	/**
	 * Adds a composable entry and binds its reference for reading or writing.
	 * 
	 * @param name — name of the Composable entry
	 * @param getter — function returning an instance of a Composable
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addData(String name , Supplier<Composable> getter) {
		
		verifyName(name);		
		data.add(new FileEntry2(name , getter));	
		return this;
		
	}
	
	/**
	 * Adds an array of bytes and binds its reference for writing.
	 * 
	 * @param name — name of the byte array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addByteArray(String name , byte[] array) {
		
		verifyName(name);
		int prefixSize = listSizePrefixSize(array.length);
		fileSizeBytes += prefixSize + array.length;
		data.add(new FileEntry2(array , prefixSize + array.length , name , FileFormat::putByteArray , FileFormat::getByteArray));
		return this;
		
	}

	/**
	 * Adds an array of bytes with no bound reference.
	 * 
	 * @param name — name of the byte array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addByteArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putByteArray , FileFormat::getByteArray));
		return this;
		
	}

	/**
	 * Adds an array of shorts and binds its reference for writing.
	 * 
	 * @param name — name of the short array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addShortArray(String name , short[] array) {
		
		verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 2); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name , FileFormat::putShortArray , FileFormat::getShortArray));
		return this;
		
	}

	/**
	 * Adds an array of shorts with no bound reference.
	 * 
	 * @param name — name of the short array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addShortArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putShortArray , FileFormat::getShortArray));
		return this;
		
	}

	/**
	 * Adds an array of ints and binds its reference for writing.
	 * 
	 * @param name — name of the int array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addIntArray(String name , int[] array) {
		
		verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 4); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name , FileFormat::putIntArray , FileFormat::getIntArray));
		return this;
		
	}

	/**
	 * Adds an array of ints with no bound reference.
	 * 
	 * @param name — name of the int array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addIntArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putIntArray , FileFormat::getIntArray));
		return this;
		
	}

	/**
	 * Adds an array of longs and binds its reference for writing.
	 * 
	 * @param name — name of the long array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addLongArray(String name , long[] array) {
		
		verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 8); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name , FileFormat::putLongArray , FileFormat::getLongArray));
		return this;
		
	}

	/**
	 * Adds an array of longs with no bound reference.
	 * 
	 * @param name — name of the long array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addLongArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putLongArray , FileFormat::getLongArray));
		return this;
		
	}

	/**
	 * Adds an array of floats and binds its reference for writing.
	 * 
	 * @param name — name of the float array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addFloatArray(String name , float[] array) {
		
		verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 4); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name , FileFormat::putFloatArray , FileFormat::getFloatArray));
		return this;
		
	}

	/**
	 * Adds an array of floats with no bound reference.
	 * 
	 * @param name — name of the float array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addFloatArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putFloatArray , FileFormat::getFloatArray));
		return this;
		
	}

	/**
	 * Adds an array of doubles and binds its reference for writing.
	 * 
	 * @param name — name of the double array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addDoubleArray(String name , double[] array) {
		
		verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 8); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name , FileFormat::putDoubleArray , FileFormat::getDoubleArray));
		return this;
		
	}

	/**
	 * Adds an array of doubles with no bound reference.
	 * 
	 * @param name — name of the double array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addDoubleArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putDoubleArray , FileFormat::getDoubleArray));
		return this;
		
	}

	/**
	 * Adds an array of booleans and binds its reference for writing.
	 * 
	 * @param name — name of the boolean array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addBooleanArray(String name , boolean[] array) {
		
		verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name , FileFormat::putBooleanArray , FileFormat::getBooleanArray));
		return this;
		
	}

	/**
	 * Adds an array of booleans with no bound reference.
	 * 
	 * @param name — name of the boolean array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addBooleanArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putBooleanArray , FileFormat::getBooleanArray));
		return this;
		
	}

	/**
	 * Adds an array of characters and binds its reference for writing.
	 * 
	 * @param name — name of the character array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addCharArray(String name , char[] array) {
		
		verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 2); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name ,FileFormat::putCharArray , FileFormat::getCharArray));
		return this;
		
	}

	/**
	 * Adds an array of characters with no bound reference.
	 * 
	 * @param name — name of the character array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addCharArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putCharArray , FileFormat::getCharArray));
		return this;
		
	}

	/**
	 * Adds an array of Strings and binds its reference for writing.
	 * 
	 * @param name — name of the String array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addStringArray(String name , String[] array) {
		
		verifyName(name); 
		int arraySize = listSizePrefixSize(array.length);
		for(String x : array) arraySize += listSizePrefixSize(x.length()) + x.length();
		fileSizeBytes += arraySize;
		data.add(new FileEntry2(array , arraySize , name , FileFormat::putStringArray , FileFormat::getStringArray));
		return this;
		
	}

	/**
	 * Adds an array of Strings with no bound reference.
	 * 
	 * @param name — name of the String array entry
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addStringArray(String name) {
		
		verifyName(name);
		data.add(new FileEntry2(name , FileFormat::putStringArray , FileFormat::getStringArray));
		return this;
		
	}
	
	/**
	 * Adds a generic data entry including a reference and size in bytes.
	 * 
	 * @param name — name of the entry
	 * @param data — reference to the entry
	 * @param sizeBytes — size in bytes this entry will take
	 * @param write — a write callback (optionally null if this is a read only {@code FileComponent}
	 * @param read — a read callback (optionally null if this is a write only {@code FileComponent}
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addData(
		String name , 
		final Object data , 
		final int sizeBytes , 
		BiConsumer<Object , ByteBuffer> write , 
		Function<ByteBuffer , Object> read
	) {
		
		verifyName(name) ; require(write) ; require(read);		
		fileSizeBytes += sizeBytes;		
		this.data.add(new FileEntry2(data , sizeBytes , name , write , read));		
		return this;
		
	}

	/**
	 * Adds a generic data entry without including a reference or size in bytes of the entry.
	 * 
	 * @param name — name of the entry
	 * @param write — a write callback (optionally null if this is a read only {@code FileComponent}
	 * @param read — a read callback (optionally null if this is a write only {@code FileComponent}
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addData(
		String name , 
		BiConsumer<Object , ByteBuffer> write , 
		Function<ByteBuffer , Object> read
	) {
		
		verifyName(name) ; require(write) ; require(read);		
		data.add(new FileEntry2(name , write, read));
		return this;
	
	}

	/**
	 * Adds a generic data entry without including a reference but including a size in bytes the entry occupies.
	 * 
	 * @param name — name of the entry
	 * @param sizeBytes — number of bytes this entry will use
	 * @param write — a write callback (optionally null if this is a read only {@code FileComponent}
	 * @param read — a read callback (optionally null if this is a write only {@code FileComponent}
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> addData(
		String name , 
		int sizeBytes , 
		BiConsumer<Object , ByteBuffer> write, 
		Function<ByteBuffer , Object> read
	) {
		
		verifyName(name) ; require(write) ; require(read);		
		data.add(new FileEntry2(sizeBytes , name , write, read));
		return this;
	
	}

	public FileStructureEntry addStructure(int repetitions , String name) {
		
		verifyName(name);
		FileStructureEntry entry = new FileStructureEntry(name);
		data.add(entry);
		return entry;
		
	}

	/**
	 * Binds a new value to the byte entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — byte value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindByte(String name , byte value) {
		
		
		return bindData(name , value , 1);
		
	}

	/**
	 * Binds a new value to the short entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — short value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindShort(String name , short value) {
		
		
		return bindData(name , value , 2);
		
	}

	/**
	 * Binds a new value to the int entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — int value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindInt(String name , int value) {
		
		
		return bindData(name , value , 4);
		
	}

	/**
	 * Binds a new value to the long entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — long value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindLong(String name , long value) {
		
		
		return bindData(name , value , 8);
		
	}

	/**
	 * Binds a new value to the float entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — float value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindFloat(String name , float value) {
		
		
		return bindData(name , value , 4);
		
	}

	/**
	 * Binds a new value to the double entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — double value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindDouble(String name , double value) {
		
		
		return bindData(name , value , 8);
		
	}

	/**
	 * Binds a new value to the boolean entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — boolean value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindBoolean(String name , boolean value) {
		
		
		return bindData(name , value , 1);
		
	}

	/**
	 * Binds a new value to the character entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — character value
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindChar(String name , char value) {
		
		
		return bindData(name , value , 2);
		
	}

	/**
	 * Binds a new reference to the String entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — String array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindString(String name , String value) {
		
		
		return bindData(name , value , value.length() + listSizePrefixSize(value.length()));
		
	}

	/**
	 * Binds a new reference to the byte array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — byte array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindByteArray(String name , byte[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + array.length);
		
	}

	/**
	 * Binds a new reference to the short array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — short array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindShortArray(String name , short[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 2));
		
	}

	/**
	 * Binds a new reference to the int array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — int array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindIntArray(String name , int[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 4));
		
	}

	/**
	 * Binds a new reference to the long array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — long array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindLongArray(String name , long[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 8));
		
	}

	/**
	 * Binds a new reference to the float array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — float array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindFloatArray(String name , float[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 4));
		
	}

	/**
	 * Binds a new reference to the double array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — double array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindDoubleArray(String name , double[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 8));
		
	}

	/**
	 * Binds a new reference to the boolean array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — boolean array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindBooleanArray(String name , boolean[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length));
		
	}

	/**
	 * Binds a new reference to the char array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — char array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindCharArray(String name , char[] array) {
		
		
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 2));
		
	}

	/**
	 * Binds a new reference to the String array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — String array reference
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindStringArray(String name , String[] array) {
		
		
		int size = listSizePrefixSize(array.length);
		for(String x : array) size += listSizePrefixSize(x.length()) + x.length();		
		return bindData(name , array , size);
		
	}

	/**
	 * Binds a new reference to the data entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — reference to an object
	 * @param sizeBytes — the total number of bytes this entry wil require
	 * @return {@code this}.
	 */
	public FileFormat<Writer , Reader> bindData(String name , Object data , int sizeBytes) {
		
		fileSizeBytes += sizeBytes;
		
		for(FileEntry2 x : this.data) if(x.name.equals(name)) { 
			
			x.object = data;
			x.sizeBytes = sizeBytes;
			return this;
			
		}
				
		System.err.println("Warning: " + name + " not found in this File Composition");

		return this;
	
	}
	
	/**
	 * Writes this File Composition to the file located at {@code filepath}.
	 * 
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void write() throws IOException {

		try(OutputStream stream = writer.get()) {
			
			write(stream);
			
		}
		
	}
		
	/**
	 * Writes this File Composition to the file open by {@code writer}.
	 * 
	 * @param writer — a file writer
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void write(final OutputStream writer) throws IOException {

		int finalFileSize = fileSizeBytes;
				
		ByteBuffer space = ByteBuffer.allocate(finalFileSize);		
		for(FileEntry2 x : data) x.write(space);
		
		if(space.hasArray()) writer.write(space.array());
		else {
			
			space.rewind();
			byte[] bytes = new byte[space.capacity()];
			int i = 0;
			while(space.hasRemaining()) bytes[i++] = space.get();
			writer.write(bytes);
						
		}
		
	}
	
	/**
	 * Reads the file located at {@code filepath} according to {@code this}.
	 * 
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void read() throws IOException {
		
		try(InputStream stream = reader.get()) {
			
			read(stream);
			
		}
		
	}

	/**
	 * Reads the file opened by {@code reader} according to {@code this}.
	 * 
	 * @param filepath — a file reader
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void read(InputStream reader) throws IOException {
		
		byte[] buffer = reader.readAllBytes();
		fileSizeBytes = buffer.length;
		ByteBuffer space = ByteBuffer.wrap(buffer);
		for(FileEntry2 x : data) x.read(space);
		
	}
	
	/**
	 * Writes the data entries of {@code this} and returns them.
	 * 
	 * @return {@code ByteBuffer} containing the bytes of the data entries of {@code this}.
	 */
	public ByteBuffer get() {
		
		return get(ByteBuffer.allocate(fileSizeBytes));
		
	}
	
	/**
	 * Writes the data entries of {@code this} into the given {@code ByteBuffer}.
	 * 
	 * @param destination — buffer to write to
	 * @return {@code destination} post writing.
	 */
	public ByteBuffer get(ByteBuffer destination) {
		
		specify(
			destination.remaining() >= fileSizeBytes , 
			"Destination Byte Buffer does not have enough space to buffer this File Composition."
		);
		
		destination.mark();
		for(FileEntry2 x : data) x.write(destination);
		destination.reset();
		
		return destination;
				
	}
	
	/**
	 * Returns the entry of the given name's object.
	 * 
	 * @param name — name of an entry
	 * @return Object reference to data located within the entry.
	 */
	public Object get(String name) {

		for(FileEntry2 x : data) if(x.name.equals(name)) return x.object;		
		
		throw new IllegalArgumentException("No file entry exists with name " + name);
		
	}
		
	/**
	 * Finds a file entry by the given name.
	 * 
	 * @param name — name of an entry within this File Composition
	 * @return The File Entry whose name is {@code name}.
	 */
	public FileEntry2 getEntry(String name) {
		
		for(FileEntry2 x : data) if(x.name.equals(name)) return x;		
		throw new IllegalArgumentException(name + " does not name an entry in this File Composition.");
		
	}

	private void verifyName(final String name) {
		
		specify(name , "The name of a data entry on a File Specification must not be null.");
		ensureNoDuplicates(name);
		
	}
	
	private void ensureNoDuplicates(final String name) {
		
		for(FileEntry2 x : data) specify(!name.equals(x.name) , "No duplicate named entries are allowed in a File Composition");
		
	}
	
	/**
	 * Returns the exact size in bytes the file written by this FileComposition will occupy, including the header if it is enabled.
	 * 
	 * @return Exact size in bytes of a file written using this file composition.
	 */
	public int fileSizeBytes() {
		
		return fileSizeBytes;
		
	}

	/**
	 * Iterates over the data entries attached to this FileComposition.
	 */
	@Override public Iterator<FileEntry2> iterator() {
		 
		return data.iterator();
		
	}
	
}