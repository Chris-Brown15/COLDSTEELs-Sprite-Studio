package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import cs.core.graphics.CSTexture;

public class TextureShader extends CSSSShader {

	private int textureLocation;
	
	public void initialize() {
		
		super.initialize(
			readAllCharacters("assets/shaders/vertexShader.glsl") , 
			readAllCharacters("assets/shaders/fragmentTextureShader.glsl")
		);
		
		textureLocation = getUniformLocation("sampler");
		
	}
	
	@Override public void updateTextures(ArtboardPalette palette , CSTexture texture) {

		texture.activate(0);
		uploadInt(textureLocation , 0);
		
	}

	
}
