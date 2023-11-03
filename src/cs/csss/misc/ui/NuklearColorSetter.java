/**
 * 
 */
package cs.csss.misc.ui;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;

import cs.core.ui.CSNuklear;
import cs.core.utils.ShutDown;

/**
 * 
 */
public class NuklearColorSetter implements ShutDown {

	private final NkContext nuklear;
	private NkColor colorBuffer = NkColor.malloc();
	
	/**
	 * Creates a new Nuklear color setter.
	 * 
	 * @param source — Nuklear instance whose style will be modified.
	 */
	public NuklearColorSetter(CSNuklear source) {

		this.nuklear = source.context();
				
	}

	public NuklearColorSetter buttonBorderColor(int color) {
		
		nuklear.style().button().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter buttonTextBackgroundColor(int color) {
		
		nuklear.style().button().text_background(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter buttonTextNormalColor(int color) {
		
		nuklear.style().button().text_normal(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter buttonTextHoverColor(int color) {
		
		nuklear.style().button().text_hover(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter buttonTextActiveColor(int color) {
		
		nuklear.style().button().text_active(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter chartColor(int color) {
		
		nuklear.style().chart().color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter chartBorderColor(int color) {
		
		nuklear.style().chart().border_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter chartSelectedColor(int color) {
		
		nuklear.style().chart().selected_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter checkBoxBorderColor(int color) {
		
		nuklear.style().checkbox().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter comboBorderColor(int color) {
		
		nuklear.style().combo().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter contextualButtonBorderColor(int color) {
		
		nuklear.style().contextual_button().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter menuButtonBorderColor(int color) {
		
		nuklear.style().menu_button().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter textEditorBorderColor(int color) {
		
		nuklear.style().edit().border_color(colorFromInt(color));		
		return this;
		
	}
	
	public NuklearColorSetter optionBorderColor(int color) {
		
		nuklear.style().option().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter progressBorderColor(int color) {
		
		nuklear.style().progress().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter progressCursorBorderColor(int color) {
		
		nuklear.style().progress().cursor_border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter propertyBorderColor(int color) {
		
		nuklear.style().property().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter scrollHorizontalBorderColor(int color) {
		
		nuklear.style().scrollh().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter scrollHorizontalCursorBorderColor(int color) {
		
		nuklear.style().scrollh().cursor_border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter scrollVerticalBorderColor(int color) {
		
		nuklear.style().scrollv().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter scrollVerticalCursorBorderColor(int color) {
		
		nuklear.style().scrollv().cursor_border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter sliderBorderColor(int color) {
		
		nuklear.style().slider().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter tabBorderColor(int color) {
		
		nuklear.style().tab().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter textColor(int color) {
		
		nuklear.style().text().color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter windowBorderColor(int color) {
		
		nuklear.style().window().border_color(colorFromInt(color));		
		return this;
		
	}

	public NuklearColorSetter windowComboBorderColor(int color) {
		
		nuklear.style().window().combo_border_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter windowContextualBorderColor(int color) {
		
		nuklear.style().window().contextual_border_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter windowGroupBorderColor(int color) {
		
		nuklear.style().window().group_border_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter windowPopupBorderColor(int color) {
		
		nuklear.style().window().popup_border_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter windowTooltipBorderColor(int color) {
		
		nuklear.style().window().tooltip_border_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter windowMenuBorderColor(int color) {
		
		nuklear.style().window().menu_border_color(colorFromInt(color));
		return this;
		
	}

	public NuklearColorSetter windowBackgroundColor(int color) {
		
		nuklear.style().window().background(colorFromInt(color));		
		return this;
		
	}
	
	{
//		var item = nuklear.style().button().;
	}
	
	private NkColor colorFromInt(int color) {
		
		colorBuffer.set((byte)(color >> 24), (byte)(color >> 16), (byte)(color >> 8), (byte)color);
		return colorBuffer;
		
	}

	@Override public void shutDown() {

		if(isFreed()) return;
		colorBuffer.free();
		colorBuffer = null;
		
	}

	@Override public boolean isFreed() {

		return colorBuffer == null;
		
	}
	
}
