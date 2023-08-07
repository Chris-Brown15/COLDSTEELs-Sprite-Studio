package cs.csss.misc.io;

import java.nio.ByteBuffer;

class _IntChunk extends DataChunk {

	private int value;
	
	protected _IntChunk(String name, int object) {
		
		super(name);
		value = object;
		
	}

	protected _IntChunk(String name) {
		
		super(name);
		
	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (int) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {
		
		buffer.putInt(value);
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = buffer.getInt();
				
	}

	@Override public int sizeOf() {

		return Integer.BYTES;
		
	}

	@Override public String toString() {

		return "Int Chunk of " + value;
		
	}

	@Override protected DataChunk clone() {

		return new _IntArrayChunk(name , value);
		
	}

}
