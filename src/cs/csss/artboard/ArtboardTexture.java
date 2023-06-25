package cs.csss.artboard;

import static cs.core.graphics.StandardRendererConstants.MAG_FILTER_LINEAR;
import static cs.core.graphics.StandardRendererConstants.MIN_FILTER_LINEAR;
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
import cs.csss.core.FlexableGraphic;

/**
 * Extender of {@linkplain cs.core.graphics.CSTexture CSTexture} used to allow for modification of texel data. This texture object is used
 * only by the image texture of the artboard. The image texture's pixels are two-channel, one byte per channel pixels whose values are not
 * color values, but are instead lookup indices into a color palette.
 * 
 * @author Chris Brown
 *
 */
public class ArtboardTexture extends CSTexture {

	public static final int 
		channelsPerPixel = 2 ,
		bytesPerChannel = 1 ,
		glDataFormat = GL_RG ,
		glChannelType = GL_UNSIGNED_BYTE ,
		pixelSizeBytes = 2		
	;
	
	protected static final int 
		textureOptions = MIN_FILTER_LINEAR|MAG_FILTER_LINEAR|S_WRAP_MIRRORED|T_WRAP_MIRRORED ,
		zOffset = 0 , //always 0 for 2D texture as parameter into glGetTextureSubImage
		depth = 1 //always 1 for 2D texture as parameter to glGetTextureSubImage
	;
	
	public static volatile int
		backgroundCheckerWidth = 8 ,
		backgroundCheckerHeight = 8
	;

	IndexPixel 
		darkerTransparentBackground = new IndexPixel(0 , 0) ,
		lighterTransparentBackground = new IndexPixel(1 , 0)
	;
	
	int 
		width ,
		height
	;
		
	/**
	 * Tracks some additional variables but does not modify the behavior of {@linkplain CSTexture#initialize(CSGraphic, int) initialize}.
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
	 * {@code Number} version of {@linkplain ArtboardTexture#put(int, int, int, int, ByteBuffer) putColor} in which the values of the
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
			
		for(int row = 0 ; row < height ; row += backgroundCheckerHeight) {
		
			for(int col = 0 ; col < width ; col += backgroundCheckerWidth) {
				
				int 
					regionWidth = backgroundCheckerWidth ,
					regionHeight = backgroundCheckerHeight
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
	
	void freeTexelData(ByteBuffer texelBuffer) {
		
		memFree(texelBuffer);
		
	}
	
	/**
	 * Gets a {@code ByteBuffer} containing texels of this image.
	 * 
	 * @param width — width of the region in pixels
	 * @param height — height of the region in pixels
	 * @param xOffset — x coordinate of the bottom left pixel to begin at
	 * @param yOffset — y coordinate of the bottom left pixel to begin at
	 * @return Buffer containing bytes of pixels at the region specified.
	 */
	ByteBuffer texelBuffer(int width , int height , int xOffset , int yOffset) {
		
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
		glGetTextureSubImage(textureID , 0 , xOffset , yOffset , zOffset , width , height , depth , glDataFormat , glChannelType , texels);
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
		
		ByteBuffer texelBuffer = texelBuffer(1 , 1 , xIndex , yIndex);
		IndexPixel pixelValue = getPixel(texelBuffer);		
		freeTexelBuffer(texelBuffer);

		return pixelValue;
		
	}

	/**
	 * This class exists mainly because java does not have unsigned primitives. This means that code that wants a java {@code byte} to be 
	 * unsigned, as it is in the GPU, may have errors which this class tries to fix. As well, there are basically different kinds of pixels
	 * in this application, index pixels and palette pixels. Palette pixels are more complicated because their number of number of channels 
	 * per pixel vary. This class attempts to make working with pixels as painless as possible.
	 * 
	 * @author Chris Brown
	 *
	 */
	public class IndexPixel {
	
		public final short 
			xIndex ,
			yIndex
		;
		
		IndexPixel(short xIndex , short yIndex) {
		
			specify(xIndex >= 0 , xIndex + " is an invalid x index") ; specify(yIndex >= 0 , yIndex + " is an invalid y index");
			
			this.xIndex = xIndex;
			this.yIndex = yIndex;
			
		}	

		IndexPixel(int xIndex , int yIndex) {

			specify(xIndex >= 0 , xIndex + " is an invalid x index") ; specify(yIndex >= 0 , yIndex + " is an invalid y index");
			
			this.xIndex = (short) xIndex;
			this.yIndex = (short) yIndex;
			
		}	

		private IndexPixel(ByteBuffer buffer) {
			
		 	xIndex = buffer.get();
		 	yIndex = buffer.get();
			
		}

		public void buffer(ByteBuffer buffer) {
	
			//2 is used because each pixel of the image is always two bytes.
			require(buffer.remaining() >= 2);
			
			buffer.put((byte)xIndex);
			buffer.put((byte)yIndex);
			
		}
		
		@Override public String toString() {
			
			return "Index Pixel -> (" + xIndex + ", " + yIndex + ")";
			
		}
		
	}

}