package cs.csss.project.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for a 2D area, typically and likely used for pixel regions. The returned type of {@link RegionIterator#next() next()} is an 
 * {@code int[]}. The first index of this array is the iterators current column position, and the second index is the iterators current row
 * position. The same array returned every time, but its values will change with each call to {@code next}.
 */
public class RegionIterator implements Iterator<RegionPosition> {

	RegionPosition position;
	
	/**
	 * Information for the iteration process.
	 */
	public final int
		startCol ,
		maxRow ,
		maxCol;

	int col , row;
	
	RegionIterator(int startX , int startY , int width , int height) {

		startCol = startX;
		maxCol = startCol + width;
		maxRow = startY + height;
		position = new RegionPosition(col = startX , row = startY);
		
	}

	/**
	 * Returns the current row of the iteration.
	 * 
	 * @return Current row of this iteration.
	 */
	public int row() {
		
		return position.row;
		
	}
	
	/**
	 * Returns the current column of the iteration.
	 * 
	 * @return Current column of this iteration.
	 */
	public int col() {
		
		return position.col;
		
	}
	
	@Override public boolean hasNext() {

		return col < maxCol && row < maxRow;
		
	}

	@Override public RegionPosition next() {
		
		if(!hasNext()) throw new NoSuchElementException("This iterator has completed.");
		
		position.col = col;
		position.row = row;
		
		col++;
		if(col == maxCol) {
			
			col = startCol;
			row++;
			
		}
				
		return position;
		
	}

}
