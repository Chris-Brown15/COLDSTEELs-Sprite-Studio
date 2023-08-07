/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

/**
 * 
 */
class _StringChunk extends DataChunk {

	private CharSequence value;
	private int size;
	
	/**
	 * @param name
	 * @param object
	 */
	public _StringChunk(String name, CharSequence object) {
		
		super(name);
	
		value = object;
		setSize();
		
	}

	/**
	 * 
	 * 
	 * @param name
	 */
	public _StringChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (CharSequence) value;
		setSize();
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		Format.writeSize(buffer, value.length());
		IntStream codepoints = value.codePoints();
		codepoints.forEachOrdered(codepoint -> buffer.putChar((char)codepoint));
		
	}

	@Override protected void read(ByteBuffer buffer) {

		int size = Format.readSize(buffer);
		char[] chars = new char[size];
		for(int i = 0 ; i < size ; i ++) chars[i] = buffer.getChar();
		value = new String(chars);
		setSize();
		
	}

	@Override public int sizeOf() {

		return size;
		
	}

	@Override public String toString() {

		return "String Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _StringChunk(name , value) : new _StringChunk(name);
		
	}

	private void setSize() { 

		int length = value.length();
		size = Format.sizeOfListSizePrefix(length) + (length * Character.BYTES);
	}
	
}
