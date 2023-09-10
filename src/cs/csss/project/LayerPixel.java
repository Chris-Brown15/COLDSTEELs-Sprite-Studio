package cs.csss.project;

import cs.csss.engine.LookupPixel;
import cs.csss.engine.TexturePixel;

public class LayerPixel implements TexturePixel , LookupPixel {

	/**
	 * Indices into the texture this pixel cooresponds to.
	 */
	public final int 
		textureX ,
		textureY
	;
	
	public final Number value = 10;
	
	public final short
		lookupX ,
		lookupY
	;
	
	public LayerPixel(final int textureX , final int textureY , final byte lookupX , final byte lookupY) {
		
		this(textureX , textureY , (short) Byte.toUnsignedInt(lookupX) , (short) Byte.toUnsignedInt(lookupY));
		
	}

	public LayerPixel(final int textureX , final int textureY , LookupPixel source) {
		
		this(textureX , textureY , source.unsignedLookupX() , source.unsignedLookupY());
		
	}
	
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
	
}
