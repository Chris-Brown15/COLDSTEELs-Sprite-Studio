package cs.csss.project;

import static cs.core.utils.CSUtils.require;
import static cs.csss.engine.Logging.*;
import static org.lwjgl.opengl.GL11C.GL_RED;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30C.glTexSubImage2D;
import static org.lwjgl.opengl.GL30C.glTexImage2D;

import static org.lwjgl.system.MemoryUtil.memCopy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import cs.core.graphics.CSTexture;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.misc.utils.FlexableGraphic;

/**
 * Implementation of {@code CSTexture} used extensively as a container of color values. The indices of pixels in the artboard are the values 
 * pixels of {@link cs.csss.project.IndexTexture IndexTexture} will contain, which are used in shaders to lookup and display the correct 
 * color. 
 * 
 * @author Chris Brown
 *
 */
@RenderThreadOnly public class ArtboardPalette extends CSTexture {

	/**
	 * OpenGL constant for the channel type for GL functions.
	 */
	public static final int glChannelType = GL_UNSIGNED_BYTE;	
	
	short currentRow = 0 , currentCol = 0;	
	
	final int channelsPerPixel;	
	
	private int	glDataFormat , pixelSizeBytes , paletteWidth = 256 , paletteHeight = 256;
	
	private volatile ByteBuffer paletteMemory;
	
	/**
	 * For these arrays, if there is an alpha channel available, it will contain either -1 or 0 depending upon if the background is 
	 * visible. In the case there is no alpha value, the background will always be visible
	 */
	final byte[] darkerCheckeredBackground , lighterCheckeredBackground;
	
	/**
	 * Creates an artboard palette.
	 * 
	 * @param channelsPerPixel — number of channels per pixel of this palette
	 */
	public ArtboardPalette(int channelsPerPixel) {
		
		this.channelsPerPixel = channelsPerPixel;
		darkerCheckeredBackground = new byte[channelsPerPixel];
		lighterCheckeredBackground = new byte[channelsPerPixel];
		initializeCheckeredBackgroundColors();
		
	}
		
	/**
	 * Initializes this artboard palette.
	 */
	public void initialize() {

		FlexableGraphic graphic = new FlexableGraphic(paletteWidth , paletteHeight , 1 , channelsPerPixel , 0xff);
		
		initialize(graphic , IndexTexture.textureOptions);
		graphic.shutDown();
		
		pixelSizeBytes = channelsPerPixel;
		
		glDataFormat = switch(channelsPerPixel) {
			case 1 -> GL_RED;
			case 2 -> GL_RG;
			case 3 -> GL_RGB;
			case 4 -> GL_RGBA;
			default -> throw new IllegalArgumentException(channelsPerPixel + " is not a valid number of channels.");
		};
				
		paletteMemory = BufferUtils.createByteBuffer(paletteHeight * paletteWidth * channelsPerPixel);
		
		put(new PalettePixel(darkerCheckeredBackground));
		put(new PalettePixel(lighterCheckeredBackground));
		
	}
	
	void put(PalettePixel pixelData) {
		
		put(currentCol , currentRow , pixelData);
		
		currentCol++;
		
		if(currentCol == paletteWidth) {
			
			currentRow++;
			currentCol = 0;
			
		}
		
		if(currentRow == paletteHeight) {
			
			syserr("Too many colors have been added to this palette, resetting palette.");
			currentRow = 0;
			currentCol = 3;
			
		}
		
//		if(currentRow == paletteHeight) {
//
//			paletteHeight += resizeInterval;
//			resizeInterval <<= 1;
//			
//			resizeAndCopy(paletteWidth , paletteHeight);
//			
//		}
		
	}
	
	void setPaletteMemory(ByteBuffer texels , int width , int height) {
		
		activate();
		glTexSubImage2D(GL_TEXTURE_2D , 0 , 0 , 0 , width , height , glDataFormat , glChannelType , texels);
		deactivate();
		
	}
	
	void resizeAndCopy(int newWidth , int newHeight) {
		
		syserr("Resizing, new palette is " + paletteWidth + " x " + paletteHeight);
		
		ByteBuffer newPaletteMemory = BufferUtils.createByteBuffer(paletteHeight * paletteWidth * channelsPerPixel);
		int palettePosition = paletteMemory.position();
		paletteMemory.position(0);
		memCopy(paletteMemory , newPaletteMemory);
		
		activate();			
		glTexImage2D(
			GL_TEXTURE_2D , 
			0 , 
			glDataFormat , 
			paletteWidth , 
			paletteHeight , 
			0 , 
			glDataFormat , 
			glChannelType , 
			newPaletteMemory
		);
		
		newPaletteMemory.position(palettePosition);
		paletteMemory = newPaletteMemory;
		
		deactivate();
		
	}
	
