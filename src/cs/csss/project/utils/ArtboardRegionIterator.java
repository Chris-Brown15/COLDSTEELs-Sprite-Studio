package cs.csss.project.utils;

import java.util.Iterator;

import cs.csss.project.Artboard;
import cs.csss.project.IndexPixel;

/**
 * Iterator over a region of an artboard.
 */
public class ArtboardRegionIterator implements Iterator<IndexPixel> {

	private final Artboard artboard;
	private final RegionIterator iterator;
	private int[] currentIndices;
	
	ArtboardRegionIterator(Artboard artboard , int col , int row , int width , int height) {
		
		this.artboard = artboard;
		iterator = Artboards.region(col, row, width, height);
					
	}		
	
	/**
	 * Returns the current row of the iteration.
	 * 
	 * @return Current row of this iteration.
	 */
	public int row() {
		
		return iterator.row;
		
	}
	
	/**
	 * Returns the current column of the iteration.
	 * 
	 * @return Current column of this iteration.
	 */
	public int col() {
		
		return iterator.col;
		
	}
	
	@Override public boolean hasNext() {

		return iterator.hasNext();
		
	}

	@Override public IndexPixel next() {

		IndexPixel next = artboard.getIndexPixelAtIndices(iterator.col , iterator.row);
		currentIndices = iterator.next();
		
		return next;
		
	}
	
	@Override public void remove() {
		
		artboard.removePixel(currentIndices[0],  currentIndices[1]);
		
	}
	
}