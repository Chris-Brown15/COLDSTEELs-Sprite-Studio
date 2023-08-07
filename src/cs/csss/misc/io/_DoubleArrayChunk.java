/**
 * 
 */
package cs.csss.misc.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 
 */
class _DoubleArrayChunk extends DataChunk{

	private double[] value;
	
	/**
	 * @param name
	 * @param object
	 */
	public _DoubleArrayChunk(String name, double... object) {
		
		super(name);

		value = object;
		
	}

	/**
	 * @param name
	 */
	public _DoubleArrayChunk(String name) {
		
		super(name);

	}

	@Override protected Object get() {
		
		return value;
		
	}

	@Override protected void set(Object value) {
		
		this.value = (double[]) value;
		
	}
	
	@Override protected void write(ByteBuffer buffer) {
		
		Format.writeSize(buffer, value.length);
		for(double x : value) buffer.putDouble(x);
			
	}

	@Override protected void read(ByteBuffer buffer) {

		value = new double[Format.readSize(buffer)];
		for(int i = 0 ; i < value.length ; i++) value[i] = buffer.getDouble();

	}

	@Override public int sizeOf() {

		return Format.sizeOfListSizePrefix(value.length) + (value.length * Double.BYTES);
		
	}

	@Override public String toString() {

		return "Double Array Chunk of " + Arrays.toString(value);
		
	}

	@Override protected DataChunk clone() {

		return value != null ? new _DoubleArrayChunk(name , Arrays.copyOf(value, value.length)) : new _DoubleArrayChunk(name);
		
	}

}
