package cs.csss.ui.utils;

import static cs.core.ui.CSUIConstants.*;
import static org.lwjgl.nuklear.Nuklear.nk_input_is_mouse_hovering_rect;
import static org.lwjgl.nuklear.Nuklear.nk_widget_bounds;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkTextWidthCallbackI;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;

/**
 * Utilities for UI elements.
 */
public final class UIUtils {

	private static NkTextWidthCallbackI fontWidthGetter;
	
	/**
	 * Used to set the callback this class invokes to get the width of a string.
	 * 
	 * @param getter — new text width getter
	 */
	public static void setFontWidthGetter(NkTextWidthCallbackI getter) {
		
		fontWidthGetter = getter;
		
	}
	
	/**
	 * Returns a byte pointer allocated on {@code stack} whose byte will be 1 if {@code state} is true, 0 otherwise.
	 * 
	 * @param stack — stack to allocate from
	 * @param state — a boolean
	 * @return A stack allocated byte pointer containing a byte representation of {@code state}.
	 */
	public static ByteBuffer toByte(MemoryStack stack , final boolean state) {
		
		return stack.bytes(state ? (byte)1 : (byte) 0);
		
	}
	
	/**
	 * Returns a stack allocated byte pointer whose byte will be 1 if {@code state} is true, 0 otherwise.
	 * 
	 * @param state — a boolean
	 * @return A stack allocated byte pointer containing a byte representation of {@code state}.
	 */
	public static ByteBuffer asByte(final boolean state) {
		
		return MemoryStack.stackGet().bytes(toByte(state));
		
	}
	
	/**
	 * Convertes a boolean to a byte
	 * 
	 * @param state — a boolean
	 * @return 1 if {@code state} is true, 0 otherwise.
	 */
	public static byte toByte(final boolean state) {
		
		return state ? (byte) 1 : 0;
		
	}

	/**
	 * Returns a symbol based on {@code state}.
	 * 
	 * @param state — boolean representing state
	 * @return Symbol based on whether {@code state} is {@code true} or {@code false}.
	 */
	public static int toMenuSymbol(final boolean state) {
		
		return state ? SYMBOL_TRIANGLE_DOWN : SYMBOL_TRIANGLE_RIGHT;
		
	}
	
	/**
	 * Helper for calculating the width needed to display a tooltip text.
	 * 
	 * @param widthCalc — the width calculating function the current {@code CSNuklear} is using
	 * @return Width of a tooltip text.
	 */
	public static int textLength(String tooltipString) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {

			stack.UTF8(tooltipString);
			long ptr = stack.getPointerAddress();
			
			return (int) Math.ceil(fontWidthGetter.invoke(NULL, 0, ptr , tooltipString.length())) + 10;
		
		}
		
	}
	
	/**
	 * Returns whether the cursor is hovering the current UI row.
	 * 
	 * @param context — Nuklear context
	 * @return {@code true} if the cursor is hovering the current UI row.
	 */
	public static boolean hovering(NkContext context) {

		try(MemoryStack stack = MemoryStack.stackPush()) {
	
			NkRect rect = NkRect.malloc(stack);
			nk_widget_bounds(context , rect);
			return nk_input_is_mouse_hovering_rect(context.input() , rect);
			
		}	
		
	}
	
	/**
	 * Sets the tooltip of any element to the cannonical tooltip show test and sets the string displayed to {@code tooltip}. 
	 *  
	 * @param element — an element whose tooltip is being initialized
	 * @param tooltip — text to display as the toolti of the element.
	 */
	public static void toolTip(CSElement element , String tooltip) {
		
		int width = textLength(tooltip);		
		element.initializeToolTip(HOVERING|MOUSE_PRESSED, MOUSE_RIGHT, 0, width);
		element.toolTip.new CSDynamicRow(20).new CSText(tooltip);
		
	}
	
	private UIUtils() {}

}
