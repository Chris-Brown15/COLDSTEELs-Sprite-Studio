package cs.csss.project;

import static cs.csss.engine.Logging.*;
import static org.lwjgl.opengl.GL11C.GL_RED;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30C.GL_BLEND;
import static org.lwjgl.opengl.GL30C.glTexSubImage2D;
import static org.lwjgl.opengl.GL30C.glTexImage2D;
import static org.lwjgl.opengl.GL30C.glDisable;
import static org.lwjgl.opengl.GL30C.glEnable;

import static org.lwjgl.system.MemoryUtil.memCopy;

import static cs.core.graphics.StandardRendererConstants.POSITION_2D;
import static cs.core.graphics.StandardRendererConstants.UV;
import static cs.core.graphics.StandardRendererConstants.STATIC_VAO;
import static cs.core.graphics.StandardRendererConstants.UINT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import cs.core.graphics.CSRender;
import cs.core.graphics.CSTexture;
import cs.core.graphics.CSVAO;
import cs.core.graphics.utils.VertexBufferBuilder;
import cs.core.utils.data.exceptions.StackUnderflowException;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.CSSSCamera;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.Engine;
import cs.csss.engine.Pixel;
import cs.csss.engine.TransformPosition;
import cs.csss.misc.files.CSFolder;
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
	
	public static final int MAX_WIDTH = 256 , MAX_HEIGHT = 256;
	
	short currentRow = 0 , currentCol = 0;	

	final int channelsPerPixel;	
	
	private int	glDataFormat , pixelSizeBytes , paletteWidth = MAX_WIDTH , paletteHeight = MAX_HEIGHT;
	
	private volatile ByteBuffer paletteMemory;
	
	/**
	 * For these arrays, if there is an alpha channel available, it will contain either -1 or 0 depending upon if the background is 
	 * visible. In the case there is no alpha value, the background will always be visible
	 */
	final byte[] darkerCheckeredBackground , lighterCheckeredBackground;
	
	private CSRender render = null;
	private CSVAO vao;
	private TransformPosition transform;
	
	/**
	 * Creates an artboard palette.
	 * 
	 * @param channelsPerPixel — number of channels per pixel of this palette
	 */
	public ArtboardPalette(int channelsPerPixel) {
		
		this(channelsPerPixel , MAX_WIDTH , MAX_HEIGHT);

	}
	
	/**
	 * Creates an artboard palette.
	 * 
	 * @param channelsPerPixel — number of channels per pixel for this palette
	 * @param width — initial width of this palette
	 * @param height — initial height of this palette
	 */
	public ArtboardPalette(int channelsPerPixel , int width , int height) {
		
		this.paletteWidth = width;
		this.paletteHeight = height;
		this.channelsPerPixel = channelsPerPixel;
		darkerCheckeredBackground = new byte[channelsPerPixel];
		lighterCheckeredBackground = new byte[channelsPerPixel];
		initializeCheckeredBackgroundColors();
		
	}
		
	/**
	 * Initializes this artboard palette.
	 */
	@RenderThreadOnly public void initialize() {

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

	 	VertexBufferBuilder vertices = new VertexBufferBuilder(POSITION_2D|UV);
	 	vertices.size(paletteWidth, paletteHeight);
	 	vertices.midpoint(0 , 0);
	 	transform = new TransformPosition(vertices.attribute(POSITION_2D));
	 	vao = new CSVAO(vertices.attributes , STATIC_VAO , vertices.get()); 	 		 		
	 	vao.drawAsElements(6, UINT);
	 	render = new CSRender(vao , CSSSProject.theTextureShader() , this);
	 		
	}
	
	void put(ColorPixel pixelData) {
		
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
		glTexImage2D(GL_TEXTURE_2D , 0 , glDataFormat , paletteWidth , paletteHeight , 0 , glDataFormat , glChannelType , newPaletteMemory);
		
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
	public void put(int xIndex , int yIndex , ColorPixel writeThis) {

		try(MemoryStack stack = MemoryStack.stackPush()) {
			
			//buffer this into the CPU buffer
			int position = paletteMemory.position();
			paletteMemory.position(yIndex * paletteWidth * pixelSizeBytes + xIndex * pixelSizeBytes);
			ColorPixel.buffer(paletteMemory , writeThis , pixelSizeBytes);
			paletteMemory.position(position);

			ByteBuffer imageDataAsPtr = stack.malloc(pixelSizeBytes);
			ColorPixel.buffer(imageDataAsPtr, writeThis, pixelSizeBytes);			
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
	public short[] putOrGetColors(ColorPixel colors) {
	
		short[] indices = getIndicesOfColor(colors);
		if(indices == null) { 
			
			indices = new short[2];
			indices[0] = currentCol;
			indices[1] = currentRow;
			put(colors);
			
		}
		
		return indices;
		
	}
			
	/**
	 * Returns the indices of the given color in this palette, or <code>null</code> if it is not found. 
	 * <p>
	 * 	<b>Note:</b> this method does not check for {@code color} in the entire palette. It only checks the rows and columns that have been modified
	 * 	by {@link ArtboardPalette#putOrGetColors(ColorPixel) putOrGetColors}, which places colors in the palette sequentially next to each other 
	 * 	along rows of the palette from left to right. Therefore, if you modify the palette directly and you place your colors elsewhere, they likeky
	 *  won't be found by this method. 
	 * </p>
	 * 
	 * @param color — the color to find
	 * @return The indices of {@code color} in this palette if found, or <code>null</code> otherwise.
	 */
	public short[] getIndicesOfColor(ColorPixel color) {
		
		boolean foundMatch = false;
		short row = 0 , col = 0;

		//iterate over palette
		FindMatch: for(; row <= currentRow ; row++) for(; col < currentCol ; col++) {
			
			PalettePixel currentPixel = new PalettePixel(paletteMemory , col , row);
			if(foundMatch = color.compareTo(currentPixel) == 0) break FindMatch;
			
		}
				  	
		if(!foundMatch) return null;

		return new short[] {col , row};
		
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
	
	/**
	 * Removes the most recently added color from this palette.
	 * 
	 * @throws StackUnderflowException if there is no color to remove from this palette.
	 */
	@RenderThreadOnly public void popRecentColor() {
		
		byte tff = (byte) 0xff;
		PalettePixel defaultColor = new PalettePixel(tff , tff , tff , tff);
		put(currentCol , currentRow , defaultColor);
		
		currentCol--;
		if(currentCol < 0) {
			
			currentCol = (short)(paletteWidth - 1);
			currentRow--;
			if(currentRow < 0) throw new StackUnderflowException("This palette is completely empty.");
			
		}		
		
	}
	
	/**
	 * Renders this palette at the given position using the given camera.
	 * 
	 * @param camera — camera to render with
	 * @param midX — the x coordinate in world space to render this palette at
	 * @param midY — the y coordinate in world space to render this palette at
	 */
 	@RenderThreadOnly public void render(CSSSCamera camera , float midX , float midY) {
		
 		CSSSShader textureShader = CSSSProject.theTextureShader();

 		transform.moveTo(midX , midY);
 		textureShader.updatePassVariables(camera.projection() , camera.viewTranslation() , transform.translation);
 		
 		glDisable(GL_BLEND);
 		render.draw(); 		
 		glEnable(GL_BLEND);
 		
 		//undo changed active shader
 		CSSSProject.setTheCurrentShader(CSSSProject.thePaletteShader());
 		this.deactivate();
 		
	}
 	
 	/**
 	 * Converts the given world coordinates to pixel indices, which are stored in {@code destination}.
 	 * 
 	 * @param worldCoordinates — array containing a world x and y coordinate
 	 * @param destination — array to store pixel indices of the given coordinates
 	 */
 	public void worldCoordinateToPixelIndices(float[] worldCoordinates , int[] destination) { 
 		
 		Objects.requireNonNull(worldCoordinates);
 		Objects.requireNonNull(destination);
 		
 		destination[0] = (int)(worldCoordinates[0] - transform.leftX());
 		destination[1] = (int)(worldCoordinates[1] - transform.bottomY());
 		
 	}
 	
 	/**
 	 * Converts the given worlc coordinates into pixel indices, returning the result as an array.
 	 * 
 	 * @param worldCoordinates — array containing a world x and y coordinate
 	 * @return Array containing pixel indices of the given coordinates.
 	 */
 	public int[]  worldCoordinateToPixelIndices(float[] worldCoordinates) {
 		
 		Objects.requireNonNull(worldCoordinates);
 		int[] destination = new int[2];
 		worldCoordinateToPixelIndices(worldCoordinates, destination);
 		return destination;
 		 	
 	}
 	
 	/**
 	 * Returns the position of this palette.
 	 * 
 	 * @return The position of this palette.
 	 */
 	public TransformPosition position() {
 		
 		return transform;
 		
 	}
	
 	/**
 	 * Returns the palette pixel at the given indices.
 	 * 
 	 * @param x — x index of the palette pixel to get
 	 * @param y — y index of the palette pixel to get
 	 * @return {@link ColorPixel} containing the color values in the palette at the given indices.
 	 */
 	public ColorPixel get(int x , int y) {
 		
 		return new PalettePixel(paletteMemory , x , y);
 		
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
	 * Dumps the palette to a file.
	 */
	@RenderThreadOnly public void dumpToFile() {
		
		if(!Engine.isDebug()) throw new IllegalStateException("This method can only be invoked in debug mode.");
		
 		File file = CSFolder.getRoot("debug").createFile(
 			"Palette Dump at " + LocalTime.now().toString().replaceAll(":", "_") , 
 			null
 		).asFile();
		
 		try(FileOutputStream writer = new FileOutputStream(file)) {

// 			writer.getChannel().write(paletteMemory);
 			
 			ByteBuffer texels = org.lwjgl.system.MemoryUtil.memCalloc(paletteWidth * paletteHeight * channelsPerPixel);
 			
 			activate();

 			int graphicChannels = switch(channelsPerPixel) { 			
 				case 1 -> GL_RED;
 				case 2 -> GL_RG;
 				case 3 -> GL_RGB;
 				case 4 -> GL_RGBA;
 				default -> throw new IllegalArgumentException(); 			
 			};
 			 			
 			org.lwjgl.opengl.GL30C.glGetTexImage(GL_TEXTURE_2D , 0 , graphicChannels , GL_UNSIGNED_BYTE , texels);
 			
 			writer.getChannel().write(texels);
 			deactivate();
 			writer.flush();
 			
 			org.lwjgl.system.MemoryUtil.memFree(texels);
 			
 		} catch (FileNotFoundException e) {

 			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}
 		 
		
	}
	
	@Override public void shutDown() {
		
		super.shutDown();
		if(render != null) render.shutDown();
		
	}
	
	/**
	 * Class representing palette texture pixels. Instances are created by a palette and will be filled out according to the palette's state, such
	 * as channels per pixel.
	 */
	public class PalettePixel implements ColorPixel {

		private byte red , green , blue , alpha;
		
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
		
		/**
		 * Creates a new palette pixel from the given array. The values in the array are copied into this pixel from 
		 * {@code [0 , channelsPerPixel - 1]}, so the given array must have enough spaces to satisfy this.
		 * 
		 * @param channelValues — array of channel values
		 */
		public PalettePixel(byte[] channelValues) {
		
			for(int i = 0 ; i < channelsPerPixel ; i++) setByIndex(channelValues[i] , i);
									
		}
		
		/**
		 * Creates a new palette pixel from the given byte values.
		 * 
		 * @param red — red channel value
		 * @param green — green channel value
		 * @param blue — blue channel value
		 * @param alpha — alpha channel value
		 */
		public PalettePixel(byte red , byte green , byte blue , byte alpha) {
			
			setByIndex(red , 0);
			setByIndex(green , 1);
			setByIndex(blue , 2);
			setByIndex(alpha , 3);
			
		}
		
		/**
		 * Creates a new palette pixel frm the channel values of {@code other}.
		 * 
		 * @param other — another palette pixel
		 */
		public PalettePixel(ColorPixel other) {
			
			this(other.r() , other.g() , other.b() , other.a());
			
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

		@Override public Pixel clone() {

			return new PalettePixel(red , green , blue , alpha);
			
		}

	}
	
}
 