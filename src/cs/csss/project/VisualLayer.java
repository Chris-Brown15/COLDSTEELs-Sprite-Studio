package cs.csss.project;

import cs.csss.project.ArtboardPalette.PalettePixel;

/**
 * Visual layers are layers that contain visual data (pixels). This class implements {@link cs.csss.project.Layer Layer}, which has more 
 * information.
 * 
 * @author Chris Brown
 */
public class VisualLayer extends Layer {

	private int channels;
	
	VisualLayer(Artboard artboard, ArtboardPalette palette , VisualLayerPrototype prototype) {

		super(prototype.name() , palette , artboard.width() , artboard.height());		
		channels = palette.channelsPerPixel();		
		
	}
	
	@Override public boolean isModifying(final int xIndex , final int yIndex) {
		
		return !hiding && super.isModifying(xIndex, yIndex);
		
	}
	
	public void toggleHiding() {
		
		hiding = !hiding;
		
	}

	public void hiding(boolean hiding) {
		
		this.hiding = hiding;
		
	}
	
	@Override public void show(Artboard artboard) {

		int thisRank = artboard.getLayerRank(this);
		
		forEachModification(pixel -> {
			
			//show this pixel if no layer of a greater rank is modifying that position			
			if(!artboard.isUpperRankLayerModifying(thisRank, pixel.textureX, pixel.textureY)) {
			
				PalettePixel palettePixel = artboard.getColorFromIndicesOfPalette(pixel.lookupX, pixel.lookupY);
				artboard.writeToIndexTexture(pixel.textureX, pixel.textureY, 1, 1, palettePixel);
				
			}
		
		});
		
		hiding = false;
		
	}
	
	@Override public void hide(Artboard artboard) {

		int thisRank = artboard.getLayerRank(this);
		
		forEachModification(pixel -> {

			if(artboard.isUpperRankLayerModifying(thisRank, pixel.textureX, pixel.textureY)) return;
			
			VisualLayer modding = artboard.getHighestLowerRankLayerModifying(thisRank , pixel.textureX , pixel.textureY);
			
			//only worry about modifying the artboard if this pixel is visible, which is the case if:
			if(modding != null) {
			
				//pixel of the layer who is now modifying if we hide the layer we're trying to hide 
				LayerPixel moddingPixel = modding.get(pixel.textureX, pixel.textureY);
				
				//palette pixel pointed to by that pixel
				PalettePixel palettePixel = artboard.getColorFromIndicesOfPalette(moddingPixel.lookupX, moddingPixel.lookupY);
				
				artboard.writeToIndexTexture(pixel.textureX, pixel.textureY, 1, 1, palettePixel);
				
			}
			//make the pixel the default color
			else artboard.writeToIndexTexture(
				pixel.textureX , 
				pixel.textureY , 
				1 , 
				1 , 
				artboard.getBackgroundColor(pixel.textureX, pixel.textureY)
			);
			
		});
	
		hiding = true;
		
	}
	
	@Override public int pixelSizeBytes() {
		
		return channels;
		
	}
	
	@Override public boolean hiding() {
		
		return hiding;
		
	}
	
	public boolean isInstanceOfPrototype(VisualLayerPrototype prototype) {
		
		return this.name.equals(prototype.name());
		
	}

	@Override public <T extends Layer> void copy(T otherLayer) {

		layerDataStore.copy(otherLayer.layerDataStore);
		
	}

}
