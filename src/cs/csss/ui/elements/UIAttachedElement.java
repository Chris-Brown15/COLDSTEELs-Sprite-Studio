package cs.csss.ui.elements;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;
import cs.core.utils.Lambda;

/**
 * UI element which executes arbitrary code when the UI gets to it.
 */
public class UIAttachedElement extends CSElement {

	/**
	 * Creates an attached element.
	 * 
	 * @param layout — a layout who owns this element
	 * @param code — code to invoke
	 */
	public UIAttachedElement(CSLayout layout , Lambda code) {

		layout.super(layout);
		
		setCode(code);

	}

	@Override protected void onResize() {


	}

}
