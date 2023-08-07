package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

class _LongArrayChunk extends DataChunk {

	private long[] value;
	
	_LongArrayChunk(String name , long... value) {
		
		super(name);
		this.value = value;
		
	}
	
	_LongArrayChunk(String name) {

		super(name);
		
	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (long[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {
		
		Format.writeSize(buffer, value.length);
		for(long x : value) buffer.putLong(x);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = new long[Format.readSize(buffer)];
		for(int i = 0 ; i < value.length ; i++) value[i] = buffer.getLong();
		
	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(value.length) + (value.length * Long.BYTES);
		
	}

	@Override public String toString() {

		return "Long Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _LongArrayChunk(name , Arrays.copyOf(value, value.length)) : new _LongArrayChunk(name);
		
	}

}
