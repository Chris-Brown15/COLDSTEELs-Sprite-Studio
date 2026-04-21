/**
 * Copyright 2025, All Rights Reserved.
 * ————————————————————————————————————
 * This file and any accompanying files
 * belong to STEEL Softworks, LLC. Do 
 * not distribute these files without 
 * permission from Chris Brown, owner 
 * of STEEL Softworks, at 
 * chris@steelsoftworks.net
 * ————————————————————————————————————
 */
package cs.csss.editor;

import static sc.core.graphics.SCRendererConstants.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Matrix4f;

import cs.csss.annotation.RenderThreadOnly;
import cs.csss.project.Artboard;
import sc.core.SCShutDown;
import sc.core.graphics.SCOrthographicCamera;
import sc.core.graphics.SCTexture;
import sc.core.graphics.SCVAO;
import sc.core.graphics.utils.SCVertexBufferBuilder;

/**
 * 
 */
public abstract class Rasterizable implements SCShutDown {

	/**
	 * When true, this shape should not be rendered.
	 */
	protected AtomicBoolean hide = new AtomicBoolean(false);

	/**
	 * World space transform and width and height of this shape.
	 */
	protected Matrix4f translation = new Matrix4f();
	
	/**
	 * Width and height of this rasterizable's texture.
	 */
	protected int textureWidth , textureHeight;	 
	
	/**
	 * Texture for this rasterizable.
	 */
	protected SCTexture texture;

	/**
	 * VAO for this rasterizable.
	 */
	protected SCVAO vao;
	
	/**
	 * Initializes {@link #vao} and {@link #position} from the given parameters.
	 * 
	 * @param midX x midpoint for this shape
	 * @param midY y midpoint for this shape
	 * @param width width of this shape
	 * @param height height of this shape
	 */
	protected void initializeRendererData(int midX , int midY , int width , int height) {

		vao = new SCVAO();
		SCVertexBufferBuilder builder = new SCVertexBufferBuilder(POSITION_2D|UV);		
		builder.dimensions(width, height);

		vao.initialize(builder.attributes, STREAM_DRAW, builder.get());
		vao.drawAsElements(6, UINT);
		
		translation.translate(midX, midY , 0);
		this.textureWidth = width;
		this.textureHeight = height;
		
	}

	/**
	 * Abstract way of defining the dimensions of this shape. If this shape is an ellipse, the parameters should represent the diameters
	 * of the ellipse. Likewise if the shape is a rectangle, the parameters should represent the width and height of the shape.
	 * 
	 * @param x x axis dimension
	 * @param y y axis dimension
	 */
	public abstract void dimensions(int x , int y);

	/**
	 * Rasterizes this shape, storing the result in the given artboard.
	 * 
	 * @param target artboard to store the resulting rasterize in.
	 */
	public abstract void rasterize(Artboard target);
	
	/**
	 * Renders this rasterizable object with the given camera.
	 * 
	 * @param camera the camera to render with
	 */
	@RenderThreadOnly public abstract void render(SCOrthographicCamera camera);

	/**
	 * Returns the width of the texture. 
	 * 
	 * @return Width of the texture.
	 */
	public int textureWidth() {
		
		return textureWidth;
		
	}

	/**
	 * Returns the height of the texture. 
	 * 
	 * @return Height of the texture.
	 */
	public int textureHeight() {
		
		return textureHeight;
		
	}

	/**
	 * Sets the width of the texture, but does not change it.  
	 * 
	 * @param width the new width of the texture
	 * @throws IllegalArgumentException if {@code width} is not positive.
	 */
	public void textureWidth(int width) {
		
		if(width <= 0) throw new IllegalArgumentException("Width is not positive: " + width);
		this.textureWidth = width;
		
	}

	/**
	 * Sets the height of the texture, but does not change it.  
	 * 
	 * @param height the new height of the texture
	 * @throws IllegalArgumentException if {@code height} is not positive.
	 */
	public void textureHeight(int height) {

		if(height <= 0) throw new IllegalArgumentException("Height is not positive: " + height);
		this.textureHeight = height;
		
	}

	@RenderThreadOnly @Override public void shutDown() {

		if(isFreed()) return;
		
		texture.shutDown();
		vao.shutDown();

	}

	@Override public boolean isFreed() {

		return texture.isFreed();
		
	}

}
