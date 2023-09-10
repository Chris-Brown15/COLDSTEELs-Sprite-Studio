package cs.csss.project;

import org.joml.Matrix4f;

import cs.core.graphics.CSTexture;
import cs.core.graphics.utils.ReloadableShader;

public abstract class CSSSShader extends ReloadableShader {

	protected static final Matrix4f IDENTITY = new Matrix4f().identity();
	
	protected int  
		projectionLocation ,
		viewLocation  ,
		translationLocation;
	
	public void initialize(String vertexSource , String fragmentSource) {
		
		users.getAndIncrement();
		
		super.initialize(vertexSource , fragmentSource);

		projectionLocation = getUniformLocation("projection");
		viewLocation = getUniformLocation("view");
		translationLocation = getUniformLocation("translation");
		
	}

	public void updatePassVariables(Matrix4f projection , Matrix4f view , Matrix4f translation) {

		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , translation);
		
	}
	
	public void updatePassVariables(Matrix4f projection , Matrix4f view) {

		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , IDENTITY);
		
	}
	
	public abstract void updateTextures(ArtboardPalette palette, CSTexture texture);
	
	public String vertexSource() {
		
		return vertexSource;
		
	}
	
	public String fragmentSource() {
		
		return fragmentSource;
		
	}
	
}
