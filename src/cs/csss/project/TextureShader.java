package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import cs.core.graphics.CSTexture;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Impelmentation of {@link CSSSShader} used to render textures directly. This shader does not use the lookup and palette system, it renders
 * textures as though they are color textures. 
 */
@RenderThreadOnly public class TextureShader extends CSSSShader {

	private int textureLocation;
	
	/**
	 * Initializes this shader by loading its source code from disk.
	 */
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
