/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 
 */
class _BooleanArrayChunk extends DataChunk {

	private boolean[] value;
	
	/**
	 * @param name
	 * @param object
	 */
	_BooleanArrayChunk(String name, boolean... object) {

		super(name);
		this.value = object;
		
	}

	/**
	 * @param name
	 */
	_BooleanArrayChunk(String name) {

		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (boolean[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		Format.writeSize(buffer, value.length);
		for(boolean x : value) buffer.put(x ? (byte) 1 : 0);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = new boolean[Format.readSize(buffer)];
		for(int i = 0 ; i < value.length ; i++) value[i] = buffer.get() != 0;

	}

	@Override public int sizeOf() {
		
		return Format.sizeOfListSizePrefix(value.length) + (value.length * 1);
		
	}

	@Override public String toString() {

		return "Boolean Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _BooleanArrayChunk(name , Arrays.copyOf(value , value.length)) : new _BooleanArrayChunk(name);
		
	}

}
