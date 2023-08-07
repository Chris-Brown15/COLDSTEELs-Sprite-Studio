package cs.csss.misc.io;

import static cs.core.utils.CSUtils.require;
import static cs.core.utils.CSUtils.specify;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ContainerChunk extends Chunk implements Format , Iterable<DataChunk> , Cloneable {
	
	protected final LinkedHashMap<String , DataChunk> entries = new LinkedHashMap<>();
	
	protected ContainerChunk(String name) {
		
		super(name);
		
	}

	public RepeatingChunk repeatingChunk(String chunkName , int repetitions) {
		
		require(!entries.containsKey(chunkName));
		
		RepeatingChunk repeating = new RepeatingChunk(chunkName, repetitions);
		addChunk(repeating);
		return repeating;
		
	}

	public RepeatingChunk repeatingChunk(String chunkName) {
		
		if(entries.containsKey(chunkName)) { 
			
			DataChunk chunk = entries.get(chunkName);
			
			require(chunk instanceof RepeatingChunk);
			
			return (RepeatingChunk) chunk;
		
		}
		
		RepeatingChunk repeating = new RepeatingChunk(chunkName);
		addChunk(repeating);
		return repeating;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		for(Chunk x : this) x.write(buffer);

	}

	@Override protected void read(ByteBuffer buffer) {

		for(Chunk x : this) x.read(buffer);
		
	}

	@Override public int sizeOf() {

		int size = 0;
		for(Chunk x : this) size += x.sizeOf();
		return size;
		
	}

	@Override public String toString() {
		
		StringBuilder builder = new StringBuilder("Chunk " + name + ", size: " + sizeOf());
		for(Chunk x : this) builder.append('\n').append(x);
		return builder.toString();
		
	}

	public void addChunk(DataChunk otherChunk) {
		
		require(otherChunk);
		entries.put(otherChunk.name , otherChunk);
		
	}
	
	public void removeChunk(Chunk otherChunk) {
		
		require(otherChunk);
		entries.remove(otherChunk.name , otherChunk);
		
	}

	/**
	 * Returns an iterator over the chunks stored within this chunk.
	 */
	@Override public Iterator<DataChunk> iterator() {
		
		return new EntryIterator();
		
	}

	@Override public ContainerChunk addByte(String entryName, byte value) {

		verifyName(entryName);
		entries.put(entryName , new _ByteChunk(entryName , value));
		return this;

	}

	@Override public ContainerChunk addShort(String entryName, short value) {

		verifyName(entryName);
		entries.put(entryName , new _ShortChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addInt(String entryName, int value) {

		verifyName(entryName);
		entries.put(entryName , new _IntChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addLong(String entryName, long value) {

		verifyName(entryName);
		entries.put(entryName , new _LongChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addFloat(String entryName, float value) {

		verifyName(entryName);
		entries.put(entryName , new _FloatChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addDouble(String entryName, double value) {

		verifyName(entryName);
		entries.put(entryName , new _DoubleChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addBoolean(String entryName, boolean value) {

		verifyName(entryName);
		entries.put(entryName , new _BooleanChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addChar(String entryName, char value) {

		verifyName(entryName);
		entries.put(entryName , new _CharChunk(entryName , value));
		return this;
	}

	@Override public ContainerChunk addString(String entryName, CharSequence value) {

		verifyName(entryName);
		entries.put(entryName , new _StringChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addByte(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _ByteChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addShort(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _ShortChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addInt(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _IntChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addLong(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _LongChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addFloat(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _FloatChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addDouble(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _DoubleChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addBoolean(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _BooleanChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addChar(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _CharChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addString(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _StringChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addBytes(String entryName, byte[] value) {

		verifyName(entryName);
		entries.put(entryName , new _ByteArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addShorts(String entryName, short[] value) {

		verifyName(entryName);
		entries.put(entryName , new _ShortArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addInts(String entryName, int[] value) {

		verifyName(entryName);
		entries.put(entryName , new _IntArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addLongs(String entryName, long[] value) {

		verifyName(entryName);
		entries.put(entryName , new _LongArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addFloats(String entryName, float[] value) {

		verifyName(entryName);
		entries.put(entryName , new _FloatArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addDoubles(String entryName, double[] value) {

		verifyName(entryName);
		entries.put(entryName , new _DoubleArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addBooleans(String entryName, boolean[] value) {

		verifyName(entryName);
		entries.put(entryName , new _BooleanArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addChars(String entryName, char[] value) {

		verifyName(entryName);
		entries.put(entryName , new _CharArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addStrings(String entryName, CharSequence[] value) {

		verifyName(entryName);
		entries.put(entryName , new _StringArrayChunk(entryName , value));
		return this;
		
	}

	@Override public ContainerChunk addBytes(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _ByteArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addShorts(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _ShortArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addInts(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _IntArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addLongs(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _LongArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addFloats(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _FloatArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addDoubles(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _DoubleArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addBooleans(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _BooleanArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addChars(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _CharArrayChunk(entryName));
		return this;
		
	}

	@Override public ContainerChunk addStrings(String entryName) {

		verifyName(entryName);
		entries.put(entryName , new _StringArrayChunk(entryName));
		return this;
		
	}

	@Override public byte getByte(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (byte) value.get();
		
	}

	@Override public short getShort(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (short) value.get();
		
	}

	@Override public int getInt(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (int) value.get();
		
	}

	@Override public long getLong(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (long) value.get();
		
	}

	@Override public float getFloat(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (float) value.get();
		
	}

	@Override public double getDouble(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (double) value.get();
		
	}

	@Override public boolean getBoolean(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (boolean) value.get();
		
	}

	@Override public char getChar(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (char) value.get();
		
	}

	@Override public CharSequence getString(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (CharSequence) value.get();
		
	}

	@Override public byte[] getByteArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (byte[]) value.get();
		
	}

	@Override public short[] getShortArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (short[]) value.get();
		
	}

	@Override public int[] getIntArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (int[]) value.get();
		
	}

	@Override public long[] getLongArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (long[]) value.get();
		
	}

	@Override public float[] getFloatArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (float[]) value.get();
		
	}

	@Override public double[] getDoubleArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (double[]) value.get();
		
	}

	@Override public boolean[] getBooleanArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (boolean[]) value.get();
		
	}

	@Override public char[] getCharArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (char[]) value.get();
		
	}

	@Override public CharSequence[] getStringArray(String entryName) {
		
		DataChunk value = entries.get(entryName);
		require(value);
		return (CharSequence[]) value.get();
		
	}

	/**
	 * Verifies the given name is not already the name of an entry within this chunk.
	 * 
	 * @param name — name of a chunk
	 */
	protected final void verifyName(String name) {
		
		specify(!entries.containsKey(name) , name + " already names a chunk.");
		
	}
	
	/**
	 * Uses the hash map iterator internally but disallows removal.
	 */
	private class EntryIterator implements Iterator<DataChunk> {

		private final Iterator<DataChunk> internalIterator;
		
		EntryIterator() {
			
			this.internalIterator = entries.values().iterator();
			
		}
		
		@Override public boolean hasNext() {

			return internalIterator.hasNext();
			
		}

		@Override public DataChunk next() {

			return internalIterator.next();
			
		}
		
	}

	@Override public ContainerChunk setByte(String entryName, byte value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setShort(String entryName, short value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setInt(String entryName, int value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setLong(String entryName, long value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setFloat(String entryName, float value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setDouble(String entryName, double value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setBoolean(String entryName, boolean value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setChar(String entryName, char value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setString(String entryName, CharSequence value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setBytes(String entryName, byte[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setShorts(String entryName, short[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setInts(String entryName, int[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setLongs(String entryName, long[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setFloats(String entryName, float[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setDoubles(String entryName, double[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setBooleans(String entryName, boolean[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setChars(String entryName, char[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}

	@Override public ContainerChunk setStrings(String entryName, CharSequence[] value) {
		
		DataChunk chunk = entries.get(entryName);
		require(chunk);
		chunk.set(value);
		return this;
		
	}
	
}
