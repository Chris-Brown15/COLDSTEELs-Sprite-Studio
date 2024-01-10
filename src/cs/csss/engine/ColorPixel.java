package cs.csss.engine;

import static cs.core.utils.CSUtils.require;

import java.nio.ByteBuffer;

/**
 * Color pixels are pixels whose components describe a color. They are therefore distinct from {@link LookupPixel} because {@code LookupPixel}
 * components describe a location on a palette. A color pixel describes color values.
 * <p>
 * 	Not all colors have the same number of components. Therefore, if the value {@code -1} is returned, the pixel described by a {@code ColorPixel}
 * 	does not have a value for the given component. In all other cases, the returned value should be {@code 0 <= comp <= 255}.
 * </p>
 */
public interface ColorPixel extends Pixel {

	/**
	 * Stores {@code pixel} in this buffer. the number of channel values put is {@code pixelSizeBytes}. 
	 * 
	 * @param buffer — buffer to write to 
	 * @param pixel — a pixel to store in the buffer
	 * @param pixelSizeBytes — number of bytes {@code pixel} contains
	 */
	public static void buffer(ByteBuffer buffer , ColorPixel pixel , int pixelSizeBytes) {
		
		require(buffer.remaining() >= pixelSizeBytes);
		
		for(int i = 0 ; i < pixelSizeBytes ; i ++) switch(i) {
			case 0 -> buffer.put(pixel.r());
			case 1 -> buffer.put(pixel.g());
			case 2 -> buffer.put(pixel.b());
			case 3 -> buffer.put(pixel.a());
		}
		
	}
	
	/**
	 * Returns the red component of a pixel.
	 * 
	 * @return Red component of a pixel.
	 */
	public byte r();

	/**
	 * Returns the green component of a pixel.
	 * 
	 * @return Green component of a pixel.
	 */
	public byte g();

	/**
	 * Returns the blue component of a pixel.
	 * 
	 * @return Blue component of a pixel.
	 */
	public byte b();

	/**
	 * Returns the alpha component of a pixel.
	 * 
	 * @return Alpha component of a pixel.
	 */
	public byte a();

	/**
	 * Returns the channel associated with the index.
	 * 
	 * @param index — index of a channel , {@code 0 <= index <= 3}.
	 * @return Channel value of a pixel.
	 */
	public default byte i(int index) {
		
		return switch(index) {
		
			case 0 -> r();
			case 1 -> g();
			case 2 -> b();
			case 3 -> a();
			default -> throw new IllegalArgumentException("Unexpected value: " + index);
		
		};
		
	}
	
	/**
	 * Returns an unsigned form of the red value of a pixel.
	 * 
	 * @return Unsigned form of the red value of a pixel.
	 */
	public short ur();

	/**
	 * Returns an unsigned form of the green value of a pixel.
	 * 
	 * @return Unsigned form of the green value of a pixel.
	 */
	public short ug();

	/**
	 * Returns an unsigned form of the blue value of a pixel.
	 * 
	 * @return Unsigned form of the blue value of a pixel.
	 */
	public short ub();

	/**
	 * Returns an unsigned form of the alpha value of a pixel.
	 * 
	 * @return Unsigned form of the alpha value of a pixel.
	 */
	public short ua();
	
	/**
	 * Returns the unsigned form of the channel associated with the index.
	 * 
	 * @param index — index of a channel , {@code 0 <= index <= 3}.
	 * @return Unsigned from of the channel value of a pixel.
	 */
	public default short ui(int index) {
		
		return switch(index) {
		
			case 0 -> ur();
			case 1 -> ug();
			case 2 -> ub();
			case 3 -> ua();
			default -> throw new IllegalArgumentException("Unexpected value: " + index);
		
		};
		
	}
	
	/**
	 * Returns an integer representing the values of this pixel.
	 * 
	 * @return Integer representation of this pixel.
	 */
	public default int pixelAsInt() {
		
		return ((255 & r()) << 24) | ((255 & g()) << 16) | ((255 & b()) << 8) | a();
				
	}
	
	@Override public default int compareTo(Pixel other) {
		
		if(other instanceof ColorPixel asColor) return asColor.r() == r() && asColor.g() == g() && asColor.b() == b() && asColor.a() == a() ? 0 : -1;
		return -1;						
		
	}
	
}

