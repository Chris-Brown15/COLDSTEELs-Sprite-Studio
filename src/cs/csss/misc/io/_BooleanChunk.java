/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;

/**
 * 
 */
class _BooleanChunk extends DataChunk {

	private boolean value;
	
	/**
	 * @param name
	 * @param object
	 */
	public _BooleanChunk(String name, boolean object) {

		super(name);
		this.value = object;
		
	}

	/**
	 * @param name
	 */
	public _BooleanChunk(String name) {

		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (boolean) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		buffer.put(value ? (byte) 1 : 0);
		
	}

	@Override protected void read(ByteBuffer buffer) {

	  	value = buffer.get() != 0;

	}

	@Override public int sizeOf() {
		
		return 1;
		
	}

	@Override public String toString() {

		return "Boolean Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return new _BooleanArrayChunk(name , value);
		
	}

}
