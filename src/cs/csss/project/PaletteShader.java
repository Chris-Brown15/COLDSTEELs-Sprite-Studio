package cs.csss.project;

import static cs.csss.utils.FileUtils.readAllCharacters;

import sc.core.graphics.SCTexture;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Implementation of {@link CSSSShader} which facilitates rendering objects using a lookup texture and a palette texture.
 */
@RenderThreadOnly public class PaletteShader extends CSSSShader {

	int paletteTextureLocation;
	int imageTextureLocation; 
	int channelsLocation;     
	int paletteWidthLocation;
	int paletteHeightLocation;
	
	/**
	 * Initializes this shader by loading the shader source code files from disk.
	 */
	public void initialize() {
		
		String vertexSource = readAllCharacters("assets/shaders/vertexShader.glsl");
		String fragmentSource = readAllCharacters("assets/shaders/fragmentPaletteShader.glsl");
		
		super.initialize(vertexSource , fragmentSource);
		
		activate();
		
		paletteTextureLocation = getUniformLocation("paletteTexture");
		imageTextureLocation = getUniformLocation("imageTexture");    
		channelsLocation = getUniformLocation("channels");            
		paletteWidthLocation = getUniformLocation("paletteWidth");    
		paletteHeightLocation = getUniformLocation("paletteHeight");

		deactivate();
		
	}
	
	@Override public void updateTextures(ArtboardPalette palette , SCTexture texture) {

		palette.activate(0);
		texture.activate(1);

		activate();
		
		uploadInt(paletteTextureLocation , 0);
		uploadInt(imageTextureLocation , 1);
		
		uploadInt(paletteWidthLocation , palette.width());
		uploadInt(paletteHeightLocation , palette.height());
	
		uploadInt(channelsLocation , palette.channelsPerPixel());

		deactivate();
		
	}
	
}
