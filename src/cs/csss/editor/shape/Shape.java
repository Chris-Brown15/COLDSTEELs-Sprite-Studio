/**
 * 
 */
package cs.csss.editor.shape;

import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30C.GL_RGBA;
import static cs.core.graphics.StandardRendererConstants.MAG_FILTER_LINEAR;
import static cs.core.graphics.StandardRendererConstants.MIN_FILTER_LINEAR;
import static cs.core.graphics.StandardRendererConstants.POSITION_2D;
import static cs.core.graphics.StandardRendererConstants.STREAM_VAO;
import static cs.core.graphics.StandardRendererConstants.UINT;
import static cs.core.graphics.StandardRendererConstants.UV;
import static org.lwjgl.opengl.GL30C.GL_RED;
import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.GL_RGB;
import static org.lwjgl.opengl.GL30C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30C.glGetTexImage;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Vector3f;
import cs.core.graphics.CSRender;
import cs.core.graphics.CSTexture;
import cs.core.graphics.CSVAO;
import cs.core.graphics.utils.VertexBufferBuilder;
import cs.core.utils.files.CSGraphic;
import cs.csss.annotation.FreeAfterUse;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.editor.Rasterizable;
import cs.csss.engine.CSSSCamera;
import cs.csss.engine.ChannelBuffer;
import cs.csss.engine.ColorPixel;
import cs.csss.engine.LookupPixel;
import cs.csss.project.Artboard;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.project.TextureShader;
import cs.csss.project.utils.Artboards;
import cs.csss.utils.ByteBufferUtils.CorrectedResult;

/**
 * Base class for all shapes, providing basic behavior and common variables to all shapes.
 */
public abstract class Shape extends Rasterizable {

	/**
	 * Converts a channels per pixel value into a value for shapes. Shapes must have a transparency, so if the given channels does not indicate a 
	 * transparency, it is converted so the result is the same color format, but with transparency. 
	 * 
	 * @param channels a number of channels per pixel
	 * @return Number of channels per pixel that allows for transparency.
	 * @throws IllegalArgumentException if {@code channels} is invalid as a channels per pixel. 
	 */
	public static int snapChannelsForShape(int channels) {

		if(channels < 1 || channels > 4) throw new IllegalArgumentException("Channels parameter invalid as a channels per pixel: " + channels);
		
		if(channels == 1) channels = 2;
		else if (channels == 3) channels = 4;
		
		return channels;
		
	}
	
	/**
	 * Sets a destination color propperly from the given source color. Shapes must have transparency, but they can be grayscale or RGB. If a shape is 
	 * grayscale transparency, the <em>green</em> channel of the shape's border or fill color contains the transparency. This method sets the green value
	 * of {@code destinationColor} to the alpha channel value of {@code sourceColor} if {@code channels == 2}, while also setting the remaining values 
	 * from source to destination.
	 * 
	 * @param sourceColor a color to set {@code destinationColor} to 
	 * @param destinationColor channel buffer to write the values of {@code sourceColor} to
	 * @param channels number of channels per pixel to interpret {@code destinationColor} as supporting
	 * @throws IllegalArgumentException if {@code channels} is invalid as a channels per pixel. 
	 * @throws NullPointerException if either {@code sourceColor} or {@code destinationColor} is <code>null</code>.
	 */
	public static void formatColor(ColorPixel sourceColor , ChannelBuffer destinationColor , int channels) {

		if(channels < 1 || channels > 4) throw new IllegalArgumentException("Channels parameter invalid as a channels per pixel: " + channels);
		Objects.requireNonNull(sourceColor);
		Objects.requireNonNull(destinationColor);
		
		byte z = (byte)0;		
		if(channels == 2) destinationColor.set(sourceColor.r(), sourceColor.a(), z, z);		
		else destinationColor.set(sourceColor);
		
	}
	
	/**
	 * Whether this shape should be filled.
	 */
	protected boolean fill;

	/**
	 * Color of the border of the shape and the fill of the shape.
	 */
	protected ChannelBuffer borderColor = new ChannelBuffer(), fillColor = new ChannelBuffer();
	
	/**
	 * Channels per pixel of colors for this shape.
	 */
	protected final int channelsPerPixel;
		
	/**
	 * Initializes shape data.
	 * 
	 * @param defaultColor default color for this shape
	 * @param channelsPerPixel number of channels per pixel for colors of this shape
	 * @param formatColor whether to reformat {@code borderColor} and {@code fillColor} according to the semantics of 
	 * 					  {@link Shape#formatColor(ColorPixel, ChannelBuffer, int)}
	 */
	protected Shape(ColorPixel defaultColor , int channelsPerPixel, boolean formatColor) {

		this(defaultColor, defaultColor, channelsPerPixel, formatColor);
		
	}

