/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;

/**
 * 
 */
class _FloatChunk extends DataChunk {

	private float value;
	
	/**
	 * @param name
	 * @param object
	 */
	public _FloatChunk(String name, float object) {
		
		super(name);
		this.value = object;
		
	}

	/**
	 * @param name
	 */
	public _FloatChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (float) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		buffer.putFloat(value);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = buffer.getFloat();

	}

	@Override public int sizeOf() {

		return Float.BYTES;
		
	}

	@Override public String toString() {

		return "Float Chunk of " + value;
		
	}
	
	@Override protected DataChunk clone() {

		return new _FloatArrayChunk(name , value);
		
	}

}