	/**
	 * Puts {@code writeThis} in the palette.
	 * 
	 * @param xIndex — x index to write to
	 * @param yIndex — y index to write to
	 * @param writeThis — color to write in this palette 
	 */
	public void put(int xIndex , int yIndex , PalettePixel writeThis) {

		try(MemoryStack stack = MemoryStack.stackPush()) {

			ByteBuffer imageDataAsPtr = stack.malloc(pixelSizeBytes);
			writeThis.buffer(imageDataAsPtr);
			
			//buffer this into the CPU buffer
			int position = paletteMemory.position();
			paletteMemory.position(yIndex * paletteWidth * pixelSizeBytes + xIndex * pixelSizeBytes);
			writeThis.buffer(paletteMemory);
			paletteMemory.position(position);
			
			imageDataAsPtr.flip();
			
			activate();
			glTexSubImage2D(GL_TEXTURE_2D , 0 , xIndex , yIndex , 1 , 1 , glDataFormat , glChannelType , imageDataAsPtr);
			deactivate();
					
		}
		
	}
		
	PalettePixel getColorByIndices(int xIndex , int yIndex) {
		
		return new PalettePixel(paletteMemory , xIndex , yIndex);
		
	}
	
	PalettePixel getColorByIndicesFromTexelBuffer(ByteBuffer texelBuffer , int xIndex , int yIndex) {
		
		return new PalettePixel(paletteMemory , xIndex , yIndex);
		
	}
	
	/**
	 * Scans the palette texture and attempts to find an instance of {@code colors} within the palette already. If none is found, the new
	 * color is put at the next open space. If {@code colors} is already in the palette, the indices is returned.
	 * 
	 * <B>Note:</B> Must be called from the render thread.
	 * 
	 * @param colors
	 * @return {@code short[]} containing the x and y coordinates of {@code colors}.
	 */
	public short[] putOrGetColors(PalettePixel colors) {
		
		boolean foundMatch = false;
		short row = 0 , col = 0;

		//iterate over palette
		FindMatch: for(; row <= currentRow ; row++) for(; col < currentCol ; col++) {
			
			PalettePixel currentPixel = new PalettePixel(paletteMemory , col , row);
			if(foundMatch = colors.compareTo(currentPixel) == 0) break FindMatch;
			
		}
				  	
		short[] result;
		
		if(!foundMatch) {
			
			put(colors);
			//bump down the currentCol as it is modified in the putColor method so it now points to the next empty space to write to
			col = (short) (currentCol - 1); 
			if(col == -1) col = 0;
			row = currentRow;
					
		}
		
		result = new short[] {col , row};

		return result;
		
	}
	
	/**
	 * Returns a read-only texel buffer for this palette.
	 * 
	 * @return Read-only texel buffer for this palette.
	 */
	public ByteBuffer texelData() {
		
		return paletteMemory.asReadOnlyBuffer();
		
	}
	
