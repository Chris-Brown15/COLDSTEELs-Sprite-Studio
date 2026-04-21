package cs.csss.project.io;

import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_image_free;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import sc.core.binary.SCGraphic;

/**
 * Class containing image data of a JPEG file.
 */
public class JPG extends SCGraphic {
	
	public final ByteBuffer imageData;
	public final int width;
	public final int height;
	public final int bitsPerPixel;
	private volatile boolean isShutDown = false;
	
	/**
	 * Loads the file at the given path as a jpeg file.
	 * 
	 * @param absFilePath absolute file path to load
	 * @param channels desired channels per pixel
	 */
	public JPG(final String absFilePath , int channels) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {

			IntBuffer x = stack.mallocInt(1);
			IntBuffer y = stack.mallocInt(1);
			IntBuffer bytesPerPixel = stack.mallocInt(1);
			
			ByteBuffer imageData = stbi_load(absFilePath , x , y , bytesPerPixel , channels);
			if(imageData == null) throw new IllegalStateException();
			
			this.width = x.get();
			this.height = y.get();
			this.imageData = imageData;
			this.bitsPerPixel = bytesPerPixel.get() * 8;
			
		}
		
	}
	
	/**
	 * Loads the file at the given path as a jpeg file, reading the number of channels from the file.
	 * 
	 * @param absFilePath absolute file path to load
	 */
	public JPG(String absFilePath) {
		
		this(absFilePath , 0);
		
	}
	
	@Override public void shutDown() {
		
 		if(!isShutDown) stbi_image_free(imageData);
 		isShutDown = true;
	
	}

	@Override public boolean isFreed() {

		return isShutDown;
		
	}

	@Override public int width() {

		return width;
		
	}

	@Override public int height() {

		return height;
		
	}
	
	@Override public int channels() {

		return 3;
		
	}

	@Override public ByteBuffer data() {

		return imageData;
		
	}

	@Override public int bytesPerPixel() {

		return bitsPerPixel >> 3;

	}

	@Override public int bytesPerChannel() {

		return 1;
		
	}
	
}
