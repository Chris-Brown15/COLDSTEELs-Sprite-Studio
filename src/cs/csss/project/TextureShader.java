package cs.csss.project;

import static cs.csss.utils.FileUtils.readAllCharacters;

import cs.csss.annotation.RenderThreadOnly;
import sc.core.graphics.SCTexture;

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

		activate();
		
		textureLocation = getUniformLocation("sampler");
		channelsLocation = getUniformLocation("channels");
		channels(4);

		deactivate();
			
	}
	
	/**
	 * Sets the number of channels per pixel the texture this shader is intended to render is. This must be called <em>after</em> 
	 * {@link #updateTextures(ArtboardPalette, CSTexture)}.
	 * 
	 * @param channels number of channels per pixel.
	 */
	@RenderThreadOnly public void channels(int channels) {

		activate();
		
		uploadInt(channelsLocation, channels);

		deactivate();
		
	}
	
	@RenderThreadOnly @Override public void updateTextures(ArtboardPalette palette , SCTexture texture) {

		texture.activate(0);
		activate();
		
		uploadInt(textureLocation , 0);
		channels(4);		
		
		deactivate();
		
	}

	
}
