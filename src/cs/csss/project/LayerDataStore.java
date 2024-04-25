package cs.csss.project;

import java.util.function.Consumer;

interface LayerDataStore  {

	public int mods();
	
	public boolean modifiesAtIndex(final int xIndex , final int yIndex);
	
	public LayerPixel get(final int xIndex , final int yIndex);

	public void put(final LayerPixel putThis);
	
	public LayerPixel remove(final int xIndex , final int yIndex);
	
	/**
	 * Gets a two dimensional array of layer modifications representing a subregion of this layer's modification contents.
	 * 
	 * @param xIndex — x index of the bottom left corner of the subregion to query
	 * @param yIndex — y index of the bottom left corner of the subregion to query
	 * @param width — width of the subregion to query
	 * @param height — height of the subregion to query
	 * @return Two dimensional array containing modifications of this layer.
	 */
	public default LayerPixel[][] get(int xIndex , int yIndex , int width , int height) {
		
		LayerPixel[][] region = new LayerPixel[height][width];
		for(int row = 0 ; row < height ; row++ , yIndex++) for(int col = 0 , xOffset = xIndex; col < width ; col++ , xOffset++) {
			
			region[row][col] = get(xOffset , yIndex);
			
		}
		
		return region;
		
	}
	
	public default void put(final LayerPixel[][] putThis) {
		
		for(LayerPixel[] row : putThis) for(LayerPixel x : row) put(x);
		
	}
	
	public default LayerPixel[][] remove(int xIndex , int yIndex , final int width , final int height) {
		
		LayerPixel[][] region = new LayerPixel[height][width];
		for(int row = 0 ; row < height ; row++ , yIndex++) for(int col = 0 , xOffset = xIndex; col < width ; col++ , xOffset++) {
			
			region[row][col] = remove(xOffset , yIndex);
			
		}
		
		return region;
		
	}
	
	default void copy(final LayerDataStore destination) {
		
		forEach(destination::put);
		
	}
	
	void forEach(Consumer<LayerPixel> callback);

}
