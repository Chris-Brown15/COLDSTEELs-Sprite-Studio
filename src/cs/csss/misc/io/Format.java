package cs.csss.misc.io;

import java.nio.ByteBuffer;

/**
 * Interface for all objects that are pieces of a file.
 * 
 * <p>
 * 	Chunks and lists are {@code Format}s. {@code Format}s define add and get methods for the primitive types, {@code CharSequence} and 
 * 	arrays of the primitives and {@code CharSequence}.  
 * </p>
 * <p>
 * 	This interface also provides methods for efficiently encoding and reading array sizes in files. For this, the highest two bits of the
 * 	first byte of an array entry will store the number of additional bytes after the current one used to store the size of the list. If the
 * 	highest two bits are nonzero, they are masked out and the additional bytes are composed into a larger number for the size of the list. 
 * </p>
 * 
 * @author Chris Brown
 */
public interface Format {

	/*
	 * Used for list prefix size calculations.
	 */		
	int
		maxValue6Bits  = 0b111111 ,
		maxValue14Bits = 0b11111111111111 ,
		maxValue22Bits = 0b1111111111111111111111 ,
		maxValue30Bits = 0b111111111111111111111111111111
	;		

	/*
	 * The following 3 methods are used to compute the number of bytes to precede a list notating the list's size. Formats stores
	 * list lengths preceding their contents in files. Instead of using a flat 4 byte list size value for all lists, we choose a size as a 
	 * based on the number of elements in the list by using the two highest bits to store the number of additional bytes used to store the 
	 * list length.
	 * 
	 * With two bits, the greatest value we can store is 3. So at least one byte is always used to store the list size. The last two bits
	 * of this number contain the amount of bytes to read forward and compose into a list size in elements. Since the user will know the
	 * types of elements in an array, it is acceptable to store the elements of the list rather than the bytes of the list.
	 *
	 * The method below the following 3 reads the list size prefix from a buffer and returns the number of elements of some list that 
	 * follows.
	 *
	 */
	
	/**
	 * Returns the total number of bytes a list prefix will be for a list of size {@code listLength}.
	 * 
	 * @param listLength — arbitrary size of a list
	 * @return Number of bytes a prefix to a list of size {@code listLength} would be. 
	 */
	public static int sizeOfListSizePrefix(int listLength) {
		
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
	
	public Format addByte(String entryName , byte value);
	public Format addShort(String entryName , short value);
	public Format addInt(String entryName , int value);
	public Format addLong(String entryName , long value);
	public Format addFloat(String entryName , float value);
	public Format addDouble(String entryName , double value);
	public Format addBoolean(String entryName , boolean value);
	public Format addChar(String entryName , char value);
	public Format addString(String entryName , CharSequence value);
	
	public Format addByte(String entryName);
	public Format addShort(String entryName);
	public Format addInt(String entryName);
	public Format addLong(String entryName);
	public Format addFloat(String entryName);
	public Format addDouble(String entryName);
	public Format addBoolean(String entryName);
	public Format addChar(String entryName);
	public Format addString(String entryName);
	
	public Format addBytes(String entryName , byte[] value);
	public Format addShorts(String entryName , short[] value);
	public Format addInts(String entryName , int[] value);
	public Format addLongs(String entryName , long[] value);
	public Format addFloats(String entryName , float[] value);
	public Format addDoubles(String entryName , double[] value);
	public Format addBooleans(String entryName , boolean[] value);
	public Format addChars(String entryName , char[] value);
	public Format addStrings(String entryName , CharSequence[] value);
	
	public Format addBytes(String entryName);
	public Format addShorts(String entryName);
	public Format addInts(String entryName);
	public Format addLongs(String entryName);
	public Format addFloats(String entryName);
	public Format addDoubles(String entryName);
	public Format addBooleans(String entryName);
	public Format addChars(String entryName);
	public Format addStrings(String entryName);

	public byte getByte(String entryName);
	public short getShort(String entryName);
	public int getInt(String entryName);
	public long getLong(String entryName);
	public float getFloat(String entryName);
	public double getDouble(String entryName);
	public boolean getBoolean(String entryName);
	public char getChar(String entryName);
	public CharSequence getString(String entryName);
	
	public byte[] getByteArray(String entryName);   
	public short[] getShortArray(String entryName);  
	public int[] getIntArray(String entryName);    
	public long[] getLongArray(String entryName);   
	public float[] getFloatArray(String entryName);  
	public double[] getDoubleArray(String entryName); 
	public boolean[] getBooleanArray(String entryName);
	public char[] getCharArray(String entryName);   
	public CharSequence[] getStringArray(String entryName); 

	public Format setByte(String entryName , byte value);
	public Format setShort(String entryName , short value);
	public Format setInt(String entryName , int value);
	public Format setLong(String entryName , long value);
	public Format setFloat(String entryName , float value);
	public Format setDouble(String entryName , double value);
	public Format setBoolean(String entryName , boolean value);
	public Format setChar(String entryName , char value);
	public Format setString(String entryName , CharSequence value);

	public Format setBytes(String entryName , byte[] value);
	public Format setShorts(String entryName , short[] value);
	public Format setInts(String entryName , int[] value);
	public Format setLongs(String entryName , long[] value);
	public Format setFloats(String entryName , float[] value);
	public Format setDoubles(String entryName , double[] value);
	public Format setBooleans(String entryName , boolean[] value);
	public Format setChars(String entryName , char[] value);
	public Format setStrings(String entryName , CharSequence[] value);
		
}
