package cs.csss.engine;

/**
 * Used for tracking the position of an object and moving it around world space. Implementations define how to move an object, but this
 * abstract class is used for tracking the changes. This class requires the initial positions to not be translated. This class also requires
 * its positions to be laid out as simple positions, with no other data in the array.
 */
public abstract class Position {

	private final float[] positions;
	
	public Position(float[] positions) {

		this.positions = positions;
		
	}

	/**
	 * Moves this position by {@code x} along the x axis and {@code y} along the y axis.
	 * 
	 * @param x — amount to move horizontally
	 * @param y — amount to move vertically
	 */
	public abstract void translate(int x , int y);

	/**
	 * Moves this position by {@code x} along the x axis and {@code y} along the y axis.
	 * 
	 * @param x — amount to move horizontally
	 * @param y — amount to move vertically
	 */
	public abstract void translate(float x , float y);

	/**
	 * Updates the positions of this {@code Position} object. Should be called when other translations occur
	 * 
	 * @param x — x translation
	 * @param y — y translation
	 */
	protected final void updatePositions(float x , float y) {
	
		positions[0] += x;
		positions[2] += x;
		positions[4] += x;
		positions[6] += x;		
		positions[1] += y;
		positions[3] += y;
		positions[5] += y;
		positions[7] += y;
		
	}
	
	/**
	 * Moves this object to the given coordinates.
	 * 
	 * @param xPosition — x world coordinate to move to
	 * @param yPosition — y world coordinate to move to
	 */
	public void moveTo(float xPosition , float yPosition) {
	
		translate(-midX() + xPosition , -midY() + yPosition);
			
	}	

	/**
	 * Moves this object to the given coordinates.
	 * 
	 * @param xPosition — x world coordinate to move to
	 * @param yPosition — y world coordinate to move to
	 */
	public void moveTo(int xPosition , int yPosition) {
	
		translate(-midX() + xPosition , -midY() + yPosition);
			
	}	

	/**
	 * Returns the width of this artboard.
	 * 
	 * @return — width of this artboard.
	 */
	public int width() {
		
		return (int) (rightX() - leftX());
		
	}

	/**
	 * Returns the height of this artboard.
	 * 
	 * @return — height of this artboard.
	 */
	public int height() {
		
		return (int) (topY() - bottomY());
		
	}

	/**
	 * Returns the top Y coordinate (in world space) of this artboard.
	 * 
	 * @return — top Y coordinate of this artboard.
	 */
	public float topY() {
		
		return positions[1];
		
	}

	/**
	 * Returns the bottom Y coordinate (in world space) of this artboard.
	 * 
	 * @return — bottom Y coordinate of this artboard.
	 */
	public float bottomY() {
		
		return positions[3];
		
	}

	/**
	 * Returns the left X coordinate (in world space) of this artboard.
	 * 
	 * @return — left X coordinate of this artboard.
	 */
	public float leftX() {
		
		return positions[4];
		
	}

	/**
	 * Returns the right X coordinate (in world space) of this artboard.
	 * 
	 * @return — right X coordinate of this artboard.
	 */
	public float rightX() {
		
		return positions[0];
		
	}

	/**
	 * Returns the midpoint Y coordinate (in world space) of this artboard.
	 * 
	 * @return — midpoint X coordinate of this artboard.
	 */
	public float midX() {
		
		return leftX() + (width() / 2);
		
	}

	/**
	 * Returns the midpoint Y coordinate (in world space) of this artboard.
	 * 
	 * @return — midpoint Y coordinate of this artboard.
	 */
	public float midY() {
		
		return bottomY() + (height() / 2);
		
	}
	
}
