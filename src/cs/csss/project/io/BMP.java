/**
 * 
 */
package cs.csss.project.io;

import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static cs.core.utils.CSUtils.specify;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.system.MemoryStack;

import cs.core.utils.files.CSGraphic;

/**
 * Class containing image data from a bitmap file.
 */
public class BMP implements CSGraphic {

	static {
		
		stbi_set_flip_vertically_on_load(true);
		
	}
	
	private ByteBuffer image;
	public final int
		width ,
		height;
	
	public final byte bytesPerPixel;
	
	/**
	 * Loads a bitmap file from disk.
	 * 
	 * @param filepath — absolute filepath to bitmap file
	 * @param channels — the desired number of channels for pixels of the image
	 */
	public BMP(String filepath , int channels) {
		
		specify(Files.exists(Paths.get(filepath)) , filepath + " is not a valid file path.");
		specify(filepath.endsWith(".bmp") , filepath + " is not a bitmap file.");
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			IntBuffer x = stack.callocInt(1);
			IntBuffer y = stack.callocInt(1);
			IntBuffer bytes = stack.callocInt(1);
			image = stbi_load(filepath , x , y , bytes , channels);
			
			specify(image , "failed to load image data from bitmap file.");
						
			this.width = x.get();
			this.height = y.get();
			this.bytesPerPixel = (byte) bytes.get();
			
		}
		
	}
	
	/**
	 * Loads a bitmap image from disk with number of channels determined by the file.
	 * 
	 * @param filepath — absolute filepath to a bitmap file
	 */
	public BMP(String filepath) {
		
		this(filepath , 0);
		
	}
	
	@Override public void shutDown() {

		if(!isFreed()) {
			
			stbi_image_free(image);
			image = null;
			
		}

	}

	@Override public boolean isFreed() {

		return image == null;
		
	}

	@Override public int width() {

		return width;
		
	}

	@Override public int height() {

		return height;
		
	}

	@Override public int bitsPerPixel() {

		return bytesPerPixel << 3;
		
	}

	@Override public int bitsPerChannel() {

		return 8;
		
	}

	@Override public int channels() {

		return bytesPerPixel;
		
	}

	@Override public ByteBuffer imageData() {

		return image;
		
	}

}
