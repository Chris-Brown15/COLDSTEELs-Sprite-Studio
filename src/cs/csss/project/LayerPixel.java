package cs.csss.project;

public final class LayerPixel {

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

	public LayerPixel(final int textureX , final int textureY , final short lookupX , final short lookupY) {

		this.textureX = textureX;
		this.textureY = textureY;
		
		this.lookupX = lookupX;
		this.lookupY = lookupY;
		
	}
	
	@Override public String toString() {
		
		return "Pixel at (" + textureX + ", " + textureY + ")";				
		
	}
	
}
