package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

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
	
	@Override public void activate(Artboard artboard) {

		ArtboardPalette palette = artboard.activeLayersPalette();
		
		palette.activate(0);
		artboard.indexTexture().activate(1);
		
		uploadInt(paletteTextureLocation , 0);
		uploadInt(imageTextureLocation , 1);
		
		uploadInt(paletteWidthLocation , palette.width());
		uploadInt(paletteHeightLocation , palette.height());
	
		uploadInt(channelsLocation , artboard.activeLayerChannelsPerPixel());
		
	}
	
	public String vertexSource() {
		
		return vertexSource;
		
	}
	
	public String fragmentSource() {
		
		return fragmentSource;
		
	}
	
}
