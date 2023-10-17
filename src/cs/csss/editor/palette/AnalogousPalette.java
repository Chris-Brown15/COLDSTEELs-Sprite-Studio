/**
 * 
 */
package cs.csss.editor.palette;

import java.util.Arrays;

import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;

/**
 * Color palette generating analogous colors from a given source color.
 */
public class AnalogousPalette extends ColorPalette {

	private ChannelBuffer[] palette;
	
	/**
	 * Creates an analogous color palette.
	 * 
	 * @param initialValueScale — initial value scale for this palette
	 */
	public AnalogousPalette(int initialValueScale) {
		
		super("Analogous" , initialValueScale);
		
	}

	@Override public void setValueScale(int valueScale) {

		super.defaultSetValueScale(valueScale);
	 	palette = ColorPalette.resizePalette(palette, new ChannelBuffer[valueScale], () -> new ChannelBuffer());

	}

	@Override public ColorPixel[] generate(ColorPixel source, int channels) {

		if(channels < 1 || channels > 4) throw new IllegalArgumentException(channels + " is not a valid number of channels for a pixel.");

		byte max = (byte)0xff;
		
		//Half the analogues are created by finding the greatest color channel and incrementally increasing the next greatest channel. 
		//The second half are created by incrementally decreasing the highest channel.
		
		short ur = source.ur() , ug = source.ug() , ub = source.ub();
		short[] sorted = {ur , ug , ub} ; Arrays.sort(sorted);
		
		boolean redGreatest = ur == sorted[2];
		boolean greenGreatest = ug == sorted[2];
		boolean blueGreatest = ub == sorted[2];
		boolean redSecondGreatest = ur == sorted[1];
		boolean greenSecondGreatest = ug == sorted[1];
		boolean blueSecondGreatest = ub == sorted[1];
		
		int valueScale = palette.length;
		int difference = (sorted[2] - sorted[1]) / valueScale;
		
		for(int i = 0 ; i < palette.length ; i++) { 

			byte red = (byte) (redGreatest ? ur : redSecondGreatest ? ur + (i * difference) : ur);
			byte green = channels >= 2 ? (byte) (greenGreatest ? ug : greenSecondGreatest ? ug + (i * difference) : ug) : max;
			byte blue = channels >= 3 ? (byte) (blueGreatest ? ub : blueSecondGreatest ? ub + (i * difference) : ub) : max;
			
			palette[i].set(red, green, blue, source.a());
			
		}
		
		return palette;
		
	}
	
	@Override public ColorPixel[] get() {
		
		return palette;
		
	}

}
