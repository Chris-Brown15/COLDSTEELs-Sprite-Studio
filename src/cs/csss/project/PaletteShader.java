package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import cs.core.graphics.CSTexture;

public class PaletteShader extends CSSSShader {

	int
		paletteTextureLocation , 
		imageTextureLocation , 
		channelsLocation ,     
		paletteWidthLocation , 
		paletteHeightLocation;
	
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
	
	public String vertexSource() {
		
		return vertexSource;
		
	}
	
	public String fragmentSource() {
		
		return fragmentSource;
		
	}
	
}
