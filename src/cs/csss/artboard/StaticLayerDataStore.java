package cs.csss.artboard;

import java.util.function.Consumer;

class StaticLayerDataStore implements LayerDataStore {

	private final LayerPixel[][] image;
	private final int 
		width ,
		height
	;
	
	private int mods = 0;
	
	public StaticLayerDataStore(final int width , final int height) {

		this.width = width;
		this.height = height;
		
		image = new LayerPixel[height][width];
		
	}
	
	public int mods() {
		
		return mods;
		
	}

	@Override public boolean modifiesAtIndex(int xIndex, int yIndex) {

		return get(xIndex, yIndex) != null;
		
	}

	@Override public LayerPixel get(int xIndex, int yIndex) {

		return image[yIndex][xIndex];
		
	}

	@Override public void put(final LayerPixel putThis) {

		if(image[(int) putThis.textureY][(int) putThis.textureX] == null) mods++;
		image[(int) putThis.textureY][(int) putThis.textureX] = putThis;
		
	}

	@Override public LayerPixel remove(int xIndex, int yIndex) {
		
		LayerPixel removed = image[yIndex][xIndex];
		image[yIndex][xIndex] = null;
		if(removed != null) mods--;
		return removed;
		
	}

	@Override public void forEach(Consumer<LayerPixel> callback) {

		int found = 0;
		
		for(int row = 0 ; row < height ; row++) for(int col = 0 ; col < width ; col++) {
			
			LayerPixel x = image[row][col]; 
			if(x != null) { 
				
				callback.accept(x);
				found++;
				if(found == mods) return;
				
			}
			
		}		
		
	}

}
