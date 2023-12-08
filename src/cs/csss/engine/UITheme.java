/**
 * 
 */
package cs.csss.engine;

import org.lwjgl.nuklear.NkStyle;

import cs.core.utils.ShutDown;

/**
 * Contains some basic functionality for generating UI styles. Ultimately, this class contains a specific {@code NkStyle} struct which will be set
 * once a theme is activated.
 */
class UITheme implements ShutDown {

	private NkStyle style;
	
	/**
	 * Creates a new UI theme.
	 */
	public UITheme() {

		style = NkStyle.malloc();
		
	}
	
	/**
	 * Creates a copy of the given style into this one.
	 * 
	 * @param source — a source style to copy from
	 */
	public UITheme(NkStyle source) {
		
		style = NkStyle.malloc().set(source);
		
	}

	
	
	@Override public void shutDown() {

		if(isFreed()) return;
		style.free();
		style = null;	
		
	}

	@Override public boolean isFreed() {

		return style == null;
		
	}

}
