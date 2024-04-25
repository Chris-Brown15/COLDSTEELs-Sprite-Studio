package cs.csss.ui.elements;

import static cs.core.ui.CSUIConstants.RGB;
import static cs.core.ui.CSUIConstants.RGBA;
import static org.lwjgl.nuklear.Nuklear.nk_color_pick;

import org.lwjgl.nuklear.NkContext;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSColorPicker;

/**
 * Extension of {@link CSColorPicker} in which the internal color format of the picker can be selected.
 */
public class PublicFormatColorPicker extends CSColorPicker {

	/**
	 * Creates a new color picker.
	 * 
	 * @param csLayout owning layout
	 * @param format format of this picker, one of {@link cs.core.ui.CSUIConstants#RGB}, or {@link cs.core.ui.CSUIConstants#RGBA} 
	 */
	public PublicFormatColorPicker(NkContext context , CSLayout csLayout, int format) {

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

	/**
	 * Sets the format of this picker.
	 * 
	 * @param format new format of this picker, one of {@link cs.core.ui.CSUIConstants#RGB}, or {@link cs.core.ui.CSUIConstants#RGBA}
	 * @throws IllegalArgumentException if {@code format} is invalid.
	 */
	public void format(int format) {
		
		this.format = checkFormat(format);
		
	}
	
	private int checkFormat(int format) {

		if(format != RGBA && format != RGB) throw new IllegalArgumentException(String.format(
			"%d is invalid as a format, must be one of RGBA (%d) or RGB (%d)." , 
			format ,
			RGBA ,
			RGB
		));			
			
		return format;
		
	}
	
}
