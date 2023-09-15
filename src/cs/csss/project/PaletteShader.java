package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import cs.core.graphics.CSTexture;
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
		
		super.initialize(
			readAllCharacters("assets/shaders/vertexShader.glsl") , 
			readAllCharacters("assets/shaders/fragmentPaletteShader.glsl")
		);
		
		paletteTextureLocation = getUniformLocation("paletteTexture");
		imageTextureLocation = getUniformLocation("imageTexture");    
		channelsLocation = getUniformLocation("channels");            
		paletteWidthLocation = getUniformLocation("paletteWidth");    
		paletteHeightLocation = getUniformLocation("paletteHeight");
		
	}
	
	@Override public void updateTextures(ArtboardPalette palette , CSTexture texture) {

		palette.activate(0);
		texture.activate(1);
		
		uploadInt(paletteTextureLocation , 0);
		uploadInt(imageTextureLocation , 1);
		
		uploadInt(paletteWidthLocation , palette.width());
		uploadInt(paletteHeightLocation , palette.height());
	
		uploadInt(channelsLocation , palette.channelsPerPixel());
		
	}
	
	@Override public String vertexSource() {
		
		return vertexSource;
		
	}
	
	@Override public String fragmentSource() {
		
		return fragmentSource;
		
	}
	
}