	/**
	 * Initializes shape data with given initial colors for fill and border.
	 * 
	 * @param borderColor color of the border of the shape
	 * @param fillColor color of the shape inside the border
	 * @param channelsPerPixel number of channels per pixel
	 * @param formatColor whether to reformat {@code borderColor} and {@code fillColor} according to the semantics of 
	 * 					  {@link Shape#formatColor(ColorPixel, ChannelBuffer, int)}
	 */
	protected Shape(ColorPixel borderColor , ColorPixel fillColor , int channelsPerPixel, boolean formatColor) {
		
		this.channelsPerPixel = snapChannelsForShape(channelsPerPixel);
		
		if(formatColor) {
			
			borderColor(borderColor);
			fillColor(fillColor);

		} else {
			
			this.borderColor.set(borderColor);
			this.fillColor.set(fillColor);
			
		}
		
	}
	
	/**
	 * Resets this shape. The effects of this method are that the currently set state for the shape is made reflected in its graphical 
	 * representation. Therefore, this method most likely needs to be invoked in the render thread.
	 */
	public abstract void reset();
	
	/**
	 * Performs a deep copy of this shape into a new one, returning the result. The resulting shape needs to be {@link #reset()} before it is ready
	 * to be rendered.
	 * 
	 * @param <X> type of the resulting shape
	 * @return Deep copied shape from this shape.
	 */
	public abstract <X extends Shape> X copy(); 
	
	/**
	 * Gets the texture storing this shape as a byte buffer. 
	 * 
	 * @param width width of the image
	 * @param height height of the image 
	 * @return {@link cs.csss.annotation.FreeAfterUse @FreeAfterUse} ByteBuffer containing the texture image.
	 */
	@RenderThreadOnly protected @FreeAfterUse ByteBuffer textureToBuffer(int width , int height) {
		
		ByteBuffer destination = memAlloc(width * height * channelsPerPixel);
		
		int glFormat = switch(channelsPerPixel) {
			case 1 -> GL_RED;
			case 2 -> GL_RG;
			case 3 -> GL_RGB;
			case 4 -> GL_RGBA;
			default -> throw new IllegalArgumentException(channelsPerPixel + " is invalid as a number of channels.");
		};
		
		texture.activate();
		glGetTexImage(GL_TEXTURE_2D , 0 , glFormat , GL_UNSIGNED_BYTE , destination);
		texture.deactivate();

		return destination;
		
	}
	
	/**
	 * Default implementation for rasterization of a shape.
	 * 
	 * @param artboard the artboard to rasterize into
	 * @param imageWidth the width of the shape/its image
	 * @param imageHeight the height of the shape/its image 
	 */
	protected final void defaultRasterize(Artboard artboard , int imageWidth , int imageHeight) {

		ByteBuffer destination = textureToBuffer(imageWidth, imageHeight);
		
		CorrectedResult correct = Artboards.worldCoordinatesToCorrectArtboardCoordinates(
			artboard, 
			(int)leftX(), 
			(int)bottomY(), 
			imageWidth, 
			imageHeight
		);
		
		if(correct == null) return;

		LookupPixel[][] region = new LookupPixel[imageHeight][imageWidth];		
		ChannelBuffer pixelBuffer = new ChannelBuffer();
		ArtboardPalette palette = artboard.activeLayer().palette();
		
		LookupPixel borderColorLookup;
		LookupPixel fillColorLookup;
		
		if(channelsPerPixel == 2) {

			if(fillColor.g() == 0 || !fill) fillColorLookup = null;
			else fillColorLookup = palette.putOrGetColors(fillColor);

			if(borderColor.g() == 0) borderColorLookup = null;
			else borderColorLookup = palette.putOrGetColors(borderColor);
		
		} else {

			if(fillColor.a() == 0 || !fill) fillColorLookup = null;
			else fillColorLookup = palette.putOrGetColors(fillColor);

			if(borderColor.a() == 0) borderColorLookup = null;
			else borderColorLookup = palette.putOrGetColors(borderColor);
		
		}
		
		for(int row = 0 ; row < imageHeight ; row++) {
			
			int rowOffset = row * imageWidth * channelsPerPixel;

			for(int col = 0 ; col < imageWidth ; col++) {
				
				int offset = rowOffset + (col * channelsPerPixel);
							
				if(channelsPerPixel == 2) {
					
					pixelBuffer.set(destination.get(offset) , destination.get(offset + 1) , (byte)0 , (byte)0);
					if(pixelBuffer.g() == 0) continue;
					
				} else {
					
					pixelBuffer.set(
						destination.get(offset) , 
						destination.get(offset + 1) , 
						destination.get(offset + 2) , 
						destination.get(offset + 3)
					);
					
					//skip invisible pixels.          
					if(pixelBuffer.a() == 0) continue;
					
				}
				
				if(pixelBuffer.compareTo(borderColor) == 0) region[row][col] = borderColorLookup;
				else if (pixelBuffer.compareTo(fillColor) == 0) region[row][col] = fillColorLookup;
				
			}
			
		}
		
		memFree(destination);		
		
		artboard.putColorsInImage(correct , region);
		
		hide(true);
			
	}
	
