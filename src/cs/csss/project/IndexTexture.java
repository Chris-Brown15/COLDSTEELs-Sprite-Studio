package cs.csss.project;

import static cs.core.graphics.StandardRendererConstants.MAG_FILTER_NEAREST;
import static cs.core.graphics.StandardRendererConstants.MIN_FILTER_NEAREST;
import static cs.core.graphics.StandardRendererConstants.S_WRAP_MIRRORED;
import static cs.core.graphics.StandardRendererConstants.T_WRAP_MIRRORED;

import static cs.core.utils.CSUtils.require;
import static cs.core.utils.CSUtils.specify;

import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30C.glTexSubImage2D;
import static org.lwjgl.opengl.GL30C.glGetTexImage;
import static org.lwjgl.opengl.GL45C.glGetTextureSubImage;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

import cs.core.graphics.CSTexture;
import cs.core.graphics.ThreadedRenderer;
import cs.core.utils.files.CSGraphic;
import cs.csss.misc.utils.FlexableGraphic;

/**
 * Extender of {@linkplain cs.core.graphics.CSTexture CSTexture} used to allow for modification of texel data. This texture object is used
 * only by the image texture of the artboard. The image texture's pixels are two-channel, one byte per channel pixels whose values are not
 * color values, but are instead lookup indices into a color palette.
 * 
 * @author Chris Brown
 *
 */
public class IndexTexture extends CSTexture {

	public static final int 
		channelsPerPixel = 2 ,
		bytesPerChannel = 1 ,
		glDataFormat = GL_RG ,
		glChannelType = GL_UNSIGNED_BYTE ,
		pixelSizeBytes = 2;
	
	public static final int 
		/**
		 * min and mag filter MUST be NEAREST!
		 * <a href="https://stackoverflow.com/questions/73436422/how-to-palettetize-a-2d-texture-in-modern-opengl"></a>
		 * Stack Overflow answer helped solve this 
		 */
		textureOptions = MIN_FILTER_NEAREST|MAG_FILTER_NEAREST|S_WRAP_MIRRORED|T_WRAP_MIRRORED ,
		zOffset = 0 , //always 0 for 2D texture as parameter into glGetTextureSubImage
		depth = 1; //always 1 for 2D texture as parameter to glGetTextureSubImage
		
	public static volatile int
		backgroundWidth = 8 ,
		backgroundHeight = 8;

	public static int channels() {
		
		return channelsPerPixel;
		
	}
	
	IndexPixel 
		darkerTransparentBackground = new IndexPixel(0 , 0) ,
		lighterTransparentBackground = new IndexPixel(1 , 0);
	
	int 
		width ,
		height;
		
	/**
	 * Tracks some additional variables but does not modify the behavior of {@linkplain IndexTexture#initialize(CSGraphic, int) initialize}.
	 */
	void initialize(int width , int height) {

		this.initialize(width, height , new short[] {0 , 0});
		
	}
	
	void initialize(int width , int height , short[] defaultValueForPixels) {

		require(defaultValueForPixels.length == 2);
		
		FlexableGraphic graphic = new FlexableGraphic(
			width , 
			height , 
			bytesPerChannel , 
			channelsPerPixel , 
			defaultValueForPixels[0] , 
			defaultValueForPixels[1]
		);
		
		super.initialize(graphic, textureOptions);
		
		graphic.shutDown();
		
		this.width = graphic.width();
		this.height = graphic.height();
		
		setCheckerBackground();
		
	}
	
	/**
	 * Overwrites texels of this texture.
	 * 
	 * @param xIndex — bottom left pixel x coordinate of the region to modify
	 * @param yIndex — bottom left pixel y coordinate of the region to modify
	 * @param widthPixels — width in pixels of the region to modify, extends out from {@code xIndex}
	 * @param heightPixels — height in pixels of the region to modify, extends out from {@code yIndex}
	 * @param imageData — off-heap allocated pixel data, i.e., channel values used to overwrite this texture's texel data;
	 * 					  the layout of this buffer should be such that the first values modify the pixel at {@code (xIndex , yIndex)} and
	 * 					  the last values modify the pixel at {@code (xIndex + width - 1 , yIndex + height - 1)}
	 */
	void put(int xIndex , int yIndex , int widthPixels , int heightPixels , ByteBuffer imageData) {
		
		activate();
		glTexSubImage2D(GL_TEXTURE_2D , 0 , xIndex , yIndex , widthPixels , heightPixels , glDataFormat , glChannelType , imageData);
		deactivate();
		
	}
	
