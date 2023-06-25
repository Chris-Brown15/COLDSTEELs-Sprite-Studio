package cs.csss.artboard;

import java.util.function.Consumer;

interface LayerDataStore  {

	public boolean modifiesAtIndex(final int xIndex , final int yIndex);
	
	public LayerPixel get(final int xIndex , final int yIndex);

	public void put(final LayerPixel putThis);
	
	public LayerPixel remove(final int xIndex , final int yIndex);
	
	public default LayerPixel[][] get(final int xIndex , final int yIndex , final int width , final int height) {
		
		LayerPixel[][] region = new LayerPixel[height][width];
		for(int row = yIndex ; row < yIndex + height ; row++) for(int col = xIndex ; col < xIndex + width ; col++) { 
			
			region[row][col] = get(col , row);
			
		}
		
		return region;
		
	}
	
	public default void put(final LayerPixel[][] putThis) {
		
		for(LayerPixel[] row : putThis) for(LayerPixel x : row) put(x);
		
	}
	
	public default LayerPixel[][] remove(final int xIndex , final int yIndex , final int width , final int height) {
		
		LayerPixel[][] region = new LayerPixel[height][width];
		for(int row = yIndex ; row < yIndex + height ; row++) for(int col = xIndex ; col < xIndex + width ; col++) { 
			
			region[row][col] = remove(col , row);
			
		}
		
		return region;
		
	}
	
	default void copy(final LayerDataStore copyThis) {
		
		copyThis.forEach(this::put);
		
	}
	
	void forEach(Consumer<LayerPixel> callback);

}