	/**
	 * Returns the channels per pixel of pixels of this palette.
	 * 
	 * @return Channels per pixel of pixels of this palette.
	 */
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}

	/**
	 * Returns width of this palette.
	 * 
	 * @return Width of this palette.
	 */
	public int width() {
		
		return paletteWidth;
		
	}

	/**
	 * Returns height of this palette.
	 * 
	 * @return Height of this palette.
	 */
	public int height() {
		
		return paletteHeight;
		
	}

	/**
	 * Returns current column of this palette.
	 * 
	 * @return Current column of this palette.
	 */
	public int currentCol() {
		
		return currentCol;
		
	}

	/**
	 * Returns current row of this palette.
	 * 
	 * @return Current row of this palette.
	 */
	public int currentRow() {
		
		return currentRow;
		
	}

	/**
	 * Sets the alpha channels of the background checkered colors to 0, making those colors invisible.
	 */
	public void hideCheckeredBackground() {
		
		if(channelsPerPixel == 1 || channelsPerPixel == 3) return;
		
		lighterCheckeredBackground[channelsPerPixel - 1] = 0;
		darkerCheckeredBackground[channelsPerPixel - 1] = 0;
		
		put(0 , 0 , new PalettePixel(darkerCheckeredBackground));
		put(1 , 0 , new PalettePixel(lighterCheckeredBackground));
		
	}

	/**
	 * Sets the alpha channels of the background checkered colors to 255, making those colors opaque.
	 */
	public void showCheckeredBackground() {

		if(channelsPerPixel == 1 || channelsPerPixel == 3) return;
		
		lighterCheckeredBackground[channelsPerPixel - 1] = -1;
		darkerCheckeredBackground[channelsPerPixel - 1] = -1;
		
		put(0 , 0 , new PalettePixel(darkerCheckeredBackground));
		put(1 , 0 , new PalettePixel(lighterCheckeredBackground));
		
	}
	
	/**
	 * Returns a list representation of the contents of this palette. The elements of this list
	 * 
	 * @return List containing the colors of this palette.
	 */
	public List<ColorPixel> getColorsAsList(int size) {

		int numberPixels = (currentRow * paletteWidth) + currentCol;
		List<ColorPixel> pixels = new ArrayList<>();
		
		int position = (2 + Math.max(numberPixels - 2 - size , 0)) * pixelSizeBytes;
		int palettePosition = numberPixels * pixelSizeBytes;
		
		while(position < palettePosition) {
			
			ChannelBuffer color = new ChannelBuffer();
			for(byte i = 0 ; i < channelsPerPixel ; i++) color.i(i , paletteMemory.get(position++));
			pixels.add(color);
			
		}
		
		return pixels;
		
	}
	
	private void initializeCheckeredBackgroundColors() {
	
		if(channelsPerPixel == 2 || channelsPerPixel == 4) {
			
			for(int i = 0 ; i < channelsPerPixel - 1; i++) {
				
				darkerCheckeredBackground[i] = 77;
				lighterCheckeredBackground[i] = 115;
				
			}
			
			darkerCheckeredBackground[channelsPerPixel - 1] = -1;
			lighterCheckeredBackground[channelsPerPixel - 1] = -1;
			
		} else for(int i = 0 ; i < channelsPerPixel ; i++) {
			
			darkerCheckeredBackground[i] = 77;
			lighterCheckeredBackground[i] = 115;
			
		}
		
	}
	
	/**
	 * This class exists mainly because java does not have unsigned primitives. This means that code that wants a java {@code byte} to be 
	 * unsigned, as it is in the GPU, may have errors which this class tries to fix. As well, there are basically different kinds of pixels 
	 * in this application, image pixels and palette pixels. Palette pixels are more complicated because their number of bytes per channel 
	 * and number of channels per pixel vary. 
	 * 
	 * @author Chris Brown
	 *
	 */
	public class PalettePixel implements ColorPixel , Comparable<ColorPixel> {

		private byte
			red ,
			green ,
			blue ,
			alpha;
		
		PalettePixel(ByteBuffer paletteBuffer , int pixelXIndex , int pixelYIndex) {
						
			ByteBuffer slice = paletteBuffer.slice(
				(pixelYIndex * paletteWidth * pixelSizeBytes) + (pixelXIndex * pixelSizeBytes) , 
				pixelSizeBytes
			);
			
			for(int i = 0 ; i < channelsPerPixel ; i++) setByIndex(slice.get() , i);
					
		}
		
		PalettePixel(ByteBuffer source) {
			
			for(int i = 0 ; i < channelsPerPixel ; i++) setByIndex(source.get(), i);
			
		}
		
		PalettePixel(byte[] channelValues) {
		
			for(int i = 0 ; i < channelsPerPixel ; i++) setByIndex(channelValues[i] , i);
									
		}
		
		PalettePixel(byte red , byte green , byte blue , byte alpha) {
			
			setByIndex(red , 0);
			setByIndex(green , 1);
			setByIndex(blue , 2);
			setByIndex(alpha , 3);
			
		}
		
		PalettePixel(ColorPixel other) {
			
			this(other.r() , other.g() , other.b() , other.a());
			
		}
		
		/**
		 * Stores this pixel in {@code buffer}. 
		 * 
		 * @param buffer — buffer to write to
		 */
		public void buffer(ByteBuffer buffer) {
			
			require(buffer.remaining() >= pixelSizeBytes);
			
			for(int i = 0 ; i < channelsPerPixel ; i ++) buffer.put((byte) index(i));
			
		}
		
		/**
		 * Returns the color channel corresponding to {@code index}.
		 * 
		 * @return Color channel corresponding to {@code index}.
		 */
		public byte index(int index) {
			
			return switch(index) {
				case 0 -> red;
				case 1 -> green;
				case 2 -> blue;
				case 3 -> alpha;
				default -> throw new IllegalArgumentException(index + " is not a valid color channel index");
			};
			
		}
		
		/**
		 * Sets the color channel corresponding to {@code index} to {@code value}.
		 * 
		 * @param value — a color value
		 * @param index — channel index value
		 */
		public void setByIndex(byte value , int index) {
			
			switch(index) {
				case 0 -> red = value;
				case 1 -> green = value;
				case 2 -> blue = value;
				case 3 -> alpha = value;
			}
			
		}
		
		@Override public String toString() { 
			
			StringBuilder asString = new StringBuilder();
			asString.append("[Red: ");
			asString.append(Byte.toUnsignedInt(red));
			asString.append(", Green: ");
			asString.append(Byte.toUnsignedInt(green));
			asString.append(", Blue: ");
			asString.append(Byte.toUnsignedInt(blue));
			asString.append(", Alpha: ");
			asString.append(Byte.toUnsignedInt(alpha));
			asString.append("]");
		
			return asString.toString();
			
		}

		@Override public byte r() {

			return red;
			
		}

		@Override public byte g() {

			return green;
			
		}

		@Override public byte b() {

			return blue;
			
		}

		@Override public byte a() {

			return alpha;
			
		}

		@Override public short ur() {

			return (short)Byte.toUnsignedInt(red);
			
		}

		@Override public short ug() {

			return (short)Byte.toUnsignedInt(green);
			
		}

		@Override public short ub() {

			return (short)Byte.toUnsignedInt(blue);
			
		}

		@Override public short ua() {

			return (short)Byte.toUnsignedInt(alpha);
			
		}

	}
	
}
 