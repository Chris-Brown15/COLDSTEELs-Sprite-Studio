package cs.csss.ui.elements;

import cs.core.graphics.CSTexture;
import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSSelectableText;
import cs.core.utils.Lambda;

public class SymbolAccessSelectableText extends CSSelectableText {

	byte symbolWhenTrue = -1 , symbolWhenFalse = -1;
	
	
	public SymbolAccessSelectableText(CSLayout layout , String string, boolean initialState, Lambda onPress) {
		
		layout.super(string, initialState, onPress);
		
	}

	public SymbolAccessSelectableText(CSLayout layout , CSTexture image, boolean initialState, String text, Lambda onPress) {
		
		layout.super(image, initialState, text, onPress);
		
	}

	public SymbolAccessSelectableText(CSLayout layout , byte symbol, boolean initialState, String text, Lambda onPress) {
		
		layout.super(symbol, initialState, text, onPress);
		
	}
	
	public void setSymbol(byte symbol) {
		
		this.symbol = symbol;
		setCode(computeLayoutFunction());
		
	}

	public void setStateSymbols(byte trueSymbol , byte falseSymbol) {
		
		this.symbolWhenTrue = trueSymbol;
		this.symbolWhenFalse = falseSymbol;
		setCode(() -> {
			
			if(symbolWhenTrue != -1 && symbolWhenFalse != -1) symbol = checked() ? symbolWhenTrue : symbolWhenFalse;
			computeLayoutFunction().invoke();
			
		});
		
	}
	
}
