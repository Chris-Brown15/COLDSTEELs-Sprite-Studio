package cs.csss.project;

import static org.lwjgl.opengl.GL30C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL30C.GL_COMPILE_STATUS;

import org.joml.Matrix4f;

import cs.csss.annotation.RenderThreadOnly;
import sc.core.SCDebugging;
import sc.core.graphics.SCGLSL;
import sc.core.graphics.SCTexture;

/**
 * Base class for different types of shaders used in Sprite Studio. This shader is also reloadable, so its source code can be changed and 
 * reloaded to see the differences.
 */
@RenderThreadOnly public abstract class CSSSShader extends SCGLSL {

	protected static final Matrix4f IDENTITY = new Matrix4f().identity();
	
	protected int  
		projectionLocation ,
		viewLocation  ,
		translationLocation;
	
	@Override public void initialize(String vertexSource , String fragmentSource) {
		
		super.initialize(vertexSource , fragmentSource);

		SCDebugging.say("Checking shader initialization errors:");
		checkInitializationErrors(vertexShaderID, GL_COMPILE_STATUS);
		checkProgramErrors(GL_LINK_STATUS);		
		
		activate();

		projectionLocation = getUniformLocation("projection");
		viewLocation = getUniformLocation("view");
		translationLocation = getUniformLocation("translation");
		
		deactivate();
		
	}

	/**
	 * Sets the camera and translation variables for a render pass, which here means a render of all artboards.
	 * 
	 * @param projection the projection matrix of the camera
	 * @param view the view matrix of the camera
	 * @param translation translation matrix to apply to the camera
	 */
	public void updatePassVariables(Matrix4f projection , Matrix4f view , Matrix4f translation) {

		activate();
		
		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , translation);

		deactivate();
		
	}
	
	/**
	 * Sets the camera variables for a render pass, which here means a render of all artboards.
	 * 
	 * @param projection the projection matrix of the camera
	 * @param view the view matrix of the camera
	 */
	public void updatePassVariables(Matrix4f projection , Matrix4f view) {

		activate();
		
		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , IDENTITY);

		deactivate();
		
	}
	
	/**
	 * Updates the shader's view of the textures used for rendering.
	 * 
	 * @param palette the palette texture
	 * @param texture the artboard texture
	 */
	public abstract void updateTextures(ArtboardPalette palette, SCTexture texture);
	
}
