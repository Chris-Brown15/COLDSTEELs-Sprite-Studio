/**
 * 
 */
package cs.csss.editor.line;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.OptionalInt;

import org.joml.Vector2f;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.project.Artboard;

/**
 * Bezier line implementation. Bezier lines can have an arbitrary number of control points.
 */
public class BezierLine extends Line {

	private List<Vector2f> controlPoints = new ArrayList<>();
	
	/**
	 * Creates a new bezier line with no control points and no endpoints.
	 */
	public BezierLine() {}

	/**
	 * Adds a control point to this bezier line.
	 * 
	 * @param source artboard this bezier line belongs to.
	 * @param x x coordinate in artboard coordinates of the new control point
	 * @param y y coordinate in artboard coordinates of the new control point
	 * @throws NullPointerException if {@code source} is <code>null</code>.
	 */
	public void controlPoint(Artboard source , int x , int y) {
		
		Objects.requireNonNull(source);		
		
		int xFinal = correctifyControlPointX(source , x);
		int yFinal = correctifyControlPointY(source , y);
		
		if(controlPoints.stream().anyMatch(vector -> vector.x == xFinal && vector.y == yFinal)) return;
		
		controlPoints.add(new Vector2f(x , y));
		
	}
	
	/**
	 * Gets the {@code index}th control point.
	 * 
	 * @param index index of the control point
	 * @return The {@code index}th control point.
	 * @throws IndexOutOfBoundsException if {@code index} is out of bounds as a control point index.
	 */
	public Vector2f controlPoint(int index) {
	
		Objects.checkIndex(index, controlPoints.size());
		return controlPoints.get(index);
		
	}			
	
	/**
	 * Removes the control point at the given index.
	 * 
	 * @param index index of a control point to remove
	 * @return Removed control point.
	 * @throws IndexOutOfBoundsException if {@code index} is invalid as a control point index.
	 */
	public Vector2f removePoint(int index) {
		
		Objects.checkIndex(index, controlPoints.size());
		return controlPoints.remove(index);		
		
	}
	
	/**
	 * Adds the given vector as a point to this line.
	 * 
	 * @param artboard the artboard this line belongs to
	 * @param index index in the list of control points to add {@code point} to
	 * @param point point to add
	 * @throws NullPointerException if {@code point} or {@code artboard} is <code>null</code>.
	 * @throws IndexOutOfBoundsException if {@code index} is invalid as a control point list index.
	 */
	public void addPoint(Artboard artboard , int index , Vector2f point) {
		
		Objects.checkIndex(index, controlPoints.size() + 1);
		Objects.requireNonNull(point);
		Objects.requireNonNull(artboard);
		
		checkVector(artboard, point);

		controlPoints.add(index , point);
		
	}
	
	/**
	 * Adds the given vector as a control point at the end of the list of control points.
	 * 
	 * @param artboard the artboard this line belongs to
	 * @param point point to add
	 * @throws NullPointerException if {@code point} or {@code artboard} is <code>null</code>.
	 */
	public void addPoint(Artboard artboard , Vector2f point) {
		
		Objects.requireNonNull(point);
		Objects.requireNonNull(artboard);

		checkVector(artboard, point);

		controlPoints.add(point);
		
	}
	
	/**
	 * Moves the control point at {@code originalIndex} to {@code newIndex} in the list of control points. The control point at {@code newIndex} will
	 * be shifted forward by one, or if {@code newIndex} is equal to {@code originalIndex + 1}, the two are swapped.
	 * 
	 * @param originalIndex index of a control point to move
	 * @param newIndex index to move the control point at {@code originalIndex} to.
	 * @throws IndexOutOfBoundsException if either parameter is invalid as a control point index.
	 */
	public void movePoint(int originalIndex , int newIndex) {
		
		int size = controlPoints.size();
		Objects.checkIndex(originalIndex, size);
		Objects.checkIndex(newIndex, size);
		
		//swap
		if(newIndex == originalIndex + 1) {

			Vector2f move = controlPoints.get(originalIndex);
			Vector2f displaced = controlPoints.get(newIndex);
			
			controlPoints.set(newIndex , move);
		 	controlPoints.set(originalIndex , displaced);
		 	
		} else {

			Vector2f move = controlPoints.get(originalIndex);
			controlPoints.add(newIndex , move);
				
			if(newIndex > originalIndex) controlPoints.remove(originalIndex);
			else controlPoints.remove(originalIndex + 1);
			
		}
		
	}
	
	/**
	 * Returns the number of control points.
	 * 
	 * @return Number of control points.
	 */
	public int numberControlPoints() {
		
		return controlPoints.size();
		
	}
	
	/**
	 * Returns an iterator over the control points of this bezier line.
	 * 
	 * @return Iterator over control points of this bezier line.
	 */
	public Iterator<Vector2f> controlPoints() {
		
		return controlPoints.iterator();
		
	}
	
