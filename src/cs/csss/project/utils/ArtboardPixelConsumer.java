package cs.csss.project.utils;

/**
 * SAM interface for invoking upon individual pixels of an artboard.
 */
public interface ArtboardPixelConsumer {

	/**
	 * Receives indices of a pixel on some artboard.
	 * 
	 * @param artboardTextureX — x index of the pixel on the artboard's texture
	 * @param artboardTextureY — y index of the pixel on the artboard's texture
	 */
	void invoke(int artboardTextureX , int artboardTextureY);
	
}
