package cs.csss.project;

import cs.csss.engine.LookupPixel;
import cs.csss.engine.Pixel;
import cs.csss.engine.TexturePixel;

/**
 * Layer pixels are CPU-only pixels that {@link Layer}s use to store modifications to the artboard on the CPU. These pixels are both
 * {@link cs.csss.engine.TexturePixel TexturePixel}s and {@link cs.csss.engine.LookupPixel LookupPixel}s, so they provide a useful API.
 */
public class LayerPixel implements TexturePixel , LookupPixel {

	/**
	 * Indices into the texture this pixel corresponds to.
	 */
	public final int textureX , textureY;
	
	/**
	 * Indices into the palette this pixel corresponds to.
	 */
	public final short lookupX , lookupY;
	
	/**
	 * Creates a new layer pixel from the given indices.
	 * 
	 * @param textureX — x index into a texture/layer this pixel belongs to 
	 * @param textureY — y index into a texture/layer this pixel belongs to 
	 * @param lookupX — x index into a palette to lookup for selecting the color for this pixel
	 * @param lookupY — y index into a palette to lookup for selecting the color for this pixel
	 */
	public LayerPixel(final int textureX , final int textureY , final byte lookupX , final byte lookupY) {
		
		this(textureX , textureY , (short) Byte.toUnsignedInt(lookupX) , (short) Byte.toUnsignedInt(lookupY));
		
	}

	/**
	 * Creates a new layer pixel from the given texture indices and the existing lookup pixel.
	 * 
	 * @param textureX — x index into a texture/layer this pixel belongs to 
	 * @param textureY — y index into a texture/layer this pixel belongs to 
	 * @param source — an existing lookup pixel whose values are assigned to this one 
	 */
	public LayerPixel(final int textureX , final int textureY , LookupPixel source) {
		
		this(textureX , textureY , source.unsignedLookupX() , source.unsignedLookupY());
		
	}

	/**
	 * Creates a new layer pixel from the given indices.
	 * 
	 * @param textureX — x index into a texture/layer this pixel belongs to 
	 * @param textureY — y index into a texture/layer this pixel belongs to 
	 * @param lookupX — x index into a palette to lookup for selecting the color for this pixel
	 * @param lookupY — y index into a palette to lookup for selecting the color for this pixel
	 */
	public LayerPixel(final int textureX , final int textureY , final short lookupX , final short lookupY) {

		this.textureX = textureX;
		this.textureY = textureY;
		
		this.lookupX = lookupX;
		this.lookupY = lookupY;
		
	}
	
	@Override public String toString() {
		
		return "Pixel at (" + textureX + ", " + textureY + ")";				
		
	}

	@Override public int textureX() {

		return textureX;
		
	}

	@Override public int textureY() {

		return textureY;
		
	}

	@Override public byte lookupX() {

		return (byte) lookupX;
		
	}

	@Override public byte lookupY() {

		return (byte) lookupY;
		
	}

	@Override public short unsignedLookupX() {

		return lookupX;
		
	}

	@Override public short unsignedLookupY() {

		return lookupY;
		
	}

	@Override public Pixel copyOf() {

		return new LayerPixel(textureX , textureY , lookupX , lookupY);
		
	}
	
}
