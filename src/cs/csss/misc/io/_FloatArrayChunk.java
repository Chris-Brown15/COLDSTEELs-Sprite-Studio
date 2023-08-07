/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 
 */
class _FloatArrayChunk extends DataChunk {

	private float[] value;
	
	/**
	 * @param name
	 * @param object
	 */
	public _FloatArrayChunk(String name, float... object) {
		
		super(name);
		this.value = object;
		
	}

	/**
	 * @param name
	 */
	public _FloatArrayChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (float[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		Format.writeSize(buffer, value.length);
		for(float x : value) buffer.putFloat(x);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = new float[Format.readSize(buffer)];
		for(int i = 0 ; i < value.length ; i++) value[i] = buffer.getFloat();

	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(value.length) + (value.length * Float.BYTES);
		
	}

	@Override public String toString() {

		return "Float Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _FloatArrayChunk(name , Arrays.copyOf(value, value.length)) : new _FloatArrayChunk(name);
		
	}

}
