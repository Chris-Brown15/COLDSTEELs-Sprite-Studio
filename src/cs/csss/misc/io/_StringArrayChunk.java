/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 
 */
class _StringArrayChunk extends DataChunk {

	private CharSequence[] value;
	private int size;
	
	/**
	 * @param name
	 * @param object
	 */
	public _StringArrayChunk(String name, CharSequence... object) {
		
		super(name);
	
		value = object;
		size = Format.sizeOfListSizePrefix(object.length);
		for(CharSequence x : value) size += Format.sizeOfListSizePrefix(x.length()) + (x.length() * Character.BYTES);
		
	}

	/**
	 * 
	 * 
	 * @param name
	 */
	public _StringArrayChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (CharSequence[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {

		Format.writeSize(buffer, value.length);
		for(CharSequence x : value) {
			
			Format.writeSize(buffer, x.length());
			IntStream codepoints = x.codePoints();
			codepoints.forEachOrdered(codepoint -> buffer.putChar((char)codepoint));

		}
		
	}

	@Override protected void read(ByteBuffer buffer) {

		value = new CharSequence[Format.readSize(buffer)];
		this.size += Format.sizeOfListSizePrefix(value.length);
		
		for(int i = 0 ; i < value.length ; i++) {
			
			int size = Format.readSize(buffer);
			char[] chars = new char[size];
			for(int j = 0 ; j < size ; j++) chars[j] = buffer.getChar();
			value[i] = new String(chars);
			this.size += Format.sizeOfListSizePrefix(size) + (size * Character.BYTES);
		
		}
		
	}

	@Override public int sizeOf() {

		return size;
		
	}

	@Override public String toString() {

		return "String Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _StringArrayChunk(name , Arrays.copyOf(value, value.length)) : new _StringArrayChunk(name);
		
	}

}
