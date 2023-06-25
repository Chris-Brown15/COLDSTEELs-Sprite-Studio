package cs.csss.misc.utils;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

import cs.core.utils.ShutDown;
import cs.core.utils.files.CSGraphic;

public class DefaultArtboardGraphic implements ShutDown, CSGraphic {

	public final int
		width ,
		height ,
		channelsPerPixel ,
		bytesPerChannel
	;
	
	private final ByteBuffer imageData;
	
	private boolean freed = false;
	
	public DefaultArtboardGraphic(int width , int height , int channelsPerPixel , int bytesPerChannel) {

		this.width = width;
		this.height = height;
		this.channelsPerPixel = channelsPerPixel;
		this.bytesPerChannel = bytesPerChannel;
		
		imageData = memAlloc(width * height * channelsPerPixel * bytesPerChannel);
		
		
		
	}

	@Override public int width() {

		return width;
		
	}

	@Override public int height() {

		return height;
		
	}

	@Override public int bitsPerPixel() {

		return 8 * channelsPerPixel * bytesPerChannel;
		
	}

	@Override public int bitsPerChannel() {

		return 8 * channelsPerPixel;
		
	}

	@Override public int channels() {

		return channelsPerPixel;
		
	}

	@Override public ByteBuffer imageData() {

		return imageData;
		
	}

	@Override public void shutDown() {

		if(freed) return;
		
		memFree(imageData);
		freed = true;
		
	}

	@Override public boolean isFreed() {

		return freed;
		
	}

}
