/**
 * 
 */
package cs.csss.editor;

import static cs.core.graphics.StandardRendererConstants.POSITION_2D;
import static cs.core.graphics.StandardRendererConstants.STREAM_VAO;
import static cs.core.graphics.StandardRendererConstants.UINT;
import static cs.core.graphics.StandardRendererConstants.UV;

import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Matrix4f;

import cs.core.graphics.CSRender;
import cs.core.graphics.CSTexture;
import cs.core.graphics.CSVAO;
import cs.core.graphics.utils.VertexBufferBuilder;
import cs.core.utils.ShutDown;
import cs.csss.annotation.RenderThreadOnly;
import cs.csss.engine.CSSSCamera;
import cs.csss.project.Artboard;

/**
 * 
 */
public abstract class Rasterizable implements ShutDown {

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
	 * Texture for this shape.
	 */
	protected CSTexture texture;

	/**
	 * VAO for this shape.
	 */
	protected CSVAO vao;
	
	/**
	 * Render for this shape. Used only for resource management.
	 */
	protected CSRender render;

	/**
	 * Initializes {@link #vao} and {@link #position} from the given parameters.
	 * 
	 * @param midX x midpoint for this shape
	 * @param midY y midpoint for this shape
	 * @param width width of this shape
	 * @param height height of this shape
	 */
	protected void initializeRendererData(int midX , int midY , int width , int height) {

		vao = new CSVAO();
		VertexBufferBuilder builder = new VertexBufferBuilder(POSITION_2D|UV);		
		builder.size(width, height);

		vao.initialize(builder.attributes, STREAM_VAO, builder.get());
		vao.drawAsElements(6, UINT);
		
		translation.translate(midX, midY , 0);
		this.textureWidth = width;
		this.textureHeight = height;
		
	}

	/**
	 * Abstract way of defining the dimensions of this shape. If this shape is an ellipse, the parameters should represent the diameters of the 
	 * ellipse. Likewise if the shape is a rectangle, the parameters should represent the width and height of the shape.
	 * <p>
	 * 	This method does <em>not</em> {@link #reset() reset} the shape.
	 * </p>
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
	@RenderThreadOnly public abstract void render(CSSSCamera camera);

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

		render.shutDown();

	}

	@Override public boolean isFreed() {

		return render.isFreed();
		
	}

}
