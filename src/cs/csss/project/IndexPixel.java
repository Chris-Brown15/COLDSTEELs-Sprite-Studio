package cs.csss.project;

import static cs.core.utils.CSUtils.require;
import static cs.core.utils.CSUtils.specify;

import java.nio.ByteBuffer;

import cs.csss.engine.LookupPixel;
import cs.csss.engine.Pixel;

/**
 * Representation of a lookup pixel. A lookup pixel is a pixel containing primitives as channels which are used as lookups into a palette 
 * texture. So the x and y values of this lookup pixel correspond to the color located in a palette at the x and y coordinates within the 
 * texture of the palette.
 * 
 */
public class IndexPixel implements LookupPixel {

	public final short xIndex , yIndex;
	
	/**
	 * Creates an index pixel with the given values.
	 * 
	 * @param xIndex — x lookup of this index pixel
	 * @param yIndex — y lookup of this index pixel
	 */
	public IndexPixel(short xIndex , short yIndex) {
	
		specify(xIndex >= 0 , xIndex + " is an invalid x index") ; specify(yIndex >= 0 , yIndex + " is an invalid y index");
		
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		
	}	

	/**
	 * Creates an index pixel with the given values.
	 * 
	 * @param xIndex — x lookup of this index pixel
	 * @param yIndex — y lookup of this index pixel
	 */
	public IndexPixel(int xIndex , int yIndex) {

		specify(xIndex >= 0 , xIndex + " is an invalid x index") ; specify(yIndex >= 0 , yIndex + " is an invalid y index");
		
		this.xIndex = (short) xIndex;
		this.yIndex = (short) yIndex;
		
	}	

	/**
	 * Creates an index pixel that uses the given buffer's next two bytes as its values.
	 * 
	 * @param buffer — a bytebuffer to read from
	 */
	public IndexPixel(ByteBuffer buffer) {
		
	 	xIndex = (short)Byte.toUnsignedInt(buffer.get());
	 	yIndex = (short)Byte.toUnsignedInt(buffer.get());
		
	}

	/**
	 * Puts the contents of this index pixel into the next two bytes of {@code buffer}.
	 *  
	 * @param buffer — destination for the values of this pixel
	 */
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

	@Override public Pixel clone() {

		return new IndexPixel(xIndex , yIndex);
		
	}
	
}