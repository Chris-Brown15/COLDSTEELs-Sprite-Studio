package cs.csss.misc.files;

import static cs.core.utils.CSUtils.require;
import static cs.core.utils.CSUtils.specify;
import static cs.csss.utils.UIUtils.toByte;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
 * <br><br>
 * Instances of this class are made of up of entries. These entries contain all that is needed to write an object to a file as binary.
 * {@code FileComposition}s can be used for writing files, but they can also be used to read files that were written with a 
 * {@code FileComposition}. This is why you can add entries to instances of this class without providing any other data in some cases. 
 * Depending on the type of data wrapped by the entry, it can be written or read automatically because this class provides standard ways to
 * write primitive types, the {@code String} type, and arrays of the primitive types and {@code String} type.
 * <br><br>
 * Instances of this class can also enforce an access mode. The can be one of: 
 * <ul>
 * 	<li>
 * 		{@code MODE_READ_WRITE}
 * 	</li>
 * 	<li>
 * 		{@code MODE_WRITE}
 * 	</li>
 * 	<li>
 * 		{@code MODE_READ}
 * 	</li>
 * </ul>
 *  
 *  If an operation that does not comport with the access mode is attempted, an error is thrown.
 *  
 * @author Chris Brown
 *
 */
public class FileComposition implements Iterable<cs.csss.misc.files.FileComposition.FileEntry> {

	/**
	 * Modes of this file composition. 
	 * 
	 */
	public static final int
		MODE_READ_WRITE = 0b11 ,
		MODE_WRITE		= 0b01 ,
		MODE_READ		= 0b10
	;

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
	public static final BiConsumer<Object , ByteBuffer> 
		writeByte = (byteValue , buffer) -> buffer.put((byte)byteValue) ,
		writeShort = (shortValue , buffer) -> buffer.putShort((short)shortValue) ,
		writeInt = (intValue , buffer) -> buffer.putInt((int)intValue) ,
		writeLong = (longValue , buffer) -> buffer.putLong((long)longValue) ,
		writeFloat = (floatValue , buffer) -> buffer.putFloat((float) floatValue),
		writeDouble = (doubleValue , buffer) -> buffer.putDouble((double)doubleValue),
		writeBoolean = (booleanValue , buffer) -> buffer.put((boolean)booleanValue ? (byte) 1 : 0),
		writeChar = (charValue , buffer) -> buffer.putChar((char)charValue) ,
		writeString = (stringValue , buffer) -> {
			
			byte[] bytes = ((String)stringValue).getBytes();
			writeSize(buffer , (bytes.length)).put(bytes);
			
		} ,
		writeByteArray = (byteArray , buffer) -> {
			
			byte[] asByteArray = (byte[])byteArray;
			writeSize(buffer , asByteArray.length).put(asByteArray);
						
		} ,
		writeShortArray = (shortArray , buffer) -> {
			
			short[] asShorts = (short[]) shortArray;
			writeSize(buffer , asShorts.length);
			for(short x : asShorts) buffer.putShort(x);
			
		} ,
		writeIntArray = (intArray , buffer) -> {
			
			int[] array = (int[]) intArray;
			writeSize(buffer , array.length);
			for(int x : array) buffer.putInt(x);
			
		} ,
		writeLongArray = (longArray , buffer) -> {
					
			long[] array = (long[]) longArray;
			writeSize(buffer , array.length);
			for(long x : array) buffer.putLong(x);
					
		} ,
		writeFloatArray = (floatArray , buffer) -> {
							
			float[] array = (float[]) floatArray;
			writeSize(buffer , array.length);
			for(float x : array) buffer.putFloat(x);
						
		} ,
		writeDoubleArray = (doubleArray , buffer) -> {
									
			double[] array = (double[]) doubleArray;
			writeSize(buffer , array.length);
			for(double x : array) buffer.putDouble(x);
									
		} ,
		writeBooleanArray = (booleanArray , buffer) -> {
											
			boolean[] array = (boolean[]) booleanArray;
			writeSize(buffer , array.length);
			for(boolean x : array) buffer.put(toByte(x));
											
		} ,
		writeCharArray = (charArray , buffer) -> {
													
			char[] array = (char[]) charArray;
			writeSize(buffer , array.length);
			for(char x : array) buffer.putChar(x);
													
		} ,
		writeStringArray = (stringArray , buffer) -> {
															
			String[] array = (String[]) stringArray;
			writeSize(buffer , array.length);
			for(String x : array) writeString.accept(x, buffer);
															
		} 
	;
	
