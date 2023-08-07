package cs.csss.misc.nio;

import static cs.core.utils.CSUtils.require;

public interface FileFormat {

	byte
		BYTE = 0 ,
		SHORT = 1 , 
		INT = 2 ,
		LONG = 3 ,
		FLOAT = 4 ,
		DOUBLE = 5 , 
		BOOLEAN = 6 , 
		CHAR = 7 , 
		STRING = 8 ,
		BYTE_ARRAY = 9 ,
		SHORT_ARRAY = 10 ,
		INT_ARRAY = 11 ,
		LONG_ARRAY = 12 ,
		FLOAT_ARRAY = 13 ,
		DOUBLE_ARRAY = 14 ,
		BOOLEAN_ARRAY = 15 ,
		CHAR_ARRAY = 16 ,
		STRING_ARRAY = 17 ,
		EMPTY = 18 ,
		REPETITION = 19;
	
	static void verifyType(byte type) {
		
		require(type >= BYTE && type <= REPETITION);
		
	}
	
	public FileFormat addByte(String entryName);
	public FileFormat addShort(String entryName);
	public FileFormat addInt(String entryName);
	public FileFormat addLong(String entryName);
	public FileFormat addFloat(String entryName);
	public FileFormat addDouble(String entryName);
	public FileFormat addBoolean(String entryName);
	public FileFormat addChar(String entryName);
	public FileFormat addString(String entryName);
           
	public FileFormat addByteArray(String entryName);
	public FileFormat addShortArray(String entryName);
	public FileFormat addIntArray(String entryName);
	public FileFormat addLongArray(String entryName);
	public FileFormat addFloatArray(String entryName);
	public FileFormat addDoubleArray(String entryName);
	public FileFormat addBooleanArray(String entryName);
	public FileFormat addCharArray(String entryName);
	public FileFormat addStringArray(String entryName);

	public FileFormat addChunk(String name);
	public FileFormat addRepetition(String name);

	public FileFormat endFormat();
	
}
