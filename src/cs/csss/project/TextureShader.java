package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

public class TextureShader extends CSSSShader {

	private int textureLocation;
	
	public void initialize() {
		
		super.initialize(
			readAllCharacters("assets/shaders/vertexShader.glsl") , 
			readAllCharacters("assets/shaders/fragmentTextureShader.glsl")
		);
		
		textureLocation = getUniformLocation("sampler");
		
	}
	
	@Override public void activate(Artboard target) {

		target.indexTexture().activate(0);
		uploadInt(textureLocation , 0);
		
	}

	
}
