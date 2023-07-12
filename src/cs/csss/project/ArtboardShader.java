package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import org.joml.Matrix4f;

import cs.core.graphics.utils.ReloadableShader;

public class ArtboardShader extends ReloadableShader {

	private static final Matrix4f IDENTITY = new Matrix4f().identity();
	
	private int  
		projectionLocation ,
		viewLocation  ,
		translationLocation ,
		paletteTextureLocation ,
		imageTextureLocation ,
		channelsLocation 
	;

	void initialize() {
		
		users.getAndIncrement();
		
		super.initialize(
			vertexSource = readAllCharacters("assets/shaders/vertexPaletteShader.glsl") , 
			fragmentSource = readAllCharacters("assets/shaders/fragmentPaletteShader.glsl")
		);

		projectionLocation = getUniformLocation("projection");
		viewLocation = getUniformLocation("view");
		translationLocation = getUniformLocation("translation");
		paletteTextureLocation = getUniformLocation("paletteTexture");
		imageTextureLocation = getUniformLocation("imageTexture");
		channelsLocation = getUniformLocation("channels");
		
	}
	
	public void updatePassVariables(Matrix4f projection , Matrix4f view , Matrix4f translation , int channelsPerPixel) {

		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , translation);
		uploadInt(channelsLocation , channelsPerPixel);				
		
	}
	
	public void updatePassVariables(Matrix4f projection , Matrix4f view , int channelsPerPixel) {

		uploadMatrix(projectionLocation, false , projection);
		uploadMatrix(viewLocation , false , view);		
		uploadMatrix(translationLocation , false , IDENTITY);
		uploadInt(channelsLocation , channelsPerPixel);				
		
	}
	
	public void activate(Artboard artboard) {

		artboard.activeLayersPalette().activate(0);
		artboard.indexTexture().activate(1);
		uploadInt(paletteTextureLocation , 0);
		uploadInt(imageTextureLocation , 1);
		
	}

	public String vertexSource() {
		
		return vertexSource;
		
	}
	
	public String fragmentSource() {
		
		return fragmentSource;
		
	}
	
}
