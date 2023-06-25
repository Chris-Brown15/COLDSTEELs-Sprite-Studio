package cs.csss.artboard;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import cs.core.graphics.CSOrthographicCamera;
import cs.core.graphics.utils.ReloadableShader;

public class ArtboardShader extends ReloadableShader {

	private int  
		projectionLocation ,
		viewLocation  ,
		paletteTextureLocation ,
		imageTextureLocation ,
		channelsLocation ,
		grayscaleShadeLocation
	;

	void initialize() {
		
		super.initialize(
			vertexSource = readAllCharacters("assets/shaders/vertexPaletteShader.glsl") , 
			fragmentSource = readAllCharacters("assets/shaders/fragmentPaletteShader.glsl")
		);

		projectionLocation = getUniformLocation("projection");
		viewLocation = getUniformLocation("view");
		paletteTextureLocation = getUniformLocation("paletteTexture");
		imageTextureLocation = getUniformLocation("imageTexture");
		channelsLocation = getUniformLocation("channels");
		grayscaleShadeLocation = getUniformLocation("grayscaleShade");
		uploadInt(grayscaleShadeLocation , 0xffff00ff);
		
	}
	
	public void updatePassVariables(CSOrthographicCamera camera , int channelsPerPixel , int grayscaleShade ) {

		uploadMatrix(projectionLocation, false , camera.projection());
		uploadMatrix(viewLocation , false , camera.viewTranslation());
		uploadInt(grayscaleShadeLocation , grayscaleShade);
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
