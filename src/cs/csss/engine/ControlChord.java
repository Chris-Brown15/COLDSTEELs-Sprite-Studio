/**
 * 
 */
package cs.csss.engine;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Support for detecting if a key chord is pressed is provided by this class.
 */
public class ControlChord extends Control {

	public static final ControlChord 
		NEW_PROJECT = new ControlChord("New Project" , "Opens a dialogue for creating a new project" , GLFW_KEY_P , Control.PRELIM) ,
		NEW_ARTBOARD = new ControlChord("New Artboard" , "Opens a dialogue for creating a new artboard" , GLFW_KEY_A , Control.PRELIM) ,
		NEW_ANIMATION = new ControlChord(
			"New Animation" , 
			"Opens a dialogue for creating a new animation" , 
			GLFW_KEY_A , 
			Control.PRELIM , Control.PRELIM2
		) ,
		NEW_VISUAL_LAYER = new ControlChord("New Visual Layer" , "Opens a dialogue for creating a new visual layer" , GLFW_KEY_L , Control.PRELIM),
		NEW_NON_VISUAL_LAYER = new ControlChord(
			"New Nonvisual Layer" , 
			"Opens a dialogue for creating a new nonvisual layer" , 
			GLFW_KEY_N , 
			Control.PRELIM
		);
	
	protected Control[] chord;
	
	/**
	 * Creates a new control chord from the given parameters.
	 * 
	 * @param name — name of this control chord
	 * @param toolTipString — tooltip to display to the user when setting this control chord
	 * @param glfwKeyCode — key code associated with this control
	 * @param isKeyboard — if {@code true}, {@code glfwKeyCode} will be considered to be a keyboard key
	 * @param chord — the key chord accompanying this control
	 */
	public ControlChord(String name, String toolTipString, int glfwKeyCode, boolean isKeyboard , Control...chord) {

		super(name, toolTipString, glfwKeyCode, isKeyboard);
		this.chord = chord;
		
	}

	/**
	 * Creates a new control chord from the given parameters.
	 * 
	 * @param name — name of this control chord
	 * @param toolTipString — tooltip to display to the user when setting this control chord
	 * @param glfwKeyCode — key code associated with this control
	 * @param chord — the key chord accompanying this control
	 */
	public ControlChord(String name, String toolTipString, int glfwKeyCode , Control...chord) {

		this(name, toolTipString, glfwKeyCode, true , chord);
		
	}

	@Override public boolean struck() {
		
		for(Control x : chord) if(!x.pressed) return false;
		return super.struck();
		
	}
	
	@Override public boolean pressed() {
		
		for(Control x : chord) if(!x.pressed) return false;
		return super.pressed();
		
	}
	
	@Override public String keyToString() {
		
		StringBuilder asString = new StringBuilder("(");
		
		for(int i = 0 ; i < chord.length - 1 ; i++) asString.append(chord[i].keyToString() + " + ");
		asString.append(chord[chord.length - 1].keyToString()).append(") + ").append(super.keyToString());
		
		return asString.toString();
		
	}
	
}
