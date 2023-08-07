package cs.csss.misc.files;

import java.nio.ByteBuffer;

/**
 * Used in conjunction with {@link cs.csss.misc.files.FileComposition FileComposition} instances to easily pass an object to a 
 * {@code FileComposition}.
 * 
 * @author Chris Brown
 *
 */
public interface Composable {

	/**
	 * Puts the bytes of {@code this} into {@code writeTo}. 
	 * 
	 * @param writeTo — buffer to write to
	 */
	public void put(ByteBuffer writeTo);
	
	/**
	 * Reads the bytes of {@code readFrom} to parse an object, returning it.
	 * 
	 * @param readFrom — buffer to read from
	 * @return Instance of an implementor of this.
	 */
	public Object retrieve(ByteBuffer readFrom);
	
	/**
	 * Returns the exact number of bytes a call to {@code write(this , buffer)} would advance the buffer's position at the current moment.
	 * 
	 * @return Size in bytes of this object.
	 */
	public int sizeBytes();
	
}
