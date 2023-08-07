package cs.csss.misc.io;

import java.nio.ByteBuffer;

/**
 * Superclass of all types of chunks, which contain data and other chunks, and who eventually get turned into bytes.
 * 
 * <p>
 * 	Chunks come in a few flavors, and by default all chunks are package level access. Internally, chunks have a few distinctions, although
 * 	these distinctions are not represented in code, nor to the outside world. Some chunks are data chunks. Some are container chunks. Data 
 * 	chunks do not by default contain other chunks. Container chunks contain no data, but do contain other chunks.
 * </p>
 * 
 * @author Chris Brown
 */
public abstract class Chunk {

	public final String name;

	protected Chunk(String name) {
		
		this.name = name;
	
	}

	/**
	 * Writes the bytes of this chunk entry into the given buffer.
	 * 
	 * @param buffer — buffer to write to
	 */
	protected abstract void write(ByteBuffer buffer);
	
	/**
	 * Reads the bytes of this chunk entry from the given buffer, creating an object from them.
	 * 
	 * @param buffer — buffer to read from
	 */
	protected abstract void read(ByteBuffer buffer);
	
	/**
	 * Returns the size in bytes of this chunk, that is, the size of it, and all its subchunks.
	 * 
	 * @return Total size in bytes needed to store this chunk.
	 */
	public abstract int sizeOf();
	
	/**
	 * Present to force chunks to be displayable as a String.
	 */
	public abstract String toString();

}