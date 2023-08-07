/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 
 */
class _CharArrayChunk extends DataChunk {

	private char[] value;
	
	/**
	 * @param name
	 * @param object
	 */
	public _CharArrayChunk(String name, char... object) {
		
		super(name);
		value = object;
	}

	/**
	 * @param name
	 */
	public _CharArrayChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (char[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {
		
		Format.writeSize(buffer, value.length);
		for(char x : value) buffer.putChar(x);

	}

	@Override protected void read(ByteBuffer buffer) {
		
		value = new char[Format.readSize(buffer)];
		for(int i = 0 ; i < value.length ; i++) value[i] = buffer.getChar();

	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(value.length) + (value.length * Character.BYTES);
		
	}

	@Override public String toString() {

		return "Character Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _CharArrayChunk(name , Arrays.copyOf(value, value.length)) : new _CharArrayChunk(name);
		
	}

}
