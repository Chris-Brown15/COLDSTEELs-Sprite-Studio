package cs.csss.editor;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

import cs.core.utils.files.CSGraphic;

public class EmptyGraphic implements CSGraphic {

	private ByteBuffer pixel = memAlloc(4).putInt(0x000000ff).flip();
	
	public EmptyGraphic() {}

	@Override public void shutDown() {
		
		if(isFreed()) return;
		
		memFree(pixel);
		pixel = null;
		
	}

	@Override public boolean isFreed() {
		
		return pixel == null;
		
	}

	@Override public int width() {

		return 1;
		
	}

	@Override public int height() {
		
		return 1;
		
	}

	@Override public int bitsPerPixel() {

		return 32;
		
	}

	@Override public int bitsPerChannel() {
		
		return 8;
	
	}

	@Override public int channels() {

		return 4;
		
	}

	@Override public ByteBuffer imageData() {

		return pixel;
	
	}

}
