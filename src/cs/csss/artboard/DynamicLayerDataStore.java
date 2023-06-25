package cs.csss.artboard;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

class DynamicLayerDataStore implements LayerDataStore {

	private final LinkedList<LayerPixel> mods = new LinkedList<>();
	
	public DynamicLayerDataStore() {}

	@Override public boolean modifiesAtIndex(int xIndex, int yIndex) {

		boolean modifies = false;
		
		for(LayerPixel x : mods) if(x.textureX == xIndex && x.textureY == yIndex) { 
			
			modifies = true;
			break;
			
		}
		
		return modifies;
		
	}

	@Override public LayerPixel get(int xIndex, int yIndex) {

		for(LayerPixel x : mods) if(x.textureX == xIndex && x.textureY == yIndex) return x;				
		return null;
		
	}

	@Override public void put(final LayerPixel putThis) {

		int 
			i = 0 ,
			previousY = -1 ,
			previousX = -1
		; 
		
		for(LayerPixel x : mods) {
			
			if(x.textureY != previousY) {
				
				if(previousY < putThis.textureY && putThis.textureY < x.textureY) break;				
				
			}
			
			previousY = x.textureY;
			
			if(previousY == putThis.textureY) {
				
				if(previousX < putThis.textureX && putThis.textureX < x.textureX) break;
				
			} else if (x.textureX == putThis.textureX) return;
			
			previousX = x.textureX;
			
			i++;
			
		}
		
		mods.add(i , putThis);
		
	}
	
	@Override public LayerPixel remove(int xIndex, int yIndex) {
		
		Iterator<LayerPixel> iter = mods.iterator();
		LayerPixel x = null;
		while(iter.hasNext()) {
			
			x = iter.next(); 
			if(x.textureX == xIndex && x.textureY == yIndex) {
				
				iter.remove();
				break;
				
			}
			
		}
		
		return x;
		
	}

	@Override public void forEach(Consumer<LayerPixel> callback) {

		mods.forEach(callback);
		
	}
	
}
