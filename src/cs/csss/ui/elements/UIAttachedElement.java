package cs.csss.ui.elements;

import sc.core.ui.SCElements.SCUI.SCLayout;
import sc.core.ui.SCElements.SCUI.SCLayout.SCElement;

/**
 * UI element which executes arbitrary code when the UI gets to it.
 */
public class UIAttachedElement extends SCElement {

	/**
	 * Creates an attached element.
	 * 
	 * @param layout a layout who owns this element
	 * @param code code to invoke
	 */
	public UIAttachedElement(SCLayout layout , Runnable code) {

		layout.super(layout);
		
		setCode(code);

	}

	@Override protected void onResize() {


	}

}
