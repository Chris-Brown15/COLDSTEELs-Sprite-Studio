package cs.csss.utils;

import static cs.core.ui.CSUIConstants.SYMBOL_TRIANGLE_DOWN;
import static cs.core.ui.CSUIConstants.SYMBOL_TRIANGLE_RIGHT;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

import org.lwjgl.nuklear.NkTextWidthCallbackI;
import org.lwjgl.system.MemoryStack;

public final class UIUtils {

	public static ByteBuffer toByte(MemoryStack stack , final boolean state) {
		
		return stack.bytes(state ? (byte)1 : (byte) 0);
		
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
	public static int toolTipLength(final NkTextWidthCallbackI widthCalc , String tooltipString) {
		
		try(MemoryStack stack = MemoryStack.stackPush()) {

			stack.UTF8(tooltipString);
			long ptr = stack.getPointerAddress();
			
			return (int) Math.ceil(widthCalc.invoke(NULL, 0, ptr , tooltipString.length())) + 10;
		
		}
		
	}
	
	private UIUtils() {}

}
