package cs.csss.project;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

public class AtomicLayerDataStore implements LayerDataStore {

	//2D atomic reference array of layer pixels; row major
	private final AtomicReferenceArray<AtomicReferenceArray<LayerPixel>> image;
	private final AtomicInteger mods = new AtomicInteger(0);
		
	public final int
		width ,
		height;
	
	AtomicLayerDataStore(int width , int height) { 

		this.width = width;
		this.height = height;
		
		image = new AtomicReferenceArray<>(height);
		for(int i = 0 ; i < height ; i++) image.set(i, new AtomicReferenceArray<>(width));
		
	}
	
	@Override public boolean modifiesAtIndex(int xIndex, int yIndex) {

		return image.get(yIndex).get(xIndex) != null;
		
	}

	@Override public LayerPixel get(int xIndex, int yIndex) {

		return image.get(yIndex).get(xIndex);
		
	}

	@Override public void put(LayerPixel putThis) {
		
		Objects.requireNonNull(putThis);
		
		AtomicReferenceArray<LayerPixel> col = image.get(putThis.textureY);
		int x = putThis.textureX;
		if(col.get(x) == null) mods.incrementAndGet();
		col.set(x , putThis);
				
	}

	@Override public LayerPixel remove(int xIndex, int yIndex) {

		AtomicReferenceArray<LayerPixel> col = image.get(yIndex);
		LayerPixel removed = col.getAndSet(xIndex, null);
		if(removed != null) mods.decrementAndGet();
		return removed;
		
	}

	@Override public void forEach(Consumer<LayerPixel> callback) {
		
		int found = 0;
		int neededMods = mods.get();
		
		for(int i = 0 ; i < height ; i++) {
			
			AtomicReferenceArray<LayerPixel> col = image.get(i);
			for(int j = 0 ; j < width ; j++) { 
				
				LayerPixel x = col.get(j);
				if(x != null) {
					
					callback.accept(x);
					found++;
					if(found == neededMods) return;
					
				}
				
			}
			
		}

	}

	@Override public int mods() {

		return mods.get();
		
	}

}
