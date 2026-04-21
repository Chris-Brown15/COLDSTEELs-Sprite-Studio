package cs.csss.ui.elements;

import static org.lwjgl.nuklear.Nuklear.nk_prog;

import java.util.function.LongSupplier;

import sc.core.ui.SCElements.SCUI.SCLayout;
import sc.core.ui.SCElements.SCUI.SCLayout.SCElement;
import sc.core.ui.SCNuklear;

/**
 * Progress bar which slides from left to right to fill out a region. Used for the animation panel.
 */
public class ProgressBar extends SCElement {

	/**
	 * Whether the cursor can adjust the progress bar.
	 */
	public boolean modifiable;
	
	private long state = 0;
	
	/**
	 * Creates a new progress bar.
	 * 
	 * @param nuklear the Nuklear factory
	 * @param layout an owning layout
	 * @param progressValue a provider of a current value for the slider
	 * @param maxValue a provider for the max value the slider can reach
	 * @param modifiable default value for {@link ProgressBar#modifiable modifiable}
	 */
	public ProgressBar(SCNuklear nuklear , SCLayout layout , LongSupplier progressValue , LongSupplier maxValue , boolean modifiable) {

		layout.super();
		this.modifiable = modifiable;
		
		setCode(() -> state = nk_prog(nuklear.context() , progressValue.getAsLong() , maxValue.getAsLong() , this.modifiable));
		
	}
	
	/**
	 * Returns the current value of this progress bar.
	 * 
	 * @return Current value of this progress bar.
	 */
	public long result() {
		
		return state;
		
	}

	@Override protected void onResize() {}

}