	/**
	 * Rasterizes this shape, storing the result in the given artboard.
	 * 
	 * @param target artboard to store the resulting rasterize in.
	 */
	public abstract void rasterize(Artboard target);

	/**
	 * Renders this shape on the GPU.
	 * 
	 * @param camera the camera to render with
	 */
	@RenderThreadOnly @Override public void render(CSSSCamera camera) {
		
		if(hide.get() || vao == null) return;
		
		TextureShader textureShader = CSSSProject.theTextureShader();
		textureShader.updatePassVariables(camera.projection(), camera.viewTranslation() , translation);
		textureShader.updateTextures(null, texture);
		textureShader.channels(channels());
		textureShader.activate();
		vao.activate();
		vao.draw();
	
	}

	/**
	 * Called at the end of {@link #reset()} to set of the GPU resources for this shape from the reset graphic. 
	 * 
	 * @param graphic graphic for the texture
	 * @param width width of the graphic
	 * @param height height of the graphic
	 */
	@RenderThreadOnly protected final void defaultReset(CSGraphic graphic , int width , int height) {

		if(render != null) render.shutDown();

		float midX = midX();
		float midY = midY();
		
		vao = new CSVAO();
		VertexBufferBuilder builder = new VertexBufferBuilder(POSITION_2D|UV);
		builder.size(this.textureWidth = width , this.textureHeight = height);

		vao.initialize(builder.attributes, STREAM_VAO, builder.get());
		vao.drawAsElements(6, UINT);
		
		texture = new CSTexture(graphic , MIN_FILTER_LINEAR|MAG_FILTER_LINEAR);
		
		graphic.shutDown();
		//used for resource freeing purposes.
		render = new CSRender(vao , texture);

		moveTo(midX , midY);
		
	}
	
	/**
	 * Returns whether this shape is filled.
	 * 
	 * @return Whether this shape is filled.
	 */
	public boolean fill() {
		
		return fill;
		
	}
	
	/**
	 * Sets whether to fill in this shape, but does not fill it in now. {@link #reset()} must be called for the fill to take effect.
	 * 
	 * @param fill whether to fill this shape
	 * @return {@code this}.
	 */
	public void fill(boolean fill) {
		
		this.fill = fill;
				
	}
	
	/**
	 * Toggles whether this shape is filled. Does not immediately make the shape filled, call {@link #reset()} for that.
	 */
	public void toggleFill() {
		
		fill = !fill;
		
	}
	
	/**
	 * Translates this shape {@code (x , y)} world space coordinates.
	 * 
	 * @param x x translation amount
	 * @param y y translation amount
	 */
	public void translate(int x, int y) {
		
		translation.translate(x, y , 0);
		
	}
	
	/**
	 * Moves this shape to the given position in world space.
	 * 
	 * @param xPosition x position to move this shape to
	 * @param yPosition y position to move this shape to 
	 */
	public void moveTo(int xPosition, int yPosition) {

		moveTo((float)xPosition , (float)yPosition);
		
	}

	/**
	 * Moves this shape to the given position in world space.
	 * 
	 * @param xPosition x position to move this shape to
	 * @param yPosition y position to move this shape to 
	 */
	public void moveTo(float xPosition , float yPosition) {
		
		Vector3f translationVector = new Vector3f();
		translation.getTranslation(translationVector);
		translationVector.negate().add(xPosition , yPosition , 0);
		translation.translate(translationVector);		
		
	}

	/**
	 * Sets the width only of this shape. Does not {@link #reset()} it.
	 * 
	 * @param width width of this shape
	 * @return {@code this}.
	 */
	public abstract Shape shapeWidth(int width);
	
	/**
	 * Sets the height only of this shape. Does not {@link #reset()} it.
	 * 
	 * @param height height of this shape
	 * @return {@code this}.
	 */
	public abstract Shape shapeHeight(int height);
	
