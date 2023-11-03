/**
 * 
 */
package cs.csss.misc.ui;

import java.util.Objects;

import org.lwjgl.nuklear.NkCursor;

import cs.core.utils.ShutDown;

/**
 * Container for a cursor and its texture object.
 */
public class NkCursorAndNkImageAndTexture implements ShutDown {

	private final NkCursor cursor;
	private final NkImageAndTexture texture;
	
	NkCursorAndNkImageAndTexture(NkCursor cursor , NkImageAndTexture texture) {
		
		Objects.requireNonNull(cursor);
		Objects.requireNonNull(texture);
		
		this.cursor = cursor;
		this.texture = texture;
		
	}
	
	public NkCursor cursor() {
		
		return cursor;
		
	}
	
	@Override public boolean equals(Object obj) {

		if(obj instanceof NkCursorAndNkImageAndTexture valid && valid.cursor.address() == cursor.address()) return true; 
		return false;
		
	}

	@Override public int hashCode() {

		return cursor.hashCode() + texture.hashCode();

	}

	@Override public String toString() {

		return "NkCursorAndNkImageAndTexture at " + cursor.address();
		
	}

	@Override public void shutDown() {

		if(isFreed()) return;
		
		cursor.free();
		texture.shutDown();
				
	}

	@Override public boolean isFreed() {

		return texture.isFreed();
		
	}

}