	/**
	 * {@code Number} version of {@linkplain IndexTexture#put(int, int, int, int, ByteBuffer) putColor} in which the values of the
	 * given array are considered channel values and buffered into offheap memory and passed to the graphics card.
	 * 
	 * @param xIndex — x index of the bottom left corner of the region to recolor
	 * @param yIndex — y index of the bottom left corner of the region to recolor
	 * @param widthPixels — width of the region to recolor; extends from the {@code xIndex}
	 * @param heightPixels — height of the region to recolor; extends from the {@code yIndex}
	 * @param imageData — a pixel pointing to a position in the palette whose color will be rendered 
	 */
	void put(int xIndex , int yIndex , int widthPixels , int heightPixels , IndexPixel imageData) {
		
		if(xIndex < 0) xIndex = 0;
		if(yIndex < 0) yIndex = 0;
		
		//I HAVE NO IDEA WHY THESE NEED TO BE BUMPED, THEY JUST DO
		int pixels = (widthPixels + 1) * (heightPixels + 1);
		ByteBuffer imageDataAsPtr = memAlloc(pixels * pixelSizeBytes);
		for(int i = 0 ; i < imageDataAsPtr.limit() ; i += 2) imageData.buffer(imageDataAsPtr);			
		imageDataAsPtr.rewind();

		put(xIndex, yIndex, widthPixels, heightPixels, imageDataAsPtr);
		
		memFree(imageDataAsPtr);
		
	}
	
	void setCheckerBackground() {
		
		/* Initializes the gray boxes that give the background transparency effect */

		boolean 
			//tracks whether to use the darker color (located at location 1) or the less dark color (located at location 2)
			lower = true ,
			previousLower = true
		;
			
		for(int row = 0 ; row < height ; row += backgroundHeight) {
		
			for(int col = 0 ; col < width ; col += backgroundWidth) {
				
				int 
					regionWidth = backgroundWidth ,
					regionHeight = backgroundHeight
				;
					
				IndexPixel pixel = lower ? darkerTransparentBackground : lighterTransparentBackground;
				
				if(row + regionHeight > height) regionHeight = height - row;
				if(col + regionWidth > width) regionWidth = width - col;
				put(col , row , regionWidth , regionHeight , pixel);
				
				lower = !lower;
				
			}
			
			//sets lower and previousLower to the opposite of previous lower 
			lower = (previousLower = !previousLower);
									
		}
		
	}
	
	ByteBuffer allTexelData() {
		
		ByteBuffer texels = memAlloc(width * height * pixelSizeBytes);
		activate();
		glGetTexImage(GL_TEXTURE_2D , 0 , glDataFormat , glChannelType , texels);
		deactivate();
		
		return texels;
		
	}
	
	/**
	 * Gets a {@code ByteBuffer} containing texels of this image. This method performs some reformatting of the buffer recieved from the 
	 * GPU, but the caller will recieve a buffer containing the expected contents.
	 * 
	 * @param width — width of the region in pixels
	 * @param height — height of the region in pixels
	 * @param xIndex — x coordinate of the bottom left pixel to begin at
	 * @param yIndex — y coordinate of the bottom left pixel to begin at
	 * @return Buffer containing bytes of pixels at the region specified.
	 */
	ByteBuffer texelBufferWithReformat(int width , int height , int xIndex , int yIndex) {
		
		specify(width > 0 && width <= this.width , width + " is not a valid width.");
		specify(height > 0 && height <= this.height, height + " is not a valid height.");
		specify(xIndex >= 0 && xIndex < this.width , xIndex + " is not a valid x value.");
		specify(yIndex >= 0 && yIndex < this.height , yIndex + " is not a valid y value.");
		specify(xIndex + width <= this.width , (xIndex + width) + " is out of bounds x wise.");
		specify(yIndex + height <= this.height , (yIndex + height) + " is out of bounds y wise.");
		
		ByteBuffer texels;

		int numberPixels = width * height;

		/*
		 * glGetTextureSubImage seems to require the buffer passed to it to be 4 byte aligned or invalid opperation is raised which is why
		 * 2 is added to pixelSizeBytes.
		 * 
		 * glGetTextureSubImage also seems to add a byte of padding between the rows of the image when a data format other than GL_RGBA is
		 * used. This is why we allocate a second texel buffer and copy, skipping ahead one pixel's worth of bytes when reaching the end
		 * of a row.
		 * 
		 */		
		texels = memAlloc(numberPixels * (pixelSizeBytes + 2));
		
		//activates the texture we are sampling from (this class instance)
		activate();		
		//retreives a region of this texture into texels
		glGetTextureSubImage(textureID , 0 , xIndex , yIndex , zOffset , width , height , depth , glDataFormat , glChannelType , texels);
		//checks for GL errors
		ThreadedRenderer.checkErrors();
		//deactivates this texture
		deactivate();
		
		ByteBuffer fixedTexels = memAlloc(numberPixels * pixelSizeBytes);
		
		for(int b = 1 ; b <= numberPixels ; b++) {
			
			fixedTexels.put(texels.get());
			fixedTexels.put(texels.get());
			
			//glGetTextureSubImage seems to add a padding pixel to rows, which this removes from the copy
			if(b % width == 0) {
				
				texels.get();
				texels.get();
				
			}
			
		}
		
		fixedTexels.rewind();
		
		memFree(texels);

		return fixedTexels;

	}
	
