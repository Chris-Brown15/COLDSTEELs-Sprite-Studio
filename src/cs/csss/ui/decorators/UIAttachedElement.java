package cs.csss.ui.decorators;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;
import cs.core.utils.Lambda;

public class UIAttachedElement extends CSElement {

	public UIAttachedElement(CSLayout layout , Lambda code) {

		layout.super(layout);
		
		setCode(code);

	}

	@Override protected void onResize() {


	}

}
