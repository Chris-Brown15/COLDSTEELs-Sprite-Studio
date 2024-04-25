/**
 * 
 */
package cs.csss.editor.line;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.LookupPixel;
import cs.csss.project.Artboard;
import cs.csss.project.Layer;
import cs.csss.project.LayerPixel;
import cs.csss.utils.ByteBufferUtils;
/**
 * Base class for all lines. 
 */
public abstract class Line {
	
	/**
	 * First endpoint of the line in artboard coordinates.
	 */
	protected int endpoint1X = -1 , endpoint1Y = -1;
	
	/**
	 * Second endpoint of the line in artboard coordinates.
	 */
	protected int endpoint2X = -1 , endpoint2Y = -1;
	
	/**
	 * Color the line will be.
	 */
	protected ChannelBuffer color = new ChannelBuffer((byte)0 , (byte)0 , (byte)0 , (byte)0);

	/**
	 * List of mods made by this line.
	 */
	protected List<LineMod> lineMods = new ArrayList<>();
	
	/**
	 * Thickness of this line.
	 */
	protected int thickness = 1;
	
	/**
	 * Destination for index correction operations.
	 */
	protected int[] correctifyIndices = new int[6];
	
	/**
	 * Resets this line. Draws it on the given artboard. 
	 * 
	 * @param target artboard to draw this line into
	 * 
	 * @throws IllegalStateException if one or both of the endpoints of the line have not been set.
	 */
	public abstract void reset(Artboard target);
	
	/**
	 * Sets the active layer of {@code artboard} to {@code targetLayer} prior to performing {@link #reset(Artboard)}, then sets it back afterward. 
	 * 
	 * @param artboard artboard to draw this line into
	 * @param targetLayer the layer to store the modifications made by this line
	 * @throws NullPointerException if either {@code artboard} or {@code targetLayer} is <code>null</code>.
	 */
	public void reset(Artboard artboard , Layer targetLayer) {
		
		Objects.requireNonNull(artboard);
		Objects.requireNonNull(targetLayer);
		
		Layer previousActive = artboard.activeLayer();
		artboard.setActiveLayer(targetLayer);
		reset(artboard);		
		artboard.setActiveLayer(previousActive);
		
	}
	
	/**
	 * Sets the color of this line. {@link #reset(Artboard)} must be called to see the line's color change reflected in the artboard.
	 * 
	 * @param newColor new color for this line
	 * @throws NullPointerException if {@code newColor} is <code>null</code>
	 */
	public void color(ChannelBuffer newColor) {
		
		Objects.requireNonNull(color);
		this.color = newColor;
		
	}
	
	/**
	 * Returns the color of this line. 
	 * 
	 * @return Color of this line.
	 */
	public ChannelBuffer color() {
		
		return color;
		
	}
	
	/**
	 * Sets one of the endpoints of this line. If either of {@code x} or {@code y} is less than zero, it is snapped to zero.
	 * 
	 * @param x x coordinate of the endpoint in artboard coordinates
	 * @param y y coordinate of the endpoint in artboard coordinates
	 */
	public void setEndpoint1(Artboard source , int x , int y) {
		
		if(x == endpoint1X && y == endpoint1Y) return;
		int width = source.width();
		int height = source.height();
		
		//correct parameters
		
		if(x < 0) x = 0;
		else if (x >= width) x = width - 1;
		
		if(y < 0) y = 0;
		else if (y >= height) y = height - 1;
		
		endpoint1X = x;
		endpoint1Y = y;
		
	}

	/**
	 * Sets one of the endpoints of this line. If either of {@code x} or {@code y} is less than zero, it is snapped to zero.
	 * 
	 * @param x x coordinate of the endpoint in artboard coordinates
	 * @param y y coordinate of the endpoint in artboard coordinates
	 */
	public void setEndpoint2(Artboard source , int x , int y) {

		if(x == endpoint2X && y == endpoint2Y) return;
		int width = source.width();
		int height = source.height();
		
		//correct parameters
		
		if(x < 0) x = 0;
		else if (x >= width) x = width - 1;
		
		if(y < 0) y = 0;
		else if (y >= height) y = height - 1;
				
		endpoint2X = x;
		endpoint2Y = y;
		
	}
	
	/**
	 * Returns the x coordinate of endpoint one. 
	 * 
	 * @return X coordinate of endpoint one.
	 */
	public int endpoint1X() {
		
		return endpoint1X;
		
	}

	/**
	 * Returns the y coordinate of endpoint one. 
	 * 
	 * @return Y coordinate of endpoint one.
	 */
	public int endpoint1Y() {
		
		return endpoint1Y;
		
	}

	/**
	 * Returns the x coordinate of endpoint two. 
	 * 
	 * @return X coordinate of endpoint two.
	 */
	public int endpoint2X() {
		
		return endpoint2X;
		
	}

	/**
	 * Returns the y coordinate of endpoint two. 
	 * 
	 * @return Y coordinate of endpoint two.
	 */
	public int endpoint2Y() {
		
		return endpoint2Y;
		
	}

	/**
	 * Throws an {@code IllegalStateException} if the values of endpoint two have not been set.
	 */
	protected void checkEndpoint2() {
		
		if(endpoint2X < 0 || endpoint2Y < 0) throw new IllegalStateException("Endpoint 2 has not been set yet, cannot calculate slope.");
	
	}

