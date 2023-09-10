package cs.csss.project;

public class LayerCachedLayerPixel extends LayerPixel {

	public final Layer source;
	
	public LayerCachedLayerPixel(Layer source , int textureX, int textureY, byte lookupX, byte lookupY) {

		super(textureX, textureY, lookupX, lookupY);
		
		this.source = source;
		
	}

	public LayerCachedLayerPixel(Layer source , int textureX, int textureY, short lookupX, short lookupY) {
		
		super(textureX, textureY, lookupX, lookupY);
		this.source = source;
		
	}

}
