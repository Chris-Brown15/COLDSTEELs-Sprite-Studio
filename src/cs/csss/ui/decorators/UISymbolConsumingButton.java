package cs.csss.ui.decorators;

import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSButton;
import cs.core.utils.ByteSupplier;
import cs.core.utils.Lambda;

public class UISymbolConsumingButton extends CSButton {

	public UISymbolConsumingButton(CSLayout owner , ByteSupplier symbol , Lambda onPress) {
	
		owner.super(symbol.getAsByte() , onPress);
	
		setCode(() -> {
			
			this.symbol = symbol.getAsByte();
			onPress.invoke();
			
		});
		
	}
	
}