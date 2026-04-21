package cs.csss.ui.elements;

import static org.lwjgl.nuklear.Nuklear.NK_RGB;
import static org.lwjgl.nuklear.Nuklear.NK_RGBA;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;

import org.lwjgl.nuklear.NkContext;

import sc.core.ui.SCElements.SCUI.SCLayout;
import sc.core.ui.SCElements.SCUI.SCLayout.SCColorPicker;

/**
 * Extension of {@link CSColorPicker} in which the internal color format of the picker can be selected.
 */
public class PublicFormatColorPicker extends SCColorPicker {

	/**
	 * Creates a new color picker.
	 * 
	 * @param csLayout owning layout
	 * @param format format of this picker, one of {@link cs.core.ui.CSUIConstants#RGB}, or {@link cs.core.ui.CSUIConstants#RGBA} 
	 */
	public PublicFormatColorPicker(NkContext context , SCLayout csLayout, int format) {

		csLayout.super(format);
		checkFormat(format);
		setCode(() -> nk_color_pick(context , color , this.format));
	}
	
	/**
	 * Returns the format of this picker
	 * 
	 * @return Current format of this picker.
	 */
	public int format() {
		
		return format;
		
	}
	
	private int checkFormat(int format) {

		if(format != NK_RGBA && format != NK_RGB) throw new IllegalArgumentException(String.format(
			"%d is invalid as a format, must be one of RGBA (%d) or RGB (%d)." , 
			format ,
			NK_RGBA ,
			NK_RGB
		));			
			
		return format;
		
	}
	
}
