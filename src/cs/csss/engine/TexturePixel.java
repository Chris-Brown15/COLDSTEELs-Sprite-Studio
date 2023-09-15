package cs.csss.engine;

/**
 * Provides methods for getting texture x and y indices.
 */
public interface TexturePixel {

	/**
	 * Returns a lookup x index.
	 * 
	 * @return X lookup index.
	 */
	int textureX();
	
	/**
	 * Returns a lookup y index.
	 * 
	 * @return Y lookup index.
	 */
	int textureY();
	
}
