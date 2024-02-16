/**
 * 
 */
package cs.csss.engine;

/**
 * Color pixel for a color palette from an implementation of {@link cs.csss.editor.palette.ColorPalette ColorGenerator}
 */
public class Color implements ColorPixel {

	/**
	 * Channel values.
	 */
	public final byte r , g , b , a;
	
	/**
	 * Creates a color palette pixel from the given values
	 * @param r — red channel value
	 * @param g — green channel value
	 * @param b — blue channel value
	 * @param a — alpha channel value
	 */
	public Color(byte r , byte g , byte b , byte a) {

		this.r = r ; this.g = g ; this.b = b ; this.a = a;
		
	}

	@Override public byte r() {

		return r;
		
	}

	@Override public byte g() {

		return g;
		
	}

	@Override public byte b() {
		
		return b;
		
	}

	@Override public byte a() {
		
		return a;
		
	}

	@Override public short ur() {
		
		return (short)Byte.toUnsignedInt(r);
		
	}

	@Override public short ug() {
		
		return (short)Byte.toUnsignedInt(g);
	}

	@Override public short ub() {
		
		return (short)Byte.toUnsignedInt(b);
		
	}

	@Override public short ua() {
		
		return (short)Byte.toUnsignedInt(a);
		
	}

	@Override public String toString() {
		
		return String.format("R: %d , G: %d , B: %d , A: %d", r() , g() , b() , a());
		
	}

	@Override public Pixel clone() {

		return new Color(r , g , b , a);		
		
	}
	
}
