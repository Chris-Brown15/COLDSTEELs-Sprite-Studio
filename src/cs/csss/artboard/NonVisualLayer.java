package cs.csss.artboard;

public class NonVisualLayer extends Layer {

	private final int bytesPerPixel;
	
	public NonVisualLayer(Artboard artboard , ArtboardPalette palette , NonVisualLayerPrototype prototype) {

		super(prototype.name() , palette , artboard.width() , artboard.height());
		this.bytesPerPixel = prototype.sizeBytes();		
		
	}

	@Override public void hide(Artboard artboard) {

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

	@Override public void show(Artboard artboard) {

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
	
	public int bytesPerPixel() {
		
		return bytesPerPixel;
		
	}

}
