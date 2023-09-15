package cs.csss.project;

import org.joml.Matrix4f;

import cs.core.graphics.CSTexture;
import cs.core.graphics.utils.ReloadableShader;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Base class for different types of shaders used in Sprite Studio. This shader is also reloadable, so its source code can be changed and 
 * reloaded to see the differences.
 */
@RenderThreadOnly public abstract class CSSSShader extends ReloadableShader {

	protected static final Matrix4f IDENTITY = new Matrix4f().identity();
	
	protected int  
		projectionLocation ,
		viewLocation  ,
		translationLocation;
	
	@Override public void initialize(String vertexSource , String fragmentSource) {
		
		users.getAndIncrement();
		
		super.initialize(vertexSource , fragmentSource);

		projectionLocation = getUniformLocation("projection");
		viewLocation = getUniformLocation("view");
		translationLocation = getUniformLocation("translation");
		
	}

	/**
	 * Sets the camera and translation variables for a render pass, which here means a render of all artboards.
	 * 
	 * @param projection — the projection matrix of the camera
	 * @param view — the view matrix of the camera
	 * @param translation — translation matrix to apply to the camera
	 */
	public void updatePassVariables(Matrix4f projection , Matrix4f view , Matrix4f translation) {

		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , translation);
		
	}
	
	/**
	 * Sets the camera variables for a render pass, which here means a render of all artboards.
	 * 
	 * @param projection — the projection matrix of the camera
	 * @param view — the view matrix of the camera
	 */
	public void updatePassVariables(Matrix4f projection , Matrix4f view) {

		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , IDENTITY);
		
	}
	
	/**
	 * Updates the shader's view of the textures used for rendering.
	 * 
	 * @param palette — the palette texture
	 * @param texture — the artboard texture
	 */
	public abstract void updateTextures(ArtboardPalette palette, CSTexture texture);
	
	/**
	 * Returns the source code of the vertex shader.
	 * 
	 * @return Source code of the vertex shader.
	 */
	public String vertexSource() {
		
		return vertexSource;
		
	}

	/**
	 * Returns the source code of the fragment shader.
	 * 
	 * @return Source code of the fragment shader.
	 */
	public String fragmentSource() {
		
		return fragmentSource;
		
	}
	
}