	/**
	 * Default means of reading data from a byte buffer for the primitive classes and {@code String}.
	 */
	public static final Function<ByteBuffer , Object>
		readByte = buffer -> buffer.get() ,
		readShort = buffer -> buffer.getShort() ,
		readInt = buffer -> buffer.getInt() ,
		readLong = buffer -> buffer.getLong() , 
		readFloat = buffer -> buffer.getFloat() ,
		readDouble = buffer -> buffer.getDouble() ,
		readBoolean = buffer -> buffer.get() == 1 ? true : false ,
		readChar = buffer -> buffer.getChar() ,
		readString = buffer -> {
			
			byte[] bytes = new byte[readSize(buffer)];
			for(int i = 0 ; i < bytes.length ; i++) bytes[i] = buffer.get();
			return new String(bytes);
					
		} ,
		readByteArray = buffer -> {
		
			byte[] array = new byte[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.get();
			return array;
			
		} ,
		readShortArray = buffer -> {
			
			short[] array = new short[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getShort();
			return array;
			
		} ,
		readIntArray = buffer -> {
			
			int[] array = new int[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getInt();
			return array;
			
		} ,
		readLongArray = buffer -> {
			
			long[] array = new long[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getLong();
			return array;
			
		} ,
		readFloatArray = buffer -> {
			
			float[] array = new float[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getFloat();
			return array;
			
		} ,
		readDoubleArray = buffer -> {
			
			double[] array = new double[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getDouble();
			return array;
			
		} ,
		readBooleanArray = buffer -> {
			
			boolean[] array = new boolean[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.get() == 1 ? true : false;
			return array;
			
		} ,
		readCharArray = buffer -> {
			
			char[] array = new char[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = buffer.getChar();
			return array;
			
		} ,
		readStringArray = buffer -> {
			
			String[] array = new String[readSize(buffer)];
			for(int i = 0 ; i < array.length ; i++) array[i] = (String) readString.apply(buffer);
			return array;
			
		}
	;

	/*
	 * The following 3 methods are used to compute the number of bytes to precede a list notating the list's size. File Composition stores
	 * list lengths preceding their contents in files. Instead of using a flat 4 byte size value, we choose a size as a function of the 
	 * number of elements in the list. This means the preceding size is [1 , 4]. This also means that we lose out on a significant number
	 * of elements a single list can store because the highest two bits are used to store the number of bytes the size will take up.
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
	
	private final LinkedList<FileEntry> data = new LinkedList<>(); 
	
	public int mode = MODE_READ_WRITE;
	
	private int fileSizeBytes = 0;
	
	protected FileComposition(FileComposition source) {
		
		source.data.forEach(entry -> data.add(new FileEntry(entry)));
		this.mode = source.mode;
		this.fileSizeBytes = source.fileSizeBytes;
		
	}
	
	public FileComposition() {}
	
	public FileComposition(int mode) {
		
		specify(mode <= MODE_READ_WRITE && mode > 0 , "Invalid Mode Parameter");
		this.mode = mode;
		
	}
	
	/**
	 * Adds a byte and binds its value for writing.
	 * 
	 * @param name — name of the byte entry
	 * @param value — value of the byte
	 * @return {@code this}.
	 */
	public FileComposition addByte(String name , byte value) { 
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry(value , 1 , name , writeByte , readByte));	
		return this;
		
	}
	
	/**
	 * Adds a byte and relies on a supplier for its value.
	 * 
	 * @param name — name of the byte entry
	 * @param byteGetter — getter
	 * @return {@code this}.
	 */
	public FileComposition addByte(String name , ByteSupplier byteGetter) { 
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry(1 , () -> byteGetter.getAsByte() , name , writeByte , readByte));	
		return this;
		
	}
	
	/**
	 * Adds a byte entry with no bound value.
	 * 
	 * @param name — name of the byte entry
	 * @return {@code this}.
	 */
	public FileComposition addByte(String name) {

		verifyMode(MODE_READ) ; verifyName(name);		
		data.add(new FileEntry(name , writeByte, readByte));		
		return this;
		
	}

	/**
	 * Adds a short and binds its value for writing.
	 * 
	 * @param name — name of the short entry
	 * @param value — value of the short
	 * @return {@code this}.
	 */
	public FileComposition addShort(String name , short value) {

		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 2;		
		data.add(new FileEntry(value , 2 , name , writeShort , readShort));
		return this;
	
	}

	/**
	 * Adds a short and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param shortGetter — getter
	 * @return {@code this}.
	 */
	public FileComposition addShort(String name , ShortSupplier shortGetter) {

		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 2;		
		data.add(new FileEntry(2 , () -> shortGetter.getAsShort() , name , writeShort , readShort));
		return this;
	
	}

	/**
	 * Adds a short entry with no bound value.
	 * 
	 * @param name — name of the short entry
	 * @return {@code this}.
	 */
	public FileComposition addShort(String name) {

		verifyMode(MODE_READ) ; verifyName(name);	
		data.add(new FileEntry(name , writeShort , readShort));		
		return this;
		
	}

	/**
	 * Adds an int and binds its value for writing.
	 * 
	 * @param name — name of the int entry
	 * @param value — value of the int
	 * @return {@code this}.
	 */
	public FileComposition addInt(String name , int value) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 4;		
		data.add(new FileEntry(value , 4 , name , writeInt , readInt));
		return this;
	
	}

	/**
	 * Adds an int and relies on a supplier for its value.
	 * 
	 * @param name — name of the int entry
	 * @param intGetter — getter
	 * @return {@code this}.
	 */
	public FileComposition addInt(String name , IntSupplier intGetter) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 4;		
		data.add(new FileEntry(4 , () -> intGetter.getAsInt() , name , writeInt , readInt));
		return this;
	
	}

	/**
	 * Adds an int entry with no bound value.
	 * 
	 * @param name — name of the int entry
	 * @return {@code this}.
	 */
	public FileComposition addInt(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);		
		data.add(new FileEntry(name , writeInt , readInt));		
		return this;
		
	}

	/**
	 * Adds a long and binds its value for writing.
	 * 
	 * @param name — name of the long entry
	 * @param value — value of the long
	 * @return {@code this}.
	 */
	public FileComposition addLong(String name , long value) {

		verifyMode(MODE_WRITE) ; verifyName(name);
		fileSizeBytes += 8;
		data.add(new FileEntry(value , 8 , name , writeLong , readLong));
		return this;
	
	}

	/**
	 * Adds a long and relies on a supplier for its value.
	 * 
	 * @param name — name of the long entry
	 * @param longGetter — getter
	 * @return {@code this}.
	 */
	public FileComposition addLong(String name , LongSupplier longGetter) {

		verifyMode(MODE_WRITE) ; verifyName(name);
		fileSizeBytes += 8;
		data.add(new FileEntry(8 , () -> longGetter.getAsLong() , name , writeLong , readLong));
		return this;
	
	}

	/**
	 * Adds a long entry with no bound value.
	 * 
	 * @param name — name of the long entry
	 * @return {@code this}.
	 */
	public FileComposition addLong(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeLong , readLong));
		return this;
		
	}

	/**
	 * Adds a float and binds its value for writing.
	 * 
	 * @param name — name of the float entry
	 * @param value — value of the float
	 * @return {@code this}.
	 */
	public FileComposition addFloat(String name , float value) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 4;				
		data.add(new FileEntry(value , 4 , name , writeFloat , readFloat));
		return this;
	
	}

