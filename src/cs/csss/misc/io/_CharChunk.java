/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;

/**
 * 
 */
class _CharChunk extends DataChunk {

	private char value;
	
	/**
	 * @param name
	 * @param object
	 */
	public _CharChunk(String name, char object) {
		
		super(name);
		value = object;
	}

	/**
	 * @param name
	 */
	public _CharChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (char) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		buffer.putChar(value);

	}

	@Override protected void read(ByteBuffer buffer) {

		value = buffer.getChar();

	}

	@Override public int sizeOf() {

		return Character.BYTES;
		
	}

	@Override public String toString() {

		return "Character Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return new _CharChunk(name , value);
		
	}

}
