/**
 * 
 */
package cs.csss.project.utils;

/**
 * Used to return region iteration positions to a user of {@link RegionIterator}.
 */
public class RegionPosition {

	int col , row;
	
	/**
	 * 
	 */
	RegionPosition(int initialCol , int initialRow) {

		this.col = initialCol;
		this.row = initialRow;
		
	}

	/**
	 * Returns the current column
	 * 
	 * @return Current column.
	 */
	public int col() {
		
		return col;
		
	}
	
	/**
	 * Returns the current row.
	 * 
	 * @return Current row.
	 */
	public int row() {
		
		return row;
		
	}
	
}