	/**
	 * Adds a float and relies on a supplier for its value.
	 * 
	 * @param name — name of the float entry
	 * @param value — value of the float
	 * @return {@code this}.
	 */
	public FileComposition addFloat(String name , FloatSupplier floatGetter) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 4;				
		data.add(new FileEntry(4 , () -> floatGetter.getAsFloat() , name , writeFloat , readFloat));
		return this;
	
	}

	/**
	 * Adds a float entry with no bound value.
	 * 
	 * @param name — name of the float entry
	 * @return {@code this}.
	 */
	public FileComposition addFloat(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeFloat , readFloat));
		return this;
		
	}

	/**
	 * Adds a double and binds its value for writing.
	 * 
	 * @param name — name of the double entry
	 * @param value — value of the double
	 * @return {@code this}.
	 */
	public FileComposition addDouble(String name , double value) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		fileSizeBytes += 8;				
		data.add(new FileEntry(value , 8 , name , writeDouble , readDouble));
		return this;
	
	}

	/**
	 * Adds a double and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param doubleGetter — getter
	 * @return {@code this}.
	 */
	public FileComposition addDouble(String name , DoubleSupplier doubleGetter) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		fileSizeBytes += 8;				
		data.add(new FileEntry(8 , () -> doubleGetter.getAsDouble() , name , writeDouble , readDouble));
		return this;
	
	}

	/**
	 * Adds a double entry with no bound value.
	 * 
	 * @param name — name of the double entry
	 * @return {@code this}.
	 */
	public FileComposition addDouble(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeDouble , readDouble));
		return this;
	
	}

	/**
	 * Adds a boolean and binds its value for writing.
	 * 
	 * @param name — name of the boolean entry
	 * @param value — value of the boolean
	 * @return {@code this}.
	 */
	public FileComposition addBoolean(String name , boolean value) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry(value , 1 , name , writeBoolean , readBoolean));		
		return this;
		
	}

	/**
	 * Adds a boolean and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param booleanGetter — getter
	 * @return {@code this}.
	 */
	public FileComposition addBoolean(String name , BooleanSupplier booleanGetter) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 1;		
		data.add(new FileEntry(1 , () -> booleanGetter.getAsBoolean() , name , writeBoolean , readBoolean));		
		return this;
		
	}

	/**
	 * Adds a boolean entry with no bound value.
	 * 
	 * @param name — name of the boolean entry
	 * @return {@code this}.
	 */
	public FileComposition addBoolean(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);		
		data.add(new FileEntry(name , writeBoolean , readBoolean));		
		return this;
		
	}

	/**
	 * Adds a character and binds its value for writing.
	 * 
	 * @param name — name of the character entry
	 * @param value — value of the character
	 * @return {@code this}.
	 */
	public FileComposition addCharacter(String name , char value) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 2;				
		data.add(new FileEntry(value , 2 , name , writeChar , readChar));		
		return this;
		
	}

	/**
	 * Adds a character and relies on a supplier for its value.
	 * 
	 * @param name — name of the short entry
	 * @param characterGetter — getter
	 * @return {@code this}.
	 */
	public FileComposition addCharacter(String name , CharSupplier charGetter) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);		
		fileSizeBytes += 2;				
		data.add(new FileEntry(2 , () -> charGetter.getAsChar() , name , writeChar , readChar));		
		return this;
		
	}

	/**
	 * Adds a character entry with no bound value.
	 * 
	 * @param name — name of the character entry
	 * @return {@code this}.
	 */
	public FileComposition addCharacter(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);		
		data.add(new FileEntry(name , writeChar , readChar));		
		return this;
		
	}

	/**
	 * Adds a String and binds its reference for writing.
	 * 
	 * @param name — name of the String entry
	 * @param value — reference of the String
	 * @return {@code this}.
	 */
	public FileComposition addString(String name , String value) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);	
		int prefixSize = listSizePrefixSize(value.length());
		int dataSize = (value.length() + prefixSize); 		
		fileSizeBytes += dataSize;		
		data.add(new FileEntry(value , dataSize , name , writeString , readString));
		return this;
	
	}

	/**
	 * Adds a String entry with no bound reference.
	 * 
	 * @param name — name of the String entry
	 * @return {@code this}.
	 */
	public FileComposition addString(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeString , readString));
		return this;
		
	}

	/**
	 * Adds a composable entry and binds its reference for reading or writing.
	 * 
	 * @param name — name of the Composable entry
	 * @param composable — instance of {@code Composable}
	 * @return {@code this}.
	 */
	public FileComposition addData(String name , Composable composable) {
		
		verifyName(name);		
		data.add(new FileEntry(name , composable));	
		return this;
		
	}

	/**
	 * Adds a composable entry and binds its reference for reading or writing.
	 * 
	 * @param name — name of the Composable entry
	 * @param getter — function returning an instance of a Composable
	 * @return {@code this}.
	 */
	public FileComposition addData(String name , Supplier<Composable> getter) {
		
		verifyName(name);		
		data.add(new FileEntry(name , getter));	
		return this;
		
	}
	
	/**
	 * Adds an array of bytes and binds its reference for writing.
	 * 
	 * @param name — name of the byte array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addByteArray(String name , byte[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int prefixSize = listSizePrefixSize(array.length);
		fileSizeBytes += prefixSize + array.length;
		data.add(new FileEntry(array , prefixSize + array.length , name , writeByteArray , readByteArray));
		return this;
		
	}

	/**
	 * Adds an array of bytes with no bound reference.
	 * 
	 * @param name — name of the byte array entry
	 * @return {@code this}.
	 */
	public FileComposition addByteArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeByteArray , readByteArray));
		return this;
		
	}

	/**
	 * Adds an array of shorts and binds its reference for writing.
	 * 
	 * @param name — name of the short array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addShortArray(String name , short[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 2); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeShortArray , readShortArray));
		return this;
		
	}

	/**
	 * Adds an array of shorts with no bound reference.
	 * 
	 * @param name — name of the short array entry
	 * @return {@code this}.
	 */
	public FileComposition addShortArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeShortArray , readShortArray));
		return this;
		
	}

	/**
	 * Adds an array of ints and binds its reference for writing.
	 * 
	 * @param name — name of the int array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addIntArray(String name , int[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 4); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeIntArray , readIntArray));
		return this;
		
	}

	/**
	 * Adds an array of ints with no bound reference.
	 * 
	 * @param name — name of the int array entry
	 * @return {@code this}.
	 */
	public FileComposition addIntArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeIntArray , readIntArray));
		return this;
		
	}

	/**
	 * Adds an array of longs and binds its reference for writing.
	 * 
	 * @param name — name of the long array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addLongArray(String name , long[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 8); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeLongArray , readLongArray));
		return this;
		
	}

	/**
	 * Adds an array of longs with no bound reference.
	 * 
	 * @param name — name of the long array entry
	 * @return {@code this}.
	 */
	public FileComposition addLongArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeLongArray , readLongArray));
		return this;
		
	}

	/**
	 * Adds an array of floats and binds its reference for writing.
	 * 
	 * @param name — name of the float array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addFloatArray(String name , float[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 4); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeFloatArray , readFloatArray));
		return this;
		
	}

	/**
	 * Adds an array of floats with no bound reference.
	 * 
	 * @param name — name of the float array entry
	 * @return {@code this}.
	 */
	public FileComposition addFloatArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeFloatArray , readFloatArray));
		return this;
		
	}

	/**
	 * Adds an array of doubles and binds its reference for writing.
	 * 
	 * @param name — name of the double array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addDoubleArray(String name , double[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 8); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeDoubleArray , readDoubleArray));
		return this;
		
	}

	/**
	 * Adds an array of doubles with no bound reference.
	 * 
	 * @param name — name of the double array entry
	 * @return {@code this}.
	 */
	public FileComposition addDoubleArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeDoubleArray , readDoubleArray));
		return this;
		
	}

	/**
	 * Adds an array of booleans and binds its reference for writing.
	 * 
	 * @param name — name of the boolean array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addBooleanArray(String name , boolean[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeBooleanArray , readBooleanArray));
		return this;
		
	}

	/**
	 * Adds an array of booleans with no bound reference.
	 * 
	 * @param name — name of the boolean array entry
	 * @return {@code this}.
	 */
	public FileComposition addBooleanArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeBooleanArray , readBooleanArray));
		return this;
		
	}

	/**
	 * Adds an array of characters and binds its reference for writing.
	 * 
	 * @param name — name of the character array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addCharArray(String name , char[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name);
		int arraySize = listSizePrefixSize(array.length) + (array.length * 2); 
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeCharArray , readCharArray));
		return this;
		
	}

	/**
	 * Adds an array of characters with no bound reference.
	 * 
	 * @param name — name of the character array entry
	 * @return {@code this}.
	 */
	public FileComposition addCharArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeCharArray , readCharArray));
		return this;
		
	}

	/**
	 * Adds an array of Strings and binds its reference for writing.
	 * 
	 * @param name — name of the String array entry
	 * @param value — reference of the array
	 * @return {@code this}.
	 */
	public FileComposition addStringArray(String name , String[] array) {
		
		verifyMode(MODE_WRITE) ; verifyName(name); 
		int arraySize = listSizePrefixSize(array.length);
		for(String x : array) arraySize += listSizePrefixSize(x.length()) + x.length();
		fileSizeBytes += arraySize;
		data.add(new FileEntry(array , arraySize , name , writeStringArray , readStringArray));
		return this;
		
	}

	/**
	 * Adds an array of Strings with no bound reference.
	 * 
	 * @param name — name of the String array entry
	 * @return {@code this}.
	 */
	public FileComposition addStringArray(String name) {
		
		verifyMode(MODE_READ) ; verifyName(name);
		data.add(new FileEntry(name , writeStringArray , readStringArray));
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
	public FileComposition addData(
		String name , 
		final Object data , 
		final int sizeBytes , 
		BiConsumer<Object , ByteBuffer> write , 
		Function<ByteBuffer , Object> read
	) {
		
		verifyMode(MODE_WRITE) ; verifyName(name) ; require(write) ; require(read);		
		fileSizeBytes += sizeBytes;		
		this.data.add(new FileEntry(data , sizeBytes , name , write , read));		
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
	public FileComposition addData(String name , BiConsumer<Object , ByteBuffer> write, Function<ByteBuffer , Object> read) {
		
		verifyMode(MODE_READ) ; verifyName(name) ; require(write) ; require(read);		
		data.add(new FileEntry(name , write, read));
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
	public FileComposition addData(
		String name , 
		int sizeBytes , 
		BiConsumer<Object , ByteBuffer> write, 
		Function<ByteBuffer , Object> read
	) {
		
		verifyMode(MODE_WRITE) ; verifyName(name) ; require(write) ; require(read);		
		data.add(new FileEntry(sizeBytes , name , write, read));
		return this;
	
	}
	
	/**
	 * Binds a new value to the byte entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — byte value
	 * @return {@code this}.
	 */
	public FileComposition bindByte(String name , byte value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 1);
		
	}

	/**
	 * Binds a new value to the short entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — short value
	 * @return {@code this}.
	 */
	public FileComposition bindShort(String name , short value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 2);
		
	}

	/**
	 * Binds a new value to the int entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — int value
	 * @return {@code this}.
	 */
	public FileComposition bindInt(String name , int value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 4);
		
	}

	/**
	 * Binds a new value to the long entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — long value
	 * @return {@code this}.
	 */
	public FileComposition bindLong(String name , long value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 8);
		
	}

	/**
	 * Binds a new value to the float entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — float value
	 * @return {@code this}.
	 */
	public FileComposition bindFloat(String name , float value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 4);
		
	}

	/**
	 * Binds a new value to the double entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — double value
	 * @return {@code this}.
	 */
	public FileComposition bindDouble(String name , double value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 8);
		
	}

	/**
	 * Binds a new value to the boolean entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — boolean value
	 * @return {@code this}.
	 */
	public FileComposition bindBoolean(String name , boolean value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 1);
		
	}

	/**
	 * Binds a new value to the character entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — character value
	 * @return {@code this}.
	 */
	public FileComposition bindChar(String name , char value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , 2);
		
	}

	/**
	 * Binds a new reference to the String entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — String array reference
	 * @return {@code this}.
	 */
	public FileComposition bindString(String name , String value) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , value , value.length() + listSizePrefixSize(value.length()));
		
	}

	/**
	 * Binds a new reference to the byte array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — byte array reference
	 * @return {@code this}.
	 */
	public FileComposition bindByteArray(String name , byte[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + array.length);
		
	}

	/**
	 * Binds a new reference to the short array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — short array reference
	 * @return {@code this}.
	 */
	public FileComposition bindShortArray(String name , short[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 2));
		
	}

	/**
	 * Binds a new reference to the int array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — int array reference
	 * @return {@code this}.
	 */
	public FileComposition bindIntArray(String name , int[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 4));
		
	}

	/**
	 * Binds a new reference to the long array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — long array reference
	 * @return {@code this}.
	 */
	public FileComposition bindLongArray(String name , long[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 8));
		
	}

	/**
	 * Binds a new reference to the float array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — float array reference
	 * @return {@code this}.
	 */
	public FileComposition bindFloatArray(String name , float[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 4));
		
	}

	/**
	 * Binds a new reference to the double array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — double array reference
	 * @return {@code this}.
	 */
	public FileComposition bindDoubleArray(String name , double[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 8));
		
	}

	/**
	 * Binds a new reference to the boolean array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — boolean array reference
	 * @return {@code this}.
	 */
	public FileComposition bindBooleanArray(String name , boolean[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length));
		
	}

	/**
	 * Binds a new reference to the char array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — char array reference
	 * @return {@code this}.
	 */
	public FileComposition bindCharArray(String name , char[] array) {
		
		verifyMode(MODE_WRITE); 
		return bindData(name , array , listSizePrefixSize(array.length) + (array.length * 2));
		
	}

	/**
	 * Binds a new reference to the String array entry given by {@code name}.
	 * 
	 * @param name — name of an entry
	 * @param value — String array reference
	 * @return {@code this}.
	 */
	public FileComposition bindStringArray(String name , String[] array) {
		
		verifyMode(MODE_WRITE); 
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
	public FileComposition bindData(String name , Object data , int sizeBytes) {
		
		verifyMode(MODE_WRITE); 
		fileSizeBytes += sizeBytes;
		
		for(FileEntry x : this.data) if(x.name.equals(name)) { 
			
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
	 * @param filePath — filepath of a write location
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void write(String filePath) throws IOException {

		verifyMode(MODE_WRITE);
		
		try(FileOutputStream writer = new FileOutputStream(filePath)) {
			
			write(writer);
			
		}
		
	}
	
	/**
	 * Writes this File Composition to the file open by {@code writer}.
	 * 
	 * @param writer — a file writer
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void write(final FileOutputStream writer) throws IOException {

		verifyMode(MODE_WRITE);
		
		ByteBuffer space = ByteBuffer.allocate(fileSizeBytes);
		for(FileEntry x : data) x.write(space);
		
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
	 * @param filepath — a file path
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void read(String filepath) throws IOException {
		
		try(FileInputStream reader = new FileInputStream(filepath)) {
			
			read(reader);
			
		}
		
	}

	/**
	 * Reads the file opened by {@code reader} according to {@code this}.
	 * 
	 * @param filepath — a file reader
	 * @throws IOException if the IO operations of this method fail.
	 */
	public void read(FileInputStream reader) throws IOException {
		
		byte[] buffer = reader.readAllBytes();
		fileSizeBytes = buffer.length;
		ByteBuffer space = ByteBuffer.allocate(fileSizeBytes).put(buffer);
		space.flip();		
		for(FileEntry x : data) x.read(space);
		
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
		for(FileEntry x : data) x.write(destination);
		destination.reset();
		
		return destination;
				
	}
	
	public Object get(String name) {

		for(FileEntry x : data) if(x.name.equals(name)) return x.object;		
		
		throw new IllegalArgumentException("No file entry exists with name " + name);
		
	}
		
	public FileEntry getEntry(String name) {
		
		for(FileEntry x : data) if(x.name.equals(name)) return x;		
		return null;
		
	}
	
	private void verifyName(final String name) {
		
		specify(name , "The name of a data entry on a File Specification must not be null.");
		ensureNoDuplicates(name);
		
	}
	
	private void verifyMode(int requiredMode) {
		
		specify((mode & requiredMode) == requiredMode , "The requested action requires a different access mode.");
		
	}
	
	private void ensureNoDuplicates(final String name) {
		
		for(FileEntry x : data) specify(!name.equals(x.name) , "No duplicate named entries are allowed in a File Composition");
		
	}
	
	/**
	 * Returns the exact size in bytes the file written by this FileComposition will occupy.
	 * 
	 * @return
	 */
	public int fileSizeBytes() {
		
		return fileSizeBytes;
		
	}

	/**
	 * Iterates over the data entries attached to this FileComposition.
	 */
	@Override public Iterator<FileEntry> iterator() {
		 
		return data.iterator();
		
	}
		
	/**
	 * Container for individual data entries of the file.
	 */
	public class FileEntry {
		
		//object to compose 
		private Object object;
		//name of this entry
		public final String name;
		//size of this entry in bytes
		private int sizeBytes = -1;

		//read and write functions
		private BiConsumer<Object , ByteBuffer> write;
		private Function<ByteBuffer , Object> read;
		
		//getter of object. This is prioritized over the object member of this class.
		private Supplier<Object> getter = null;
		
		private FileEntry(FileEntry copyThis) {
			
			this.object = copyThis.object;
			this.name = copyThis.name;
			this.sizeBytes = copyThis.sizeBytes;
			this.write = copyThis.write;
			this.read = copyThis.read;
			this.getter = copyThis.getter;
			
		}
		
		private FileEntry(
			final String name , 
			final BiConsumer<Object , ByteBuffer> write , 
			final Function<ByteBuffer , Object> read
		) {
			
			this.name = name;
			this.write = write;
			this.read = read;
			
		}

		private FileEntry(
			Object object , 
			int sizeBytes ,
			final String name , 
			final BiConsumer<Object , ByteBuffer> write , 
			final Function<ByteBuffer , Object> read
		) {
			
			this.sizeBytes = sizeBytes;
			this.object = object;
			this.name = name;
			this.write = write;
			this.read = read;
			
		}

		private FileEntry(
			int sizeBytes ,
			Supplier<Object> getter , 
			final String name , 
			final BiConsumer<Object , ByteBuffer> write , 
			final Function<ByteBuffer , Object> read
		) {
			
			this.sizeBytes = sizeBytes;
			this.getter = getter;
			this.name = name;
			this.write = write;
			this.read = read;
			
		}

		private FileEntry(
			int sizeBytes ,
			final String name , 
			final BiConsumer<Object , ByteBuffer> write , 
			final Function<ByteBuffer , Object> read
		) {
			
			this.sizeBytes = sizeBytes;			
			this.name = name;
			this.write = write;
			this.read = read;
			
		}
		
		private FileEntry(String name , Composable composable) {
			
			this(composable , composable.sizeBytes() , name , (obj , buffer) -> composable.write(buffer) , composable::read);
			
		}
		
		private FileEntry(String name , Supplier<Composable> getter) {
			
			this(getter , getter.get().sizeBytes() , name , (obj , buffer) ->  getter.get().write(buffer) , getter.get()::read);
			
		}
				
		/**
		 * Gets the object contained within this file entry. Note that the returned object will be the one gotten from this object's getter
		 * if the getter is not null and the mode's {@code WRITE} bit is set, otherwise it will return the object.
		 * 
		 * @return The object contained within this file entry.
		 */
		public Object object() {
			
			return getter != null && (mode & MODE_WRITE) != 0 ? getter.get() : object;
			
		}
		
		public void write(ByteBuffer buffer) {

			if(getter != null) object = getter.get();
			
			specify(object , "Data must be bound before writing a component of a File Composition.");
			
			int bufferInitialPosition = buffer.position();
			write.accept(object, buffer);
			
			specify(
				bufferInitialPosition + sizeBytes == buffer.position() , 
				"Invalid write operation: " + sizeBytes + " is " + name + "'s size in bytes, but " + 
				(buffer.position() - bufferInitialPosition) + " bytes were written by " + name + "'s write function."
			);
			
		}
		
		public void read(ByteBuffer buffer) {
		
			int bufferInitialPosition = buffer.position();			
			object = read.apply(buffer);
			sizeBytes = buffer.position() - bufferInitialPosition;

		}
		
		/**
		 * Attempts to return a string representation of the object this FileData contains as accurately as possible.
		 */
		@Override public String toString() {
			
			String objectString;
			
			if(object != null) {
				
				if(!object.getClass().isArray()) objectString = object.toString();
				else {
					
					if(object instanceof byte[]) objectString = Arrays.toString((byte[]) object);
					else if (object instanceof short[]) objectString = Arrays.toString((short[]) object);
					else if (object instanceof int[]) objectString = Arrays.toString((int[]) object);
					else if (object instanceof long[]) objectString = Arrays.toString((long[]) object);
					else if (object instanceof float[]) objectString = Arrays.toString((float[]) object);
					else if (object instanceof double[]) objectString = Arrays.toString((double[]) object);
					else if (object instanceof boolean[]) objectString = Arrays.toString((boolean[]) object);
					else if (object instanceof char[]) objectString = Arrays.toString((char[]) object);
					else objectString = Arrays.toString((Object[]) object);
					
				}
				
			} else objectString = "Null";
			
			return name + " => " + objectString;
			
		}
				
	}

}