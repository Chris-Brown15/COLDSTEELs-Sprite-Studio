package cs.csss.misc.io;

import static cs.core.utils.CSUtils.specify;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;

public class RepeatingChunk extends DataChunk implements Format , ChunkCreator {

	private static final HashMap<RepeatingChunk , __RepeatingChunkReference> references = new HashMap<>();
	
	protected int 
		instances ,
		next = 0 ,
		recent = 0
	;

	private final ContainerChunk sourceChunk = new ContainerChunk("CONTAINER DON'T USE");
	
	/**
	 * Array of containers used to represent repeating structure. 
	 */
	protected ContainerChunk[] chunks;

	protected RepeatingChunk(String name , int instances) {

		super(name);
		initializeChunks(instances);		
	
	}

	protected RepeatingChunk(String name) {

		super(name);
		initializeChunks(1);
	
	}

	protected void initializeChunks(int instances) {
		
		next = 0;
		this.instances = instances;
		chunks = new ContainerChunk[instances];
		for(int i = 0 ; i < chunks.length ; i++) addChunk(chunk("Container " + i));
	
	}
	
	public void setRepetitions(int repetitions) {
		
		ContainerChunk[] chunks = new ContainerChunk[repetitions];
		
		System.arraycopy(this.chunks, 0, chunks, 0, chunks.length);
		
		this.chunks = chunks;
		instances = repetitions;
		
	}
	
	protected void copySourceToChunk(ContainerChunk destination) {

		for(DataChunk d : sourceChunk) destination.addChunk(d.clone());
		
	}
	
	public RepeatingChunk repeatingChunk(String name , int repeats) {

		for(ContainerChunk x : chunks) { 
		
//			require(!x.name.equals(name));
			if(x.name.equals(name)) {
				
				return (RepeatingChunk) x.entries.get(name);
				
			}
			
		}
		
		LinkedList<RepeatingChunk> listOfRepeats = new LinkedList<>();
		
		listOfRepeats.add(sourceChunk.repeatingChunk(name , repeats));
		for(ContainerChunk x : chunks) listOfRepeats.add(x.repeatingChunk(name, repeats));
		
		__RepeatingChunkReference reference = new __RepeatingChunkReference(name , repeats , listOfRepeats.toArray(RepeatingChunk[]::new));
		references.put(this , reference);
		
		return reference;
		
	}

	public RepeatingChunk repeatingChunk(String name) {

		for(ContainerChunk x : chunks) if(x.name.equals(name)) return references.get(this);
		
		LinkedList<RepeatingChunk> listOfRepeats = new LinkedList<>();
		
		listOfRepeats.add(sourceChunk.repeatingChunk(name));
		for(ContainerChunk x : chunks) listOfRepeats.add(x.repeatingChunk(name));
		
		__RepeatingChunkReference reference = new __RepeatingChunkReference(name , listOfRepeats.toArray(RepeatingChunk[]::new));
		references.put(this , reference);
		
		return reference;
		
	}
	
	@Override public ContainerChunk chunk(String name) {

		ContainerChunk chunk = new ContainerChunk(name);		
		copySourceToChunk(chunk);		
		return chunk;
		
	}

	@Override public void addChunk(ContainerChunk chunk) {
	
		chunks[next++] = chunk;
		
	}

	@Override public void removeChunk(ContainerChunk chunk) {
		
		throw new UnsupportedOperationException("Chunks cannot be removed individually from a repeating chunk.");
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		Format.writeSize(buffer, instances);
		for(ContainerChunk x : chunks) x.write(buffer);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		int listSize = Format.readSize(buffer);
		
		if(chunks == null) initializeChunks(listSize);
		
		for(ContainerChunk x : chunks) x.read(buffer);
		
	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(instances) + (sourceChunk.sizeOf() * instances);
		
	}

	@Override public String toString() {

		StringBuilder builder = new StringBuilder("Repeating chunk with " + instances + " instances, size: " + sizeOf() + ", chunks: ");
		for(Chunk x : chunks) builder.append('\n').append(x);
		
		return builder.toString();
		
	}

