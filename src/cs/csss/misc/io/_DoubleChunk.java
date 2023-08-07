/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;

/**
 * 
 */
class _DoubleChunk extends DataChunk{

	private double value;
	
	/**
	 * @param name
	 * @param object
	 */
	public _DoubleChunk(String name, double object) {
		
		super(name);

		value = object;
		
	}

	/**
	 * @param name
	 */
	public _DoubleChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (double) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		buffer.putDouble(value);
			
	}

	@Override protected void read(ByteBuffer buffer) {

		value = buffer.getDouble();

	}

	@Override public int sizeOf() {

		return Double.BYTES;
		
	}

	@Override public String toString() {

		return "Double Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return new _DoubleChunk(name , value);
		
	}

}
