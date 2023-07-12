package cs.csss.ui.elements;

import static org.lwjgl.nuklear.Nuklear.nk_prog;

import java.util.function.LongSupplier;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSLayout;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSElement;

public class ProgressBar extends CSElement {

	public boolean modifiable;
	
	private long state = 0;
	
	public ProgressBar(CSNuklear nuklear , CSLayout layout , LongSupplier progressValue , LongSupplier maxValue , boolean modifiable) {

		layout.super();
		this.modifiable = modifiable;
		
		setCode(() -> state = nk_prog(nuklear.context() , progressValue.getAsLong() , maxValue.getAsLong() , this.modifiable));
		
	}
	
	public long result() {
		
		return state;
		
	}

	@Override protected void onResize() {}

}
