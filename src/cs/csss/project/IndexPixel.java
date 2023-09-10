package cs.csss.project;

import static cs.core.utils.CSUtils.require;
import static cs.core.utils.CSUtils.specify;

import java.nio.ByteBuffer;

import cs.csss.engine.LookupPixel;

/**
 * This class exists mainly because java does not have unsigned primitives. This means that code that wants a java {@code byte} to be 
 * unsigned, as it is in the GPU, may have errors which this class tries to fix. As well, there are basically different kinds of pixels
 * in this application, index pixels and palette pixels. Palette pixels are more complicated because their number of number of channels 
 * per pixel vary. This class attempts to make working with pixels as painless as possible.
 * 
 * @author Chris Brown
 *
 */
public class IndexPixel implements LookupPixel {

	public final short 
		xIndex ,
		yIndex
	;
	
	public IndexPixel(short xIndex , short yIndex) {
	
		specify(xIndex >= 0 , xIndex + " is an invalid x index") ; specify(yIndex >= 0 , yIndex + " is an invalid y index");
		
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		
	}	

	public IndexPixel(int xIndex , int yIndex) {

		specify(xIndex >= 0 , xIndex + " is an invalid x index") ; specify(yIndex >= 0 , yIndex + " is an invalid y index");
		
		this.xIndex = (short) xIndex;
		this.yIndex = (short) yIndex;
		
	}	

	public IndexPixel(ByteBuffer buffer) {
		
	 	xIndex = (short)Byte.toUnsignedInt(buffer.get());
	 	yIndex = (short)Byte.toUnsignedInt(buffer.get());
		
	}

	public void buffer(ByteBuffer buffer) {

		//2 is used because each pixel of the image is always two bytes.
		require(buffer.remaining() >= 2);
		
		buffer.put((byte)xIndex);
		buffer.put((byte)yIndex);
		
	}
	
	@Override public String toString() {
		
		return "Index Pixel -> (" + xIndex + ", " + yIndex + ")";
		
	}

	@Override public byte lookupX() {

		return (byte) xIndex;
		
	}

	@Override public byte lookupY() {

		return (byte) yIndex;
		
	}

	@Override public short unsignedLookupX() {

		return xIndex;
		
	}

	@Override public short unsignedLookupY() {

		return yIndex;
		
	}
	
}