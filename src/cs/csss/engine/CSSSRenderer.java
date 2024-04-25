package cs.csss.engine;

import java.util.concurrent.atomic.AtomicBoolean;

import cs.core.CSWindow;
import cs.core.graphics.CSStandardRenderer;

/**
 * Decorator over the COLDSTEEL Core renderer which lets the user control exactly when the render thread is to shut down.
 */
public class CSSSRenderer extends CSStandardRenderer {

	private AtomicBoolean persist = new AtomicBoolean(true);
	
	/**
	 * Creates a new COLDSTEEL's Sprite Studio renderer.
	 * 
	 * @param window the window this renderer will use for context
	 */
	public CSSSRenderer(CSWindow window) {
		
		super(window);
		
	}
	
	/**
	 * Sets whether this renderer should continue persisting, that is, stay alive.
	 * 
	 * @param state whether to continue persisting
	 */
	public void persist(boolean state) {
		
		this.persist.set(state);
		
	}
		
	@Override public boolean persist() {
		
		return persist.get();
		
	}
	
}