	/**
	 * Throws an {@code IllegalStateException} if the values of endpoint one have not been set.
	 */
	protected void checkEndpoint1() {
		
		if(endpoint1X < 0 || endpoint1Y < 0) throw new IllegalStateException("Endpoint 1 has not been set yet, cannot calculate slope.");
	
	}

	/**
	 * Returns the list of {@link LineMod}s this line currently has made to the artboard it was most recently {@link #reset(Artboard)} on.
	 * 
	 * @return This line's list of mods.
	 */
	public List<LineMod> lineMods() {
		
		return lineMods;
		
	}
	
	/**
	 * Creates a new {@link LineMod} and adds it to this line's list of mods.
	 * 
	 * @param target the artboard being queried
	 * @param x x coordinate of the mod
	 * @param y y coordinate of the mod
	 */
	@RenderThreadOnly protected void mod(Artboard target , int x , int y) {
		
		if(thickness != 1) {
			
			x -= (thickness >> 1);
			y -= (thickness >> 1);
			
		}

		ByteBufferUtils.correctifyIndices(target, x, y, thickness, thickness , correctifyIndices);
		int lx = correctifyIndices[0] , by = correctifyIndices[1] , width = correctifyIndices[2] , height = correctifyIndices[3];
		LayerPixel[][] previousRegion = target.getRegionOfLayerPixels(lx , by , width , height);
		
		lineMods.add(new LineMod(lx , by , width , height , previousRegion));
		
	}
	
	/**
	 * Puts all mods stored in this line's list of mods in {@code artboard}, then clears the list of mods.
	 * 
	 * @param artboard artboard to put mods back into
	 * @throws NullPointerException if {@code artboard} is <code>null</code>.
	 */
	public void putModsInArtboard(Artboard artboard) {

		Objects.requireNonNull(artboard);		
		lineMods.forEach(mod -> artboard.replace(mod.textureX, mod.textureY, mod.width, mod.height, mod.previousColor));		
		lineMods.clear();
		
	}
	
	/**
	 * Puts all mods stored in this line's list of mods in {@code artboard}, and specifically in {@code layer}.
	 * 
	 * @param artboard artboard to put mods back into
	 * @param target layer to store the mods in
	 * @throws NullPointerException if either {@code artboard} or {@code layer} is <code>null</code>.
	 */
	public void putModsInArtboard(Artboard artboard , Layer target) {
		
		Objects.requireNonNull(artboard);
		Objects.requireNonNull(target);
		
		Layer previousActive = artboard.activeLayer();
		artboard.setActiveLayer(target);
		
		lineMods.forEach(mod -> artboard.replace(mod.textureX, mod.textureY, mod.width, mod.height, mod.previousColor));
		
		artboard.setActiveLayer(previousActive);
		lineMods.clear();
		
	}
	
	/**
	 * Sets the thickness value for this line. 
	 * 
	 * @param newThickness new thickness value for this line.
	 * @throws IllegalArgumentException if {@code newThickness} is not positive.
	 */
	public void thickness(int newThickness) {
		
		if(newThickness <= 0) throw new IllegalArgumentException(newThickness + " is invalid as a thickness.");
		this.thickness = newThickness;
		
	}
	
	/**
	 * Returns the current thickness value for this line.
	 * 
	 * @return Current thickness value for this line.
	 */
	public int thickness() {
		
		return thickness;
		
	}
	
	/**
	 * Translates the end points of this line by the given values.
	 * 
	 * @param artboard the artboard this line belongs to
	 * @param x x amount in artboard coordinates to translate this line
	 * @param y y amount in artboard coordinates to translate this line
	 */
	@RenderThreadOnly public void translate(Artboard artboard , int x , int y) {
		
		putModsInArtboard(artboard);
		
		setEndpoint1(artboard, endpoint1X + x, endpoint1Y + y);
		setEndpoint2(artboard, endpoint2X + x, endpoint2Y + y);
		
		reset(artboard);
		
	}
	
	/**
	 * Moves this line such that its midpoint is equal to {@code (x , y)}. 
	 * 
	 * @param artboard the artboard this line belongs to
	 * @param x x artboard coordinate to move this line to
	 * @param y y artboard coordinate to move this line to
	 */
	@RenderThreadOnly public void moveTo(Artboard artboard , int x , int y) {
		
		int minX = Math.min(endpoint1X, endpoint2X);
		int maxX = Math.max(endpoint1X, endpoint2X);
		int minY = Math.min(endpoint1Y, endpoint2Y);
		int maxY = Math.max(endpoint1Y, endpoint2Y); 
		 
		int midX = minX + ((maxX - minX) / 2);
		int midY = minY + ((maxY - minY) / 2);
		
		int translationX = x - midX;
		int translationY = y - midY;
		translate(artboard , translationX , translationY);
		
	}

	/**
	 * Creates a deep copy of this line.
	 * 
	 * @param <X> type of extension of {@link Line} being copied and returned
	 * @return Deep copy of this line.
	 */
	public abstract <X extends Line> X copy();
	
	/**
	 * Container for data about mods this line has made to the artboard it belongs to. {@code previousColor} is the previous color found at 
	 * {@code (textureX , textureY)}. {@code color} can be <code>null</code>, in which case no color was present, so a background color will be 
	 * placed.
	 * 
	 */
	public record LineMod(int textureX , int textureY , int width , int height , LookupPixel[][] previousColor) {}
	
}
