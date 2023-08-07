package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

class _IntArrayChunk extends DataChunk {

	private int[] value;
	
	protected _IntArrayChunk(String name, int... object) {
		
		super(name);
		value = object;
		
	}

	protected _IntArrayChunk(String name) {
		
		super(name);
		
	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (int[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {
		
		Format.writeSize(buffer, value.length);
		for(int x : value) buffer.putInt(x);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = new int[Format.readSize(buffer)];
		for(int i = 0 ; i < value.length ; i++) value[i] = buffer.getInt();
				
	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(value.length) + (value.length * Integer.BYTES);
		
	}

	@Override public String toString() {

		return "Int Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _IntArrayChunk(name , Arrays.copyOf(value, value.length)) : new _IntArrayChunk(name);
		
	}

}
