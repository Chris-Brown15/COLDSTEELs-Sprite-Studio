package cs.csss.misc.utils;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static cs.core.utils.CSUtils.specify;

import java.nio.ByteBuffer;

import cs.core.utils.ShutDown;
import cs.core.utils.files.CSGraphic;
import cs.csss.engine.ColorPixel;

public class FlexableGraphic implements CSGraphic , ShutDown {

	private boolean isFreed = false;
	
	public final int 
		width ,
		height ,
		bytesPerChannel ,
		channelsPerPixel
	;
	
	private final ByteBuffer imageData;
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param bytesPerChannel
	 * @param channelsPerPixel
	 * @param defaultChannelValues
	 */
	public FlexableGraphic(
		int width , 
		int height , 
		int bytesPerChannel , 
		int channelsPerPixel ,
		Number...defaultChannelValues 
	) {

		specify(
			bytesPerChannel == 1 || 
			bytesPerChannel == 2 || 
			bytesPerChannel == 4 , 
			bytesPerChannel + " is not a supported channel size."
		);
		
		specify(channelsPerPixel >= 1 || channelsPerPixel <= 4 , channelsPerPixel + " is not a supported channel count");
		
		specify(
			defaultChannelValues.length == 1 || defaultChannelValues.length == channelsPerPixel , 
			defaultChannelValues.length + " is not a valid length for a set of default channel values. Must be 1 or channelsPerPixel." 
		);

		this.width = width;
		this.height = height;
		this.bytesPerChannel = bytesPerChannel;
		this.channelsPerPixel = channelsPerPixel;
		
		imageData = memAlloc(width * height * (channelsPerPixel * bytesPerChannel));
		
		initializeImagePointer(defaultChannelValues);
		
	}
	
	public FlexableGraphic(int width , int height , int channelsPerPixel , ColorPixel values) {
		
		if(width <= 0) throw new IllegalArgumentException("Width is not positive: " + width);
		if(height <= 0) throw new IllegalArgumentException("Height is not positive: " + height);		
		if(channelsPerPixel <= 0) throw new IllegalArgumentException("Channels per pixel is not positive.");
		
		this.width = width;
		this.height = height;
		this.bytesPerChannel = 1;
		this.channelsPerPixel = channelsPerPixel;

		imageData = memAlloc(width * height * (channelsPerPixel * bytesPerChannel));
		Number[] defaultValues = new Number[channelsPerPixel];
		for(int i = 0 ; i < channelsPerPixel ; i++) defaultValues[i] = values.i(i);
		initializeImagePointer(defaultValues);
		
	}

	private void initializeImagePointer(Number[] defaultPixelValues) {

		if(defaultPixelValues.length == 1) while(imageData.hasRemaining()) switch(bytesPerChannel) {
		
			case 1 -> imageData.put(defaultPixelValues[0].byteValue());
			case 2 -> imageData.putShort(defaultPixelValues[0].shortValue());
			case 4 -> imageData.putInt(defaultPixelValues[0].intValue());
			
		} else {
			
			int i = 0;
			while(imageData.hasRemaining()) {
					
				switch(bytesPerChannel) {
						
					case 1 -> imageData.put(defaultPixelValues[i].byteValue());
					case 2 -> imageData.putShort(defaultPixelValues[i].shortValue());
					case 4 -> imageData.putInt(defaultPixelValues[i].intValue());
						
				}
						
				i++ ; if(i == channelsPerPixel) i = 0;
				
			}
			
		}
		
		imageData.rewind();
		
	}
	
	@Override public void shutDown() {

		if(isFreed) return;
		memFree(imageData);
		isFreed = true;
		
	}

	@Override public boolean isFreed() {

		return isFreed;
		
	}

	@Override public int width() {

		return width;
		
	}

	@Override public int height() {

		return height;
		
	}

	@Override public int bitsPerPixel() {

		return (bytesPerChannel * channelsPerPixel) << 3;
		
	}

	@Override public ByteBuffer imageData() {

		return imageData;
		
	}

	@Override public int bitsPerChannel() {

		return bytesPerChannel << 3;
		
	}

	@Override public int channels() {

		return channelsPerPixel;
		
	}

}
