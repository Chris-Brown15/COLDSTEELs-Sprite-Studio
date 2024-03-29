package cs.csss.project;

import java.util.function.Consumer;

class StaticLayerDataStore implements LayerDataStore {

	private volatile LayerPixel[][] image;
	private final int 
		width ,
		height;
	
	private volatile int mods = 0;
	
	public StaticLayerDataStore(final int width , final int height) {

		this.width = width;
		this.height = height;
		
		image = new LayerPixel[height][width];
		
	}
	
	@Override public synchronized int mods() {
		
		return mods;
		
	}

	@Override public synchronized boolean modifiesAtIndex(int xIndex, int yIndex) {

		return get(xIndex, yIndex) != null;
		
	}

	@Override public synchronized LayerPixel get(int xIndex, int yIndex) {

		return image[yIndex][xIndex];
		
	}

	@Override public synchronized void put(final LayerPixel putThis) {

		if(!modifiesAtIndex(putThis.textureX , putThis.textureY)) mods++;
		image[putThis.textureY][putThis.textureX] = putThis;
		
	}

	@Override public synchronized LayerPixel remove(int xIndex, int yIndex) {
		
		LayerPixel removed = image[yIndex][xIndex];
		if(removed != null) mods--;
		image[yIndex][xIndex] = null;
		return removed;
		
	}

	@Override public synchronized void forEach(Consumer<LayerPixel> callback) {

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
