package cs.csss.engine;

/**
 * Color pixels are pixels whose components describe a color. They are therefore distinct from {@link LookupPixel} because {@code LookupPixel}
 * components describe a location on a palette. A color pixel describes color values.
 * <p>
 * 	Not all colors have the same number of components. Therefore, if the value {@code -1} is returned, the pixel described by a {@code ColorPixel}
 * 	does not have a value for the given component. In all other cases, the returned value should be {@code 0 <= comp <= 255}.
 * </p>
 */
public interface ColorPixel extends Comparable<ColorPixel> {

	/**
	 * Returns a distinct color from the values of {@code source}. If {@code source} is a mutable pixel, changes to its values will <em>not</em> 
	 * affect the resulting pixel.
	 * 
	 * @param source — a source pixel
	 * @return A channel-wise deep copy of {@code source}.
	 */
	public static ColorPixel copyOf(ColorPixel source) {
		
		return new Color(source.r() , source.g() , source.b() , source.a());
		
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
	
	@Override public default int compareTo(ColorPixel other) {
		
		if(other == null) return -1;						
		return other.r() == r() && other.g() == g() && other.b() == b() && other.a() == a() ? 0 : -1;
		
	}
	
}