	/**
	 * Gets a {@code ByteBuffer} containing texels of this image. This method does not reformat the buffer the GPU returns. This method is
	 * not suitable for most purposes.
	 * 
	 * @param width — width of the region in pixels
	 * @param height — height of the region in pixels
	 * @param xOffset — x coordinate of the bottom left pixel to begin at
	 * @param yOffset — y coordinate of the bottom left pixel to begin at
	 * @return Buffer containing bytes of pixels at the region specified.
	 */
	ByteBuffer texelBuffer(int width , int height , int xOffset , int yOffset) {

		specify(width > 0 && width <= this.width , width + " is not a valid width.");
		specify(height > 0 && height <= this.height, height + " is not a valid height.");
		specify(xOffset >= 0 && xOffset < this.width , xOffset + " is not a valid x value.");
		specify(yOffset >= 0 && yOffset < this.height , yOffset + " is not a valid y value.");
		specify(xOffset + width <= this.width , (xOffset + width) + " is out of bounds x wise.");
		specify(yOffset + height <= this.height , (yOffset + height) + " is out of bounds y wise.");
		
		ByteBuffer texels = memAlloc(width * height * pixelSizeBytes);
		
		activate();
		glGetTextureSubImage(textureID , 0 , xOffset , yOffset , zOffset , width , height , depth , glDataFormat , glChannelType , texels);
		boolean errored = ThreadedRenderer.checkErrors();
		
		if(errored) { 
			
			System.err.printf("Values: [%d , %d , %d , %d]\n" , xOffset , yOffset , width , height);
			
			memFree(texels);
			texels = null;
			
		}
		
		deactivate();
		
		return texels;		
		
	}
	
	void freeTexelBuffer(ByteBuffer texelBuffer) {
		
		memFree(texelBuffer);
		
	}
	
	IndexPixel getPixel(ByteBuffer texelBuffer) {
		
		IndexPixel retrieval = new IndexPixel(texelBuffer);
		return retrieval;
		
	}

	IndexPixel getPixelByIndex(ByteBuffer texelBuffer , int xIndex , int yIndex) {
		
		require(texelBuffer.limit() == width * height * pixelSizeBytes);		
		ByteBuffer texelSlice = texelBuffer.slice((yIndex * width) * pixelSizeBytes + (xIndex * pixelSizeBytes) , pixelSizeBytes);		
		IndexPixel pixelValue = getPixel(texelSlice);		
		return pixelValue;
		
	}
	
	IndexPixel getPixelByIndex(int xIndex , int yIndex) {
		
		ByteBuffer texelBuffer = texelBufferWithReformat(1 , 1 , xIndex , yIndex);
		IndexPixel pixelValue = getPixel(texelBuffer);		
		freeTexelBuffer(texelBuffer);

		return pixelValue;
		
	}
	
	/**
	 * Expands this texture by doubling its bytes per channel, allowing for a greater number of indices to be pointed to.
	 */
//	void resize() {
//		
//		ByteBuffer current = allTexelData();
//		
//		ByteBuffer newTexelBuffer = memAlloc(current.limit() * 2);
//		while(newTexelBuffer.hasRemaining()) newTexelBuffer.put((byte)0).put(current.get());		
//		newTexelBuffer.flip();
//		
//		bytesPerChannel = 2;
//		glChannelType = GL_UNSIGNED_SHORT;
//		pixelSizeBytes = channelsPerPixel * bytesPerChannel;
//		
//		activate();
//		glTexImage2D(GL_TEXTURE_2D , 0 , glDataFormat , width , height , 0 , glDataFormat , glChannelType , newTexelBuffer);
//		deactivate();
//		
//		freeTexelBuffer(current);
//		freeTexelBuffer(newTexelBuffer);
//		
//	}


}