	/**
	 * Returns the width of the shape.
	 * 
	 * @return Width of the shape.
	 */
	public abstract int shapeWidth();
	
	/**
	 * Returns the height of the shape.
	 * 
	 * @return Height of the shape.
	 */
	public abstract int shapeHeight();
	
	/**
	 * Returns the top Y coordinate in world space of this shape.
	 * 
	 * @return Top Y coordinate of this shape.
	 */
	public float topY() {
		
		return midY() + ((float)textureHeight / 2f);
		
	}

	/**
	 * Returns the bottom Y coordinate in world space of this shape.
	 * 
	 * @return Bottom Y coordinate of this shape.
	 */
	public float bottomY() {
		
		return midY() - ((float)textureHeight / 2f);
		
	}

	/**
	 * Returns the left X coordinate in world space of this shape.
	 * 
	 * @return Left X coordinate of this shape.
	 */
	public float leftX() {
		
		return midX() - ((float)textureWidth / 2f);
		
	}

	/**
	 * Returns the right X coordinate in world space of this shape.
	 * 
	 * @return Right X coordinate of this shape.
	 */
	public float rightX() {
		
		return midX() + ((float)textureWidth / 2f);
		
	}

	/**
	 * Returns the X midpoint coordinate in world space of this shape.
	 * 
	 * @return X midpoint coordinate of this shape.
	 */
	public float midX() {
		
		Vector3f translationVector = new Vector3f();
		translation.getTranslation(translationVector);
		return translationVector.x();
		
	}

	/**
	 * Returns the Y midpoint coordinate in world space of this shape.
	 * 
	 * @return Y midpoint coordinate of this shape.
	 */
	public float midY() {

		Vector3f translationVector = new Vector3f();
		translation.getTranslation(translationVector);
		return translationVector.y();
		
	}

	/**
	 * Returns the color of the border of this shape.
	 * 
	 * @return Border color of this shape.
	 */
	public ColorPixel borderColor() {
		
		return borderColor;
		
	}

	/**
	 * Sets the color of the border of this shape.
	 * 
	 * @param borderColor new border color 
	 */
	public void borderColor(ColorPixel borderColor) {
		
		formatColor(borderColor , this.borderColor , channels());
		
	}

	/**
	 * Returns the color of the fill of this shape.
	 * 
	 * @return Fill color of this shape.
	 */
	public ColorPixel fillColor() {
		
		return fillColor;
		
	}

	/**
	 * Sets the color of the fill of this shape.
	 * 
	 * @param fillColor new fill color 
	 */
	public void fillColor(ColorPixel fillColor) {
		
		formatColor(fillColor, this.fillColor, channels());
		
	}

	/**
	 * Returns whether this shape is hidden.
	 * 
	 * @return Whether this shape is hidden.
	 */
	public boolean hide() {
		
		return hide.get();
		
	}

	/**
	 * Sets whether this shape is hidden.
	 * 
	 * @param hide whether this shape is hidden
	 */
	public void hide(boolean hide) {
		
		this.hide.set(hide);
		
	}
	
	/**
	 * Toggles whether this shape is hidden.
	 */
	public void toggleHide() {
		
		hide.set(!hide.get());
		
	}
	
	/**
	 * Returns the number of channels for this shape. 
	 * 
	 * @return Number of channels of this shape.
	 */
	public int channels() {
		
		return channelsPerPixel;
		
	}

	/**
	 * Sets the reference to the hide value to {@code hide}. 
	 * 
	 * @param hide an atomic boolean to set this shape's hide value to
	 * @throws NullPointerException if {@code hide} is <code>null</code>.
	 */
	public void setHide(AtomicBoolean hide) {
		
		this.hide = Objects.requireNonNull(hide);
		
	}

	/**
	 * Returns the offset in world space this shape is from the horizontal midpoint of {@code artboard}. 
	 * 
	 * @param artboard an artboard to get the offset of this shape from
	 * @return Horizontal offset this shape is from {@code artboard}.
	 * @throws NullPointerException if {@code artboard} is <code>null</code>.
	 */
	public float xOffsetFrom(Artboard artboard) {
		
		return midX() - artboard.midX();
		
	}
	
	/**
	 * Returns the offset in world space this shape is from the vertical midpoint of {@code artboard}.
	 * 
	 * @param artboard an artboard to get the offset of this shape from
	 * @return Vertical offset this shape is from {@code artboard}.
	 * @throws NullPointerException if {@code artboard} is <code>null</code>.
	 */
	public float yOffsetFrom(Artboard artboard) {
		
		return midY() - artboard.midY();
		
	}
	
}
