/**
 * 
 */
package cs.csss.misc.ui;

import static cs.core.graphics.StandardRendererConstants.*;

import org.lwjgl.nuklear.NkCursor;
import org.lwjgl.nuklear.NkImage;

import cs.core.graphics.CSTexture;
import cs.core.ui.CSNuklear;
import cs.core.utils.files.CSGraphic;

/**
 * 
 */
public class NuklearStyler {

	private final CSNuklear source;
	
	/**
	 * 
	 */
	public NuklearStyler(CSNuklear source) {

		this.source = source;
		
	}

	public NkCursorAndNkImageAndTexture cursor(CSGraphic cursorGraphic) {
		
		NkCursor created = NkCursor.malloc(); 
		NkImageAndTexture cursorImage = image(cursorGraphic);
		created.img(cursorImage.image());
		
		return new NkCursorAndNkImageAndTexture(created, cursorImage);
				
	}
	
	public NkCursorAndNkImageAndTexture setCursor(CSGraphic cursorGraphic) {
		
		var cursor = cursor(cursorGraphic);
		source.context().style().cursor_active(cursor.cursor());
		return cursor;
		
	}
	
	public NkImageAndTexture image(CSGraphic graphic) {
		
		CSTexture texture = new CSTexture(graphic , MIN_FILTER_LINEAR|MAG_FILTER_LINEAR|S_WRAP_REPEAT|T_WRAP_REPEAT);
		NkImage nuklearImage = NkImage.malloc();
		NkImageAndTexture combined = new NkImageAndTexture(nuklearImage, texture);
		nuklearImage.handle().id(texture.textureID());
		return combined;
		
	}
	
}
