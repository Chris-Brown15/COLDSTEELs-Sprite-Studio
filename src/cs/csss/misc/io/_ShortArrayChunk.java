package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

class _ShortArrayChunk extends DataChunk {

	private short[] value;
	
	protected _ShortArrayChunk(String name , short... object) {

		super(name);
		value = object;
		
	}

	protected _ShortArrayChunk(String name) {
		
		super(name);
		
	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (short[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		Format.writeSize(buffer, value.length);
		for(short x : value) buffer.putShort(x);
		

	}

	@Override protected void read(ByteBuffer buffer) {

		value = new short[Format.readSize(buffer)];
		for(int i = 0 ; i < value.length ; i++) value[i] = buffer.getShort();
		
	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(value.length) + (value.length * Short.BYTES);
		
	}

	@Override public String toString() {

		return "Short Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _ShortArrayChunk(name , Arrays.copyOf(value, value.length)) : new _ShortArrayChunk(name);
		
	}

}
