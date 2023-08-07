package cs.csss.misc.io;

import java.nio.ByteBuffer;

class _LongChunk extends DataChunk {

	private long value;
	
	_LongChunk(String name , long value) {
		
		super(name);
		this.value = value;
		
	}
	
	_LongChunk(String name) {

		super(name);
		
	}
	
	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (long) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {
		
		buffer.putLong(value);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = buffer.getLong();
		
	}

	@Override public int sizeOf() {

		return Long.BYTES;
		
	}

	@Override public String toString() {

		return "Long Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return new _LongArrayChunk(name , value);
		
	}

}
