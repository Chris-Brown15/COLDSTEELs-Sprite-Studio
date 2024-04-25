package cs.csss.project;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import cs.core.graphics.CSTexture;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Impelmentation of {@link CSSSShader} used to render textures directly. This shader does not use the lookup and palette system, it renders
 * textures as though they are color textures. 
 */
@RenderThreadOnly public class TextureShader extends CSSSShader {

	private int textureLocation , channelsLocation;
	
	
	/**
	 * Initializes this shader by loading its source code from disk.
	 */
	public void initialize() {
		
		super.initialize(
			readAllCharacters("assets/shaders/vertexShader.glsl") , 
			readAllCharacters("assets/shaders/fragmentTextureShader.glsl")
		);
		
		textureLocation = getUniformLocation("sampler");
		channelsLocation = getUniformLocation("channels");
		channels(4);
				
	}
	
	/**
	 * Sets the number of channels per pixel the texture this shader is intended to render is. This must be called <em>after</em> 
	 * {@link #updateTextures(ArtboardPalette, CSTexture)}.
	 * 
	 * @param channels number of channels per pixel.
	 */
	@RenderThreadOnly public void channels(int channels) {
		
		uploadInt(channelsLocation, channels);
		
	}
	
	@RenderThreadOnly @Override public void updateTextures(ArtboardPalette palette , CSTexture texture) {

		texture.activate(0);
		uploadInt(textureLocation , 0);
		channels(4);		
		
	}

	
}
