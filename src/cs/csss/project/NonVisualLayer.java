package cs.csss.project;

import cs.csss.annotation.RenderThreadOnly;

/**
 * Layer containing nonvisual data. Nonvisual data is like image data and is created the same way as visual data, but can be treated as 
 * something other than visual data.
 */
public class NonVisualLayer extends Layer {

	private final int bytesPerPixel;
	
	NonVisualLayer(Artboard artboard , ArtboardPalette palette , NonVisualLayerPrototype prototype) {

		super(prototype.name() , palette , artboard.width() , artboard.height());
		this.bytesPerPixel = prototype.sizeBytes();		
		
	}

	@RenderThreadOnly @Override public void hide(Artboard artboard) {

		forEachModification(pixel -> {
			
			artboard.writeToIndexTexture(
				pixel.textureX , 
				pixel.textureY , 
				1 , 
				1 , 
				palette , 
				artboard.getBackgroundColor(pixel.textureX , pixel.textureY)
			);
			
		});
		
		hiding = true;
		
	}

	@RenderThreadOnly @Override public void show(Artboard artboard) {

		forEachModification(pixel -> {
			
			artboard.writeToIndexTexture(pixel.textureX, pixel.textureY, 1, 1, palette.getColorByIndices(pixel.lookupX, pixel.lookupY));
			
		});
		
		hiding = false;
		
	}
	
	@Override public int pixelSizeBytes() {
		
		return bytesPerPixel;
		
	}

	@Override public boolean hiding() {
		
		return hiding;
		
	}
	
	/**
	 * Returns the number of bytes per pixel of this layer.
	 * 
	 * @return Number of bytes per pixel of this layer.
	 */
	public int bytesPerPixel() {
		
		return bytesPerPixel;
		
	}
	
	void hiding(boolean hiding) {
		
		this.hiding = hiding;
		
	}

	@Override public <T extends Layer> void copy(T otherLayer) {

		layerDataStore.copy(otherLayer.layerDataStore);
		
	}
	
	/**
	 * Returns whether this layer is an instance of the given prototype.
	 * 
	 * @param prototype — a nonvisual layer prototype
	 * @return {@code true} if this layer is an instance of the given prototype.
	 */ 
	public boolean isInstanceOfPrototype(NonVisualLayerPrototype prototype) {
		
		return prototype.sizeBytes() == bytesPerPixel && prototype.name().equals(name);
		
	}

}
