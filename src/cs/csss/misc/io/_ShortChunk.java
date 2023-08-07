package cs.csss.misc.io;

import java.nio.ByteBuffer;

class _ShortChunk extends DataChunk {

	private short value;
	
	protected _ShortChunk(String name , short object) {

		super(name);
		value = object;
		
	}

	protected _ShortChunk(String name) {
		
		super(name);
		
	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (short) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		buffer.putShort(value);
		

	}

	@Override protected void read(ByteBuffer buffer) {

		value = buffer.getShort();
		
	}

	@Override public int sizeOf() {

		return Short.BYTES;
		
	}

	@Override public String toString() {

		return "Short Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return new _ShortChunk(name , value);
		
	}

}
