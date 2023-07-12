package cs.csss.utils;

import static cs.core.ui.CSUIConstants.SYMBOL_TRIANGLE_DOWN;
import static cs.core.ui.CSUIConstants.SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_input_is_mouse_hovering_rect;
import static org.lwjgl.nuklear.Nuklear.nk_widget_bounds;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkTextWidthCallbackI;
import org.lwjgl.system.MemoryStack;

import cs.core.ui.CSNuklear;

public final class UIUtils {

	public static ByteBuffer toByte(MemoryStack stack , final boolean state) {
		
		return stack.bytes(state ? (byte)1 : (byte) 0);
		
	}
	
	public static ByteBuffer asByte(final boolean state) {
		
		return MemoryStack.stackGet().bytes(toByte(state));
		
	}
	
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
	public static int textLength(final NkTextWidthCallbackI widthCalc , String tooltipString) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {

			stack.UTF8(tooltipString);
			long ptr = stack.getPointerAddress();
			
			return (int) Math.ceil(widthCalc.invoke(NULL, 0, ptr , tooltipString.length())) + 10;
		
		}
		
	}

	/**
	 * Helper for calculating the width needed to display a tooltip text.
	 * 
	 * @param context — the nuklear context
	 * @return Width of a tooltip text.
	 */
	public static int textLength(final NkContext context , String tooltipString) {
		
		return textLength(context.style().font().width(), tooltipString);
		
	}

	/**
	 * Helper for calculating the width needed to display a tooltip text.
	 * 
	 * @param nuklear — the {@code CSNuklear} instance
	 * @return Width of a tooltip text.
	 */
	public static int textLength(final CSNuklear nuklear , String tooltipString) {
		
		return textLength(nuklear.font().width(), tooltipString);
		
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
	
	private UIUtils() {}

}
