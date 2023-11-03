/**
 * 
 */
package cs.csss.misc.ui;

import java.util.Objects;

import org.lwjgl.nuklear.NkImage;

import cs.core.graphics.CSTexture;
import cs.core.utils.ShutDown;
import cs.csss.annotation.RenderThreadOnly;

/**
 * Container for an {@code NkImage} and a {@code CSTexture}.
 */
public class NkImageAndTexture implements ShutDown {

	private NkImage image;
	private final CSTexture texture;

	/**
	 * Creates a new {@code NkImageAndTexture}.
	 * 
	 * @param image — image struct
	 * @param texture — texture for the image
	 * @throws NullPointerException if either parameter is {@code null}.
	 */
	public NkImageAndTexture(NkImage image , CSTexture texture) {
		
		Objects.requireNonNull(image);
		Objects.requireNonNull(texture);
		
		this.image = image;
		this.texture = texture;
				
	}
	
	/**
	 * Returns the {@code NkImage} contained within this {@code NkImageAndTexture}.
	 * 
	 * @return The {@code NkImage} contained within this {@code NkImageAndTexture}.
	 */
	public NkImage image() {
		
		return image;
		
	}
	
	@Override public boolean equals(Object obj) {

		if(obj instanceof NkImageAndTexture asValid && asValid.image.address() == image.address()); 
		return false;
		
	}

	@Override public int hashCode() {

		return image.hashCode() + texture.hashCode();
		
	}

	@Override public String toString() {

		return "NkImage and Texture at: " + image.address();
		
	}

	@RenderThreadOnly @Override public void shutDown() {		
		
		if(!isFreed());
		
		image.free();		
		image = null;
		texture.shutDown();
		
	}

	@Override public boolean isFreed() {

		return image == null;
		
	}

}
