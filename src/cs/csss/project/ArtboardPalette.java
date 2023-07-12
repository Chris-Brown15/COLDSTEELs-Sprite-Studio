package cs.csss.project;

import static cs.core.utils.CSUtils.require;
import static org.lwjgl.opengl.GL11C.GL_RED;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30C.glTexSubImage2D;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import cs.core.graphics.CSTexture;
import cs.csss.core.FlexableGraphic;

/**
 * Extender of {@code CSTexture} used extensively as a container of color values. The indices of pixels in the artboard are the values 
 * pixels of {@code ArtboardTexture} will contain, which are used in shaders to lookup and display the correct color. Each 
 * {@code ArtboardPalette} has a {@link ArtboardPalette#defaultColor defaultColor} which is always stored at the very last position in the
 * palette.
 * 
 * @author Chris Brown
 *
 */
public class ArtboardPalette extends CSTexture {

	public static final int 
		paletteWidth = 256 ,
		paletteHeight = 256 ,
		glChannelType = GL_UNSIGNED_BYTE;
	;
	
	static final byte[] transparentBackgroundDarkerPixel = new byte[] {77 , 77 , 77 , -1};
	static final byte[] transparentBackgroundLighterPixel = new byte[] {115 , 115 , 115 , -1};
	
	short
		currentRow = 0 ,
		currentCol = 0 
	;
	
	final int channelsPerPixel;
	
	int	
		glDataFormat ,
		pixelSizeBytes
	;

	private ByteBuffer paletteMemory;
	
	public ArtboardPalette(
		int channelsPerPixel , 
		final byte defaultRed , 
		final byte defaultGreen , 
		final byte defaultBlue , 
		final byte defaultAlpha
	) {
		
		this.channelsPerPixel = channelsPerPixel;
				
	}
		
	public void initialize() {

		FlexableGraphic graphic = new FlexableGraphic(paletteWidth , paletteHeight , 1 , channelsPerPixel , 0xff);
		
		initialize(graphic , ArtboardTexture.textureOptions);
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
		
		put(this.new PalettePixel(transparentBackgroundDarkerPixel));
		put(this.new PalettePixel(transparentBackgroundLighterPixel));
		
	}
	
	void put(PalettePixel pixelData) {
		
		put(currentCol , currentRow , pixelData);
		
		currentCol++;
		
		if(currentCol == paletteWidth) {
			
			currentRow++;
			currentCol = 0;
			
		}
		
		if(currentRow == paletteHeight) {
			
			//TODO: handle this by resizing the palette and image texture.
			System.err.println("Palette is too large: " + (255 * 255) + " colors is max amount allowed.");
			currentRow = 0;
			
		}
		
	}
	
	void put(final int xIndex , final int yIndex , final PalettePixel writeThis) {

		try(MemoryStack stack = MemoryStack.stackPush()) {

			ByteBuffer imageDataAsPtr = stack.malloc(pixelSizeBytes);
			writeThis.buffer(imageDataAsPtr);
			
			//buffer this into the CPU buffer
			int position = paletteMemory.position();
			paletteMemory.position(yIndex * paletteWidth * pixelSizeBytes + xIndex * pixelSizeBytes);
			writeThis.buffer(paletteMemory);
			paletteMemory.position(position);
			
			imageDataAsPtr.rewind();
			
			activate();
			glTexSubImage2D(GL_TEXTURE_2D , 0 , xIndex , yIndex , 1 , 1 , glDataFormat , glChannelType , imageDataAsPtr);
			deactivate();
					
		}
		
	}
		
	PalettePixel getColorByIndices(final int xIndex , final int yIndex) {
		
		return new PalettePixel(paletteMemory , xIndex , yIndex);
		
	}
	
	PalettePixel getColorByIndicesFromTexelBuffer(final ByteBuffer texelBuffer , final int xIndex , final int yIndex) {
		
		return new PalettePixel(paletteMemory , xIndex , yIndex);
		
	}
	
