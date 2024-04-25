/**
 * 
 */
package cs.csss.editor.line;

import java.util.Objects;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.project.Artboard;

/**
 * Class for linear lines.
 */
public class LinearLine extends Line {
	
	/**
	 * Creates a new linear line whose endpoints are not set.
	 */
	public LinearLine() {}
	
	/**
	 * Creates a new linear line whose initial endpoints are given by the the given parameters.
	 * 
	 * @param source artboard this line belongs to
	 * @param endpoint1X endpoint 1 x coordinate for this line
	 * @param endpoint1Y endpoint 1 y coordinate for this line
	 * @param endpoint2X endpoint 2 x coordinate for this line
	 * @param endpoint2Y endpoint 2 y coordinate for this line
	 */
	public LinearLine(Artboard source , int endpoint1X , int endpoint1Y , int endpoint2X , int endpoint2Y) {
		
		Objects.requireNonNull(source);
		
		setEndpoint1(source, endpoint1X, endpoint1Y);
		setEndpoint2(source, endpoint2X, endpoint2Y);
		
	}
	
	/**
	 * Returns the slope of this line. Slopes is calculated as rise over run. Both endpoints must be defined prior to this call. 
	 * 
	 * @return Slope of this line. 
	 */
	public float slopeY() {
		
		checkEndpoint1();
		checkEndpoint2();
		
		return (float)(endpoint2Y - endpoint1Y) / (float)(endpoint2X - endpoint1X);
		
	}

	/**
	 * Returns the slope of this line. Slope is calculated as run over rise. Both endpoints must be defined prior to this call.
	 * 
	 * @return Slope of this line. 
	 */
	public float slopeX() {
		
		checkEndpoint1();
		checkEndpoint2();
		
		return (float)(endpoint2X - endpoint1X) / (float)(endpoint2Y - endpoint1Y);
		
	}

	@RenderThreadOnly @Override public void reset(Artboard target) {

		checkEndpoint1();
		checkEndpoint2();
		
		assert endpoint1X >= 0;
		assert endpoint1Y >= 0;
		assert endpoint2X >= 0;
		assert endpoint2Y >= 0;		
		
		int currentGreaterX = Math.max(endpoint1X, endpoint2X);
		int currentLesserX = Math.min(endpoint2X, endpoint1X);

		int currentGreaterY = Math.max(endpoint1Y, endpoint2Y);
		int currentLesserY = Math.min(endpoint1Y, endpoint2Y);
		
		int currentWidth = currentGreaterX - currentLesserX + 1;
		int currentHeight = currentGreaterY - currentLesserY + 1;
		
		putModsInArtboard(target);
		
		//special case
		if(endpoint1X == endpoint2X && endpoint1Y == endpoint2Y) {
			
			mod(target , endpoint1X , endpoint1Y);			
			return;
			
		}
		
		if(currentWidth > currentHeight) {

			float slope = slopeY();
			int x = currentLesserX;
			
			while(x <= currentGreaterX) {
				
				int y = Math.round(endpoint1Y + (x - endpoint1X) * slope);		
				mod(target , x , y);
				x += 1;
				
			}	
		
		} else {
			
			float slope = slopeX();
			int y = currentLesserY;
			
			while(y <= currentGreaterY) {
				
				int x = Math.round(endpoint1X + (y - endpoint1Y) * slope);		
				mod(target , x , y);
				y += 1;
				
			}
		
		}
			
		lineMods.forEach(mod -> target.putColorInImage(mod.textureX(), mod.textureY(), mod.width(), mod.height(), color));
				
	}
	 
	@Override public String toString() {
		
		return String.format("Linear line from (%d , %d) to (%d , %d)", endpoint1X , endpoint1Y , endpoint2X , endpoint2Y);
		
	}

	@SuppressWarnings("unchecked") @Override public LinearLine copy() {

		LinearLine newLinear = new LinearLine();
		newLinear.endpoint1X = endpoint1X;
		newLinear.endpoint1Y = endpoint1Y;
		newLinear.endpoint2X = endpoint2X;
		newLinear.endpoint2Y = endpoint2Y;
		newLinear.thickness(thickness);
		newLinear.color = ChannelBuffer.asChannelBuffer((ColorPixel)color.clone());
		
		return newLinear;
		
	}
	
}