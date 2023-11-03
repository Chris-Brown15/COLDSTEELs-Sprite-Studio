/**
 * 
 */
package cs.csss.ui.elements;

import java.util.concurrent.CopyOnWriteArrayList;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;

/**
 * Decorator for {@code CSUserInterface} which exposes publicly all of the protected fields of {@code CSUI} to the user.
 */
public class FullAccessCSUI extends CSUserInterface {

	/**
	 * Creates a new full access CSUI.
	 * 
	 * @param source — nuklear instance creating this element
	 * @param displayName — name this UI element will display
	 * @param xOffset — ratio representing the horizontal distance this element will be from the top left corner of the window
	 * @param yOffset — ratio representing the vertical distance this element will be from the top left corner of the window
	 * @param widthRatio — ratio representing this width of this element as a ratio of the window
	 * @param heightRatio — ratio representing this height of this element as a ratio of the window
	 */
	public FullAccessCSUI(CSNuklear source , String displayName, float xOffset, float yOffset, float widthRatio, float heightRatio) {
	
		source.super(displayName, xOffset, yOffset, widthRatio, heightRatio);
	
	}

	/**
	 * Creates a new full access CSUI.
	 * 
	 * @param source — nuklear instance creating this element
	 * @param displayName — name this UI element will display
	 * @param xPosition — horizontal position in pixels of the left side of this UI element from the left side of the window
	 * @param yPosition — vertical position in pixels of the top side of this UI element from the top side of the window
	 * @param width — width in pixels of this UI element
	 * @param height — height in pixels of this UI element 
	 */
	public FullAccessCSUI(CSNuklear source , String displayName, int xPosition, int yPosition, int width, int height) {
		
		source.super(displayName, xPosition, yPosition, width, height);
		
	}

	/**
	 * Creates a new full access CSUI.
	 * 
	 * @param source — nuklear instance creating this element
	 * @param displayName — name this UI element will display
	 * @param xOffset — ratio representing the horizontal distance this element will be from the top left corner of the window
	 * @param yOffset — ratio representing the vertical distance this element will be from the top left corner of the window
	 * @param width — width in pixels of this UI element
	 * @param height — height in pixels of this UI element 
	 */
	public FullAccessCSUI(CSNuklear source , String displayName, float xOffset, float yOffset, int width, int height) {
		
		source.super(displayName, xOffset, yOffset, width, height);
		
	}

	/**
	 * Creates a new full access CSUI.
	 * 
	 * @param source — nuklear instance creating this element
	 * @param displayName — name this UI element will display
	 * @param xPosition — horizontal position in pixels of the left side of this UI element from the left side of the window
	 * @param yPosition — vertical position in pixels of the top side of this UI element from the top side of the window
	 * @param widthRatio — ratio representing this width of this element as a ratio of the window
	 * @param heightRatio — ratio representing this height of this element as a ratio of the window
	 */
	public FullAccessCSUI(CSNuklear source , String displayName, int xPosition, int yPosition, float widthRatio, float heightRatio) {
		
		source.super(displayName, xPosition, yPosition, widthRatio, heightRatio);
		
	}

	/**
	 * Returns the x offset value.
	 * 
	 * @return The x offset value.
	 */
	public float xOffset() {
		
		return xOffset;
		
	}

	/**
	 * Returns the y offset value.
	 * 
	 * @return The y offset value.
	 */
	public float yOffset() {
		
		return yOffset;
		
	}

	/**
	 * Sets the x offset value.
	 * 
	 * @param xOffset — new x offset value
	 */
	public synchronized void xOffset(float xOffset) {
		
		this.xOffset = xOffset;
				
	}

	/**
	 * Sets the y offset value.
	 * 
	 * @param yOffset — new y offset value
	 */
	public synchronized void yOffset(float yOffset) {
		
		this.yOffset = yOffset;
		
	}

	/**
	 * Sets the width ratio value.
	 * 
	 * @param ratio — new width ratio value
	 */
	public synchronized void widthRatio(float ratio) {
		
		this.widthRatio = ratio;
				
	}

	/**
	 * Sets the height ratio value.
	 * 
	 * @param ratio — new height ratio value
	 */
	public synchronized void heightRatio(float ratio) {
		
		this.heightRatio = ratio;
				
	}

	/**
	 * Sets the x position value.
	 * 
	 * @param xPosition — new x position
	 */
	public synchronized void xPosition(int xPosition) {
		
		this.xPosition = xPosition;
				
	}

	/**
	 * Sets the y position value.
	 * 
	 * @param yPosition — new y position
	 */
	public synchronized void yPosition(int yPosition) {
		
		this.yPosition = yPosition;
				
	}

	/**
	 * Sets the width of the interface.
	 * 
	 * @param interfaceWidth — new width of the interface
	 */
	public synchronized void interfaceWidth(int interfaceWidth) {
		
		this.interfaceWidth = interfaceWidth;
				
	}

	/**
	 * Sets the height of the interface.
	 * 
	 * @param interfaceHeight — new height of the interface
	 */
	public synchronized void interfaceHeight(int interfaceHeight) {
		
		this.interfaceHeight = interfaceHeight;
				
	}

	/**
	 * Returns the list of layouts this {@code CSUI} contains.
	 * 
	 * @return List of layout objects for this {@code CSUI}.
	 */
	public CopyOnWriteArrayList<CSLayout> interfaceLayouts() {
		
		return this.interfaceLayouts;
		
	}
	
}
