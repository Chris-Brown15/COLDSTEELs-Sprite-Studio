/**
 * 
 */
package cs.csss.editor.palette;

import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;

/**
 * Palette generating complimentary colors from a given source.
 */
public final class ComplementaryPalette extends ColorPalette {

	private static final int permutationsPossible = 6;
	
	private volatile ChannelBuffer[] palette;
	
	/**
	 * Creates a new complimentary color palette.
	 * 
	 * @param initialValueScale — inital value scale for this palette.
	 */
	public ComplementaryPalette(int initialValueScale) {

		super("Complementary", initialValueScale);
		
	}

	@Override public void setValueScale(int valueScale) {

		super.defaultSetValueScale(valueScale);
		synchronized(this) {
			
			palette = resizePalette(palette, new ChannelBuffer[valueScale], () -> new ChannelBuffer());
			
		}

	}

	@Override public synchronized ChannelBuffer[] generate(ColorPixel source, int channels) {

		//generate permutations of pixels with channel values swapped.
		if(channels <= 2 || palette.length < permutationsPossible) {
			
			grayscalePalette(source);
			return palette;
			
		}
		
		//swap green and red
		palette[0].set(source.g() , source.r() , source.b() , source.a());
		//swap blue and red
		palette[1].set(source.b() , source.g() , source.r() , source.a());
		//swap alpha and red
		palette[2].set(source.a() , source.g() , source.b() , source.r());
		//swap green and blue
		palette[3].set(source.r() , source.b() , source.g() , source.a());
		//swap green and alpha
		palette[4].set(source.r() , source.a() , source.b() , source.g());
		//swap blue and alpha
		palette[5].set(source.r() , source.g() , source.a() , source.b());
		
		return palette;
		
	}

	@Override public ChannelBuffer[] get() {
		
		return palette;
		
	}
	
	private void grayscalePalette(ColorPixel source) {
		
		for(int i = 0 ; i < palette.length ; i++) if(palette[i] != null) palette[i].set(source.r() , source.g() , source.b() , source.a());
		
	}

}