	@RenderThreadOnly @Override public void reset(Artboard target) {
	
		checkEndpoint1();
		checkEndpoint2();
		
		putModsInArtboard(target);

		Vector2f endpoint1Source = new Vector2f(endpoint1X , endpoint1Y);
		Vector2f endpoint2Source = new Vector2f(endpoint2X , endpoint2Y);

		List<Vector2f> points = new ArrayList<>(controlPoints.size() + 2);
		points.add(endpoint1Source);
		points.addAll(controlPoints);		
		points.add(endpoint2Source);

		float step = stepHeuristic(points);
		
		for(float t = 0 ; t <= 1f ; t += step) {
			
			List<Vector2f> point = interpolateList(points , t);
			assert point.size() == 1;
			
			Vector2f p = point.get(0);
			
			mod(target, (int)p.x() , (int)p.y());
					
		}
	
		lineMods.forEach(mod -> target.putColorInImage(mod.textureX(), mod.textureY(), mod.width(), mod.height(), color));
		
	}

	private List<Vector2f> interpolateList(List<Vector2f> sources , float t) {
		
		assert sources.size() >= 2;
		
		List<Vector2f> result = new ArrayList<>(sources.size() - 1);
		
		Iterator<Vector2f> sourcesIter = sources.iterator();
		
		Vector2f i1 = sourcesIter.next();
		Vector2f p2MultiplicationIntermediate = new Vector2f();
		Vector2f destination;
				
		while(sourcesIter.hasNext()) {
			
			Vector2f i2 = sourcesIter.next();
			destination = new Vector2f();
			interpolate(t , i1 , i2 , p2MultiplicationIntermediate , destination);
			result.add(destination);
			i1 = i2;
			
		}
		
		if(result.size() > 1) return interpolateList(result, t);
		else return result;
		
	}

	private void interpolate(float t , Vector2f p1 , Vector2f p2 , Vector2f p2MultiplicationIntermediate , Vector2f destination) {
		
		p1.mul(1 - t , destination);
		p2.mul(t , p2MultiplicationIntermediate);
		destination.add(p2MultiplicationIntermediate);
		
	}

	private float stepHeuristic(List<Vector2f> points) {

		OptionalInt greatestX = points.stream().mapToInt(vector -> (int)vector.x()).max();
		OptionalInt leastX = points.stream().mapToInt(vector -> (int)vector.x()).min();

		OptionalInt greatestY = points.stream().mapToInt(vector -> (int)vector.y()).max();
		OptionalInt leastY = points.stream().mapToInt(vector -> (int)vector.y()).min();
		
		int differenceX = greatestX.getAsInt() - leastX.getAsInt();
		int differenceY = greatestY.getAsInt() - leastY.getAsInt();
		
		int greater = Math.max(differenceX, differenceY);		
		
		return 1f / (float)(greater);
		
	}

	private void checkVector(Artboard artboard , Vector2f point) {

		int width = artboard.width();
		int height = artboard.height();
		if(point.x < 0) point.x = 0;
		else if (point.x >= width) point.x = width - 1;
		if(point.y < 0) point.y = 0;
		else if (point.y >= height) point.y = height - 1;
		
	}

	@Override protected void mod(Artboard target , int x , int y) {

		//try to find (x , y) in the list of mods
		ListIterator<LineMod> mods = lineMods.listIterator(lineMods.size());
		
		while(mods.hasPrevious()) { 
			
			LineMod prev = mods.previous();
			if(prev.textureX() == x && prev.textureY() == y) return;
			
		}
		
		super.mod(target, x, y);
				
	}
	
	@RenderThreadOnly @Override public void translate(Artboard artboard , int x, int y) {
		
		for(Vector2f point : controlPoints) {
			
			point.x = correctifyControlPointX(artboard , x + (int)point.x);
			point.y = correctifyControlPointY(artboard , y + (int)point.y);
			
		}
		
		super.translate(artboard, x, y);
				
	}

	private int correctifyControlPointX(Artboard source , int x) {
		
		if(x < 0) x = 0;
		int width = source.width();
		if(x >= width) x = width - 1;
		
		return x;
		
	}
	
	private int correctifyControlPointY(Artboard source , int y) {
		
		if(y < 0) y = 0;
		int height = source.height();
		if(y >= height) y = height - 1;
		
		return y;
		
	}
	 
	@Override public String toString() {
			
		return String.format("Bezier line from (%d , %d) to (%d , %d)", endpoint1X , endpoint1Y , endpoint2X , endpoint2Y);
			
	}

	@SuppressWarnings("unchecked") @Override public BezierLine copy() {

		BezierLine newBezier = new BezierLine();
		newBezier.endpoint1X = endpoint1X;
		newBezier.endpoint1Y = endpoint1Y;
		newBezier.endpoint2X = endpoint2X;
		newBezier.endpoint2Y = endpoint2Y;
		for(Vector2f controlPoint : controlPoints) newBezier.controlPoints.add(new Vector2f(controlPoint));
		newBezier.thickness(thickness);
		newBezier.color = ChannelBuffer.asChannelBuffer((ColorPixel)color.clone());
		
		return newBezier;
			
	}
			
}
