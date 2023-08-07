package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

class _ByteArrayChunk extends DataChunk {

	private byte[] value;
	
	_ByteArrayChunk(String name , byte... value) {
		
		super(name);
		this.value = value;
		
	}

	_ByteArrayChunk(String name) {
		
		super(name);
		
	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (byte[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		Format.writeSize(buffer, value.length);
		buffer.put(value);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = new byte[Format.readSize(buffer)];
		System.arraycopy(buffer.array(), buffer.position(), value, 0, value.length);
		buffer.position(buffer.position() + value.length);
		
	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(value.length) + (value.length * Byte.BYTES);
		
	}

	@Override public String toString() {

		return "Byte Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _ByteArrayChunk(name , Arrays.copyOf(value, value.length)) : new _ByteArrayChunk(name);
		
	}

}
