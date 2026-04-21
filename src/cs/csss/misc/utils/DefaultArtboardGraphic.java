package cs.csss.misc.utils;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

import sc.core.SCShutDown;
import sc.core.binary.SCGraphic;

public class DefaultArtboardGraphic extends SCGraphic implements SCShutDown {

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

	@Override public int channels() {

		return channelsPerPixel;
		
	}

	@Override public ByteBuffer data() {

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

	@Override public int bytesPerPixel() {

		return channelsPerPixel * bytesPerChannel;
		
	}

	@Override
	public int bytesPerChannel() {

		return channelsPerPixel;
		
	}

}