	/**
	 * Scans the palette texture and attempts to find an instance of {@code colors} within the palette already. If none is found, the new
	 * color is put at the next open space. If {@code colors} is already in the palette, the indices is returned.
	 * 
	 * <B>Note:</B> Must be called from the render thread.
	 * 
	 * @param colors
	 * @return {@code int[]} containing the x and y coordinates of {@code colors}.
	 */
	public short[] putOrGetColors(PalettePixel colors) {
		
		boolean foundMatch = false;
		short 
			row = 0 ,
			col = 0
		;

		//iterate over palette
		FindMatch: {
			
			for(; row <= currentRow ; row++) for(; col < currentCol ; col++) {
				
				PalettePixel currentPixel = new PalettePixel(paletteMemory , col , row);
				if(foundMatch = colors.compareTo(currentPixel) == 0) break FindMatch;
				
			}
			
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
	
	public ByteBuffer texelData() {
		
		return paletteMemory.asReadOnlyBuffer();
		
	}
	
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}

	public void toPNG(String filePathAndName) {

		stbi_write_png(filePathAndName , paletteWidth , paletteHeight , channelsPerPixel , texelData() , 0);
				
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
	public class PalettePixel implements Comparable<PalettePixel> {

		private byte
			red ,
			green ,
			blue ,
			alpha
		;
		
		PalettePixel(ByteBuffer paletteBuffer , int pixelXIndex , int pixelYIndex) {
						
			ByteBuffer slice = paletteBuffer.slice(
				(pixelYIndex * paletteWidth * pixelSizeBytes) + (pixelXIndex * pixelSizeBytes) , 
				pixelSizeBytes
			);
			
			for(int i = 0 ; i < channelsPerPixel ; i++) setByIndex(slice.get() , i);
					
		}
		
		PalettePixel(byte[] channelValues) {
			
			for(int i = 0 ; i < channelsPerPixel ; i++) setByIndex(channelValues[i] , i);
									
		}
		
		public void buffer(ByteBuffer buffer) {
			
			require(buffer.remaining() >= pixelSizeBytes);
			
			for(int i = 0 ; i < channelsPerPixel ; i ++) buffer.put((byte) index(i));
			
		}
		
		@Override public int compareTo(PalettePixel o) {

			if(o == null) return -1;			
			for(int i = 0 ; i < channelsPerPixel ; i++) if(o.index(i) != this.index(i)) return -1;			
			return 0;
			
		}
		
		public byte red() {
			
			return red;
			
		}

		public byte green() {
			
			return green;
			
		}

		public byte blue() {
			
			return blue;
			
		}

		public byte alpha() {
			
			return alpha;
			
		}

		public byte index(int index) {
			
			return switch(index) {
				case 0 -> red;
				case 1 -> green;
				case 2 -> blue;
				case 3 -> alpha;
				default -> throw new IllegalArgumentException(index + " is not a valid color channel index");
			};
			
		}
		
		public void setByIndex(byte value , int index) {
			
			switch(index) {
				case 0 -> red = value;
				case 1 -> green = value;
				case 2 -> blue = value;
				case 3 -> alpha = value;
			}
			
		}
		
		public String toString() { 
			
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

		public Number[] toArray() {
			
			Number[] array = new Number[channelsPerPixel];
			for(int i = 0 ; i < array.length ; i++) array[i] = index(i);
			return array;
			
		}
		
	}
	
	public class PalettePosition extends PalettePixel {

		private int 	
			xIndex ,
			yIndex
		;
		
		PalettePosition(byte[] channelValues , int xIndex , int yIndex) {
			
			super(channelValues);
			
			this.xIndex = xIndex;
			this.yIndex = yIndex;
			
		}
		
		PalettePosition(ByteBuffer container , int xIndex , int yIndex) {
			
			super(container , xIndex , yIndex);			
			
			this.xIndex = xIndex;
			this.yIndex = yIndex;
			
		}
		
		public int xIndex() {
			
			return xIndex;
			
		}

		public int yIndex() {
			
			return yIndex;
			
		}
		
	}
	
}
 