package cs.csss.misc.io;

import java.nio.ByteBuffer;

class _ByteChunk extends DataChunk {

	private byte value;
	
	_ByteChunk(String name , byte value) {
		
		super(name);
		this.value = value;
		
	}

	_ByteChunk(String name) {
		
		super(name);
		
	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (byte) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		buffer.put(value);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = buffer.get();
		
	}

	@Override public int sizeOf() {

		return Byte.BYTES;
		
	}

	@Override public String toString() {

		return "Byte Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return new _ByteChunk(name , value);
		
	}

}
