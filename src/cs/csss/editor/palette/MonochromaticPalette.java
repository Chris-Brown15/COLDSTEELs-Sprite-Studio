/**
 * 
 */
package cs.csss.editor.palette;

import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;

/**
 * Returns an array of color pixels that describe a palette of monochromatic colors from the given one.
 */
public final class MonochromaticPalette extends ColorPalette {

	protected volatile ChannelBuffer[] palette;
	
	public MonochromaticPalette(int valueScale) {

		super("Monochromatic" , valueScale);

	}

	@Override public synchronized ChannelBuffer[] generate(ColorPixel source, int channels) {

		if(channels < 1 || channels > 4) throw new IllegalArgumentException(channels + " is not a valid number of channels for a pixel.");

		setPaletteForStrictlyDarker(source, channels);
		return palette;
			
//		final byte max = (byte)0xff;
//		
//		int valueScale = palette.length;
//		float halfValueScale = valueScale >> 1;
//		
//		float red = (source.ur() / halfValueScale);
//		float green = (source.ug() / halfValueScale);
//		float blue = (source.ub() / halfValueScale);
//		float alpha = (source.ua() / halfValueScale);
//
//		for(int i = 0 ; i < channels ; i++) if(source.ui(i) == 0xff) { 
//			
//			setPaletteForStrictlyDarker(source, channels);
//			return palette;
//				
//		}
//		
//		int halfLength = palette.length >> 1;
//		palette[halfLength] = ChannelStore.asChannelStore(source);
//		
//		for(int i = 0 ; i < halfLength ; i++) palette[i].set(
//			(byte)(i * red) , 
//			channels >= 2 ? (byte)(i * green) : max , 
//			channels >= 3 ? (byte)(i * blue) : max , 
//			channels >= 4 ? (byte)(i * alpha) : max
//				
//		);
//		
//		byte r = source.r();
//		byte g = source.g();
//		byte b = source.b();
//		byte a = source.a();
//		
//		int j = 1;
//		for(int i = halfLength + 1 ; i < palette.length -1 ; i++ , j++) palette[i].set(
//			(byte) (r + (j * red)) ,
//			channels >= 2 ? (byte)(g + (j * green)) : max , 
//			channels >= 3 ? (byte)(b + (j * blue)) : max , 
//			channels >= 4 ? (byte)(a + (j * alpha)) : max
//		);
//		
//		byte finalRed = 0;
//		byte finalGreen = 0;
//		byte finalBlue = 0;
//		byte finalAlpha = 0;
//		
//		if(r != 0) { 
//			
//			finalRed = (byte) (r + (j * red));
//			if(finalRed == 0) finalRed = -1;
//			
//		}
//		
//		if(g != 0) { 
//			
//			finalGreen = (byte) (g + (j * green));
//			if(finalGreen == 0) finalGreen = -1;
//			
//		}
//		
//		if(b != 0) { 
//			
//			finalBlue = (byte) (b + (j * blue));
//			if(finalBlue == 0) finalBlue = -1;
//			
//		}
//		
//		if(a != 0) { 
//			
//			finalAlpha = (byte) (a + (j * alpha));
//			if(finalAlpha == 0) finalAlpha = -1;
//			
//		}
//
//		palette[palette.length - 1].set(
//			finalRed , 
//			channels >= 2 ? finalGreen : max , 
//			channels >= 3 ? finalBlue : max , 
//			channels == 4 ? finalAlpha : max
//		);
//		
//		return palette;
		
	}

	@Override public void setValueScale(int valueScale) {
		
		super.defaultSetValueScale(valueScale);
	 	ChannelBuffer[] newPalette = new ChannelBuffer[valueScale];
	 	
	 	synchronized(this) {
	 	
	 		this.palette = resizePalette(palette, newPalette, () -> new ChannelBuffer());
	 		
	 	}
	
	}
	
	@Override public  ChannelBuffer[] get() {
		
		return palette;
		
	}
	
	private void setPaletteForStrictlyDarker(ColorPixel source , int channels) {

		final byte max = (byte)0xff;
		
		int valueScale = palette.length;
				
		float red = (source.ur() / valueScale);
		float green = (source.ug() / valueScale);
		float blue = (source.ub() / valueScale);
		float alpha = (source.ua() / valueScale);

		palette[palette.length - 1] = ChannelBuffer.asChannelBuffer(source);
		
		for(int i = 0 ; i < palette.length - 1 ; i++) palette[i].set(
			(byte)(i * red) , 
			channels >= 2 ? (byte)(i * green) : max , 
			channels >= 3 ? (byte)(i * blue) : max , 
			channels >= 4 ? (byte)(i * alpha) : max
		);
		
	}
	
}