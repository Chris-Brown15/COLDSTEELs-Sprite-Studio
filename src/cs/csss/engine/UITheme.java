/**
 * 
 */
package cs.csss.engine;

import java.util.LinkedList;
import java.util.Objects;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkStyle;

import cs.core.utils.ShutDown;

/**
 * Contains some basic functionality for generating UI styles. Ultimately, this class contains a specific {@code NkStyle} struct which will be set
 * once a theme is activated.
 */
public class UITheme implements ShutDown {

	private NkStyle style;
	private LinkedList<NkColor> palette;
	private NkColor windowColor;
	private String name;
	
	/**
	 * Creates a new theme.
	 * 
	 * @param name — name of this theme
	 * @param style — style for this theme
	 * @param palette — list of colors for this theme
	 * @param windowColor — color of the background window for this theme
	 */
	public UITheme(String name , NkStyle style , LinkedList<NkColor> palette , NkColor windowColor) {

		this.style = style;		
		this.palette = Objects.requireNonNull(palette);
		this.windowColor = Objects.requireNonNull(windowColor);
		this.name = name;
		
	}	
	
	/**
	 * Returns the name of this theme, which may or may not be <code>null</code> depending upon the source of this {@code UITheme} instance.
	 * 
	 * @return Name of this theme, or <code>null</code>.
	 */
	public String name() {
		
		return name;
		
	}
	
	/**
	 * Returns the {@link NkStyle} associated with this theme.
	 * 
	 * @return Style associated with this theme.
	 */
	public NkStyle style() {
		
		return style;
		
	}
	
	/**
	 * Returns the palette of this UI theme.
	 * 
	 * @return Palette of this theme.
	 */
	public LinkedList<NkColor> palette() {
		
		return palette;
		
	}
	
	/**
	 * Returns the window color of this palette.
	 * 
	 * @return Window color of this palette.
	 */
	public NkColor windowColor() {
		
		return windowColor;
		
	}
	
	@Override public void shutDown() {

		if(isFreed()) return;
		if(style != null) style.free();		
		palette.forEach(NkColor::free);
		windowColor.free();
		windowColor = null;
	}

	@Override public boolean isFreed() {

		return windowColor == null;
		
	}

}