	@Override public RepeatingChunk addByte(String entryName, byte value) {

		sourceChunk.addByte(entryName , value);
		for(ContainerChunk x : chunks) x.addByte(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addShort(String entryName, short value) {

		sourceChunk.addShort(entryName , value);
		for(ContainerChunk x : chunks) x.addShort(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addInt(String entryName, int value) {

		sourceChunk.addInt(entryName , value);
		for(ContainerChunk x : chunks) x.addInt(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addLong(String entryName, long value) {

		sourceChunk.addLong(entryName , value);
		for(ContainerChunk x : chunks) x.addLong(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addFloat(String entryName, float value) {

		sourceChunk.addFloat(entryName , value);
		for(ContainerChunk x : chunks) x.addFloat(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addDouble(String entryName, double value) {

		sourceChunk.addDouble(entryName , value);
		for(ContainerChunk x : chunks) x.addDouble(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addBoolean(String entryName, boolean value) {

		sourceChunk.addBoolean(entryName , value);
		for(ContainerChunk x : chunks) x.addBoolean(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addChar(String entryName, char value) {

		sourceChunk.addChar(entryName , value);
		for(ContainerChunk x : chunks) x.addChar(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addString(String entryName, CharSequence value) {

		sourceChunk.addString(entryName , value);
		for(ContainerChunk x : chunks) x.addString(entryName , value);
		return this;
		
	}

	@Override public RepeatingChunk addByte(String entryName) {

		sourceChunk.addByte(entryName);
		for(ContainerChunk x : chunks) x.addByte(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addShort(String entryName) {

		sourceChunk.addShort(entryName);
		for(ContainerChunk x : chunks) x.addShort(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addInt(String entryName) {

		sourceChunk.addInt(entryName);
		for(ContainerChunk x : chunks) x.addInt(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addLong(String entryName) {

		sourceChunk.addLong(entryName);
		for(ContainerChunk x : chunks) x.addLong(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addFloat(String entryName) {

		sourceChunk.addFloat(entryName);
		for(ContainerChunk x : chunks) x.addFloat(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addDouble(String entryName) {

		sourceChunk.addDouble(entryName);
		for(ContainerChunk x : chunks) x.addDouble(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addBoolean(String entryName) {

		sourceChunk.addBoolean(entryName);
		for(ContainerChunk x : chunks) x.addBoolean(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addChar(String entryName) {

		sourceChunk.addChar(entryName);
		for(ContainerChunk x : chunks) x.addChar(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addString(String entryName) {

		sourceChunk.addString(entryName);
		for(ContainerChunk x : chunks) x.addString(entryName);
		return this;
		
	}

	@Override public RepeatingChunk addBytes(String entryName, byte[] value) {

		sourceChunk.addBytes(entryName , value);
		for(ContainerChunk x : chunks) x.addBytes(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addShorts(String entryName, short[] value) {

		sourceChunk.addShorts(entryName , value);
		for(ContainerChunk x : chunks) x.addShorts(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addInts(String entryName, int[] value) {

		sourceChunk.addInts(entryName , value);
		for(ContainerChunk x : chunks) x.addInts(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addLongs(String entryName, long[] value) {

		sourceChunk.addLongs(entryName , value);
		for(ContainerChunk x : chunks) x.addLongs(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addFloats(String entryName, float[] value) {

		sourceChunk.addFloats(entryName , value);
		for(ContainerChunk x : chunks) x.addFloats(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addDoubles(String entryName, double[] value) {

		sourceChunk.addDoubles(entryName , value);
		for(ContainerChunk x : chunks) x.addDoubles(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addBooleans(String entryName, boolean[] value) {

		sourceChunk.addBooleans(entryName , value);
		for(ContainerChunk x : chunks) x.addBooleans(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addChars(String entryName, char[] value) {

		sourceChunk.addChars(entryName , value);
		for(ContainerChunk x : chunks) x.addChars(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addStrings(String entryName, CharSequence[] value) {

		sourceChunk.addStrings(entryName , value);
		for(ContainerChunk x : chunks) x.addStrings(entryName, value);
		return this;

	}

	@Override public RepeatingChunk addBytes(String entryName) {

		sourceChunk.addBytes(entryName);
		for(ContainerChunk x : chunks) x.addBytes(entryName);
		return this;

	}

	@Override public RepeatingChunk addShorts(String entryName) {

		sourceChunk.addShorts(entryName);
		for(ContainerChunk x : chunks) x.addShorts(entryName);
		return this;

	}

	@Override public RepeatingChunk addInts(String entryName) {

		sourceChunk.addInts(entryName);
		for(ContainerChunk x : chunks) x.addInts(entryName);
		return this;

	}

	@Override public RepeatingChunk addLongs(String entryName) {

		sourceChunk.addLongs(entryName);
		for(ContainerChunk x : chunks) x.addLongs(entryName);
		return this;

	}

	@Override public RepeatingChunk addFloats(String entryName) {

		sourceChunk.addFloats(entryName);
		for(ContainerChunk x : chunks) x.addFloats(entryName);
		return this;

	}

	@Override public RepeatingChunk addDoubles(String entryName) {

		sourceChunk.addDoubles(entryName);
		for(ContainerChunk x : chunks) x.addDoubles(entryName);
		return this;

	}

	@Override public RepeatingChunk addBooleans(String entryName) {

		sourceChunk.addBooleans(entryName);
		for(ContainerChunk x : chunks) x.addBooleans(entryName);
		return this;

	}

	@Override public RepeatingChunk addChars(String entryName) {

		sourceChunk.addChars(entryName);
		for(ContainerChunk x : chunks) x.addChars(entryName);
		return this;

	}

	@Override public RepeatingChunk addStrings(String entryName) {

		sourceChunk.addStrings(entryName);
		for(ContainerChunk x : chunks) x.addStrings(entryName);
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
		chunks[chunkIndex].setByte(entryName, value);
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

	protected void verifyChunkIndex(int index) {
		
		specify(index >= 0 && index < chunks.length , "Index " + index + " out of bounds for length " + chunks.length);
		recent = index;
		
	}

	@Override protected DataChunk clone() {

		throw new UnsupportedOperationException("Repeating chunks cannot be cloned.");
	
	}

	@Override protected Object get() {

		return chunks[recent];
	
	}

	@Override protected void set(Object value) {
		
		throw new UnsupportedOperationException("set method unsupported.");
		
	}

	public int getRepetitions() {
		
		return instances;
		
	}
	
}
