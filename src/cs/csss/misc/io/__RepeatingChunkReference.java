package cs.csss.misc.io;

class __RepeatingChunkReference extends RepeatingChunk {

	protected RepeatingChunk[] repeats;
	
	__RepeatingChunkReference(String name, int instances , RepeatingChunk...repeats) {

		super(name, instances);
		this.repeats = repeats;
		
	}

	__RepeatingChunkReference(String name , RepeatingChunk...repeats) {
		
		super(name);
		this.repeats = repeats;
				
	}

	@Override public RepeatingChunk addByte(String entryName, byte value) {

		for(RepeatingChunk x : repeats) x.addByte(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addShort(String entryName, short value) {

		for(RepeatingChunk x : repeats) x.addShort(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addInt(String entryName, int value) {

		for(RepeatingChunk x : repeats) x.addInt(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addLong(String entryName, long value) {

		for(RepeatingChunk x : repeats) x.addLong(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addFloat(String entryName, float value) {

		for(RepeatingChunk x : repeats) x.addFloat(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addDouble(String entryName, double value) {

		for(RepeatingChunk x : repeats) x.addDouble(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addBoolean(String entryName, boolean value) {

		for(RepeatingChunk x : repeats) x.addBoolean(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addChar(String entryName, char value) {

		for(RepeatingChunk x : repeats) x.addChar(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addString(String entryName, CharSequence value) {

		for(RepeatingChunk x : repeats) x.addString(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addByte(String entryName) {

		for(RepeatingChunk x : repeats) x.addByte(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addShort(String entryName) {

		for(RepeatingChunk x : repeats) x.addShort(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addInt(String entryName) {

		for(RepeatingChunk x : repeats) x.addInt(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addLong(String entryName) {

		for(RepeatingChunk x : repeats) x.addLong(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addFloat(String entryName) {

		for(RepeatingChunk x : repeats) x.addFloat(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addDouble(String entryName) {

		for(RepeatingChunk x : repeats) x.addDouble(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addBoolean(String entryName) {

		for(RepeatingChunk x : repeats) x.addBoolean(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addChar(String entryName) {

		for(RepeatingChunk x : repeats) x.addChar(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addString(String entryName) {

		for(RepeatingChunk x : repeats) x.addString(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addBytes(String entryName, byte[] value) {

		for(RepeatingChunk x : repeats) x.addBytes(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addShorts(String entryName, short[] value) {

		for(RepeatingChunk x : repeats) x.addShorts(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addInts(String entryName, int[] value) {

		for(RepeatingChunk x : repeats) x.addInts(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addLongs(String entryName, long[] value) {

		for(RepeatingChunk x : repeats) x.addLongs(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addFloats(String entryName, float[] value) {

		for(RepeatingChunk x : repeats) x.addFloats(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addDoubles(String entryName, double[] value) {

		for(RepeatingChunk x : repeats) x.addDoubles(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addBooleans(String entryName, boolean[] value) {

		for(RepeatingChunk x : repeats) x.addBooleans(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addChars(String entryName, char[] value) {

		for(RepeatingChunk x : repeats) x.addChars(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addStrings(String entryName, CharSequence[] value) {

		for(RepeatingChunk x : repeats) x.addStrings(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addBytes(String entryName) {

		for(RepeatingChunk x : repeats) x.addBytes(entryName);
		return this;

	}

	@Override public RepeatingChunk addShorts(String entryName) {

		for(RepeatingChunk x : repeats) x.addShorts(entryName);
		return this;

	}

	@Override public RepeatingChunk addInts(String entryName) {

		for(RepeatingChunk x : repeats) x.addInts(entryName);
		return this;

	}

	@Override public RepeatingChunk addLongs(String entryName) {

		for(RepeatingChunk x : repeats) x.addLongs(entryName);
		return this;

	}

	@Override public RepeatingChunk addFloats(String entryName) {

		for(RepeatingChunk x : repeats) x.addFloats(entryName);
		return this;

	}

	@Override public RepeatingChunk addDoubles(String entryName) {

		for(RepeatingChunk x : repeats) x.addDoubles(entryName);
		return this;

	}

	@Override public RepeatingChunk addBooleans(String entryName) {

		for(RepeatingChunk x : repeats) x.addBooleans(entryName);
		return this;

	}

	@Override public RepeatingChunk addChars(String entryName) {

		for(RepeatingChunk x : repeats) x.addChars(entryName);
		return this;

	}

	@Override public RepeatingChunk addStrings(String entryName) {

		for(RepeatingChunk x : repeats) x.addStrings(entryName);
		return this;

	}

	public byte getByte(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getByte(entryName);

	}

	public short getShort(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);	
		return chunks[chunkIndex].getShort(entryName);

	}

	public int getInt(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getInt(entryName);

	}

	public long getLong(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getLong(entryName);

	}

	public float getFloat(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getFloat(entryName);

	}

	public double getDouble(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getDouble(entryName);

	}

	public boolean getBoolean(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getBoolean(entryName);

	}

	public char getChar(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getChar(entryName);

	}

	public CharSequence getString(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getString(entryName);

	}

	public byte[] getByteArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getByteArray(entryName);

	}

	public short[] getShortArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getShortArray(entryName);

	}

	public int[] getIntArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getIntArray(entryName);

	}

	public long[] getLongArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getLongArray(entryName);

	}

	public float[] getFloatArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getFloatArray(entryName);

	}

	public double[] getDoubleArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getDoubleArray(entryName);

	}

	public boolean[] getBooleanArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getBooleanArray(entryName);

	}

	public char[] getCharArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getCharArray(entryName);

	}

	public CharSequence[] getStringArray(String entryName , int chunkIndex) {

		verifyChunkIndex(chunkIndex);		
		return chunks[chunkIndex].getStringArray(entryName);

	}

	@Override public byte getByte(String entryName) {

		return chunks[recent].getByte(entryName);

	}

	@Override public short getShort(String entryName) {

		return chunks[recent].getShort(entryName);

	}

	@Override public int getInt(String entryName) {

		return chunks[recent].getInt(entryName);

	}

	@Override public long getLong(String entryName) {

		return chunks[recent].getLong(entryName);
		
	}

	@Override public float getFloat(String entryName) {

		return chunks[recent].getFloat(entryName);
		
	}

	@Override public double getDouble(String entryName) {

		return chunks[recent].getDouble(entryName);
		
	}

	@Override public boolean getBoolean(String entryName) {

		return chunks[recent].getBoolean(entryName);
		
	}

	@Override public char getChar(String entryName) {

		return chunks[recent].getChar(entryName);
		
	}

	@Override public CharSequence getString(String entryName) {

		return chunks[recent].getString(entryName);
		
	}

	@Override public byte[] getByteArray(String entryName) {

		return chunks[recent].getByteArray(entryName);
		
	}

	@Override public short[] getShortArray(String entryName) {

		return chunks[recent].getShortArray(entryName);
		
	}

	@Override public int[] getIntArray(String entryName) {

		return chunks[recent].getIntArray(entryName);
		
	}

	@Override public long[] getLongArray(String entryName) {

		return chunks[recent].getLongArray(entryName);
		
	}

	@Override public float[] getFloatArray(String entryName) {

		return chunks[recent].getFloatArray(entryName);
		
	}

	@Override public double[] getDoubleArray(String entryName) {

		return chunks[recent].getDoubleArray(entryName);
		
	}

	@Override public boolean[] getBooleanArray(String entryName) {

		return chunks[recent].getBooleanArray(entryName);
		
	}

	@Override public char[] getCharArray(String entryName) {

		return chunks[recent].getCharArray(entryName);
		
	}

	@Override public CharSequence[] getStringArray(String entryName) {

		return chunks[recent].getStringArray(entryName);
		
	}
	
	public RepeatingChunk setByte(String entryName, byte value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		for(RepeatingChunk x : repeats) x.chunks[chunkIndex].setByte(entryName, value);
		return this;
		
	}

	public RepeatingChunk setShort(String entryName, short value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setShort(entryName, value);
		return this;
		
	}

	public RepeatingChunk setInt(String entryName, int value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setInt(entryName, value);
		return this;
		
	}

	public RepeatingChunk setLong(String entryName, long value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setLong(entryName, value);
		return this;
		
	}

	public RepeatingChunk setFloat(String entryName, float value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setFloat(entryName, value);
		return this;
		
	}

	public RepeatingChunk setDouble(String entryName, double value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setDouble(entryName, value);
		return this;
		
	}

	public RepeatingChunk setBoolean(String entryName, boolean value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setBoolean(entryName, value);
		return this;
		
	}

	public RepeatingChunk setChar(String entryName, char value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setChar(entryName, value);
		return this;
		
	}

	public RepeatingChunk setString(String entryName, CharSequence value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setString(entryName, value);
		return this;
		
	}

	public RepeatingChunk setBytes(String entryName, byte[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setBytes(entryName, value);
		return this;
		
	}

	public RepeatingChunk setShorts(String entryName, short[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setShorts(entryName, value);
		return this;
		
	}

	public RepeatingChunk setInts(String entryName, int[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setInts(entryName, value);
		return this;
		
	}

	public RepeatingChunk setLongs(String entryName, long[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setLongs(entryName, value);
		return this;
		
	}

	public RepeatingChunk setFloats(String entryName, float[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setFloats(entryName, value);
		return this;
		
	}

	public RepeatingChunk setDoubles(String entryName, double[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setDoubles(entryName, value);
		return this;
		
	}

	public RepeatingChunk setBooleans(String entryName, boolean[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setBooleans(entryName, value);
		return this;
		
	}

	public RepeatingChunk setChars(String entryName, char[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setChars(entryName, value);
		return this;
		
	}

	public RepeatingChunk setStrings(String entryName, CharSequence[] value , int chunkIndex) {

		verifyChunkIndex(chunkIndex);
		chunks[chunkIndex].setStrings(entryName, value);
		return this;
		
	}
	
	@Override public RepeatingChunk setByte(String entryName, byte value) {

		chunks[recent].setByte(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setShort(String entryName, short value) {

		chunks[recent].setShort(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setInt(String entryName, int value) {

		chunks[recent].setInt(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setLong(String entryName, long value) {

		chunks[recent].setLong(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setFloat(String entryName, float value) {

		chunks[recent].setFloat(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setDouble(String entryName, double value) {

		chunks[recent].setDouble(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setBoolean(String entryName, boolean value) {

		chunks[recent].setBoolean(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setChar(String entryName, char value) {

		chunks[recent].setChar(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setString(String entryName, CharSequence value) {

		chunks[recent].setString(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setBytes(String entryName, byte[] value) {

		chunks[recent].setBytes(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setShorts(String entryName, short[] value) {

		chunks[recent].setShorts(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setInts(String entryName, int[] value) {

		chunks[recent].setInts(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setLongs(String entryName, long[] value) {

		chunks[recent].setLongs(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setFloats(String entryName, float[] value) {

		chunks[recent].setFloats(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setDoubles(String entryName, double[] value) {

		chunks[recent].setDoubles(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setBooleans(String entryName, boolean[] value) {

		chunks[recent].setBooleans(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setChars(String entryName, char[] value) {

		chunks[recent].setChars(entryName, value);
		return this;
		
	}

	@Override public RepeatingChunk setStrings(String entryName, CharSequence[] value) {

		chunks[recent].setStrings(entryName, value);
		return this;
		
	}


}
