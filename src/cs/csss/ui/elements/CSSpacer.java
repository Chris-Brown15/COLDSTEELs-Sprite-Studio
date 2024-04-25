package cs.csss.ui.elements;

import static org.lwjgl.nuklear.Nuklear.nk_spacer;

import org.lwjgl.nuklear.NkContext;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;

/**
 * Class used to take up space in a {@link CSLayout} without displaying any element.
 */
public class CSSpacer extends CSElement {

	/**
	 * Creates a new spacer. 
	 * 
	 * @param layout owning layout
	 * @param context the nuklear context
	 */
	public CSSpacer(CSLayout layout , NkContext context) {
		
		layout.super();
		setCode(() -> nk_spacer(context));
		
	}

	@Override protected void onResize() {}

}
