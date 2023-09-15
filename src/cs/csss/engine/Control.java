package cs.csss.engine;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * Representation of a control, meaning a key bound to an action.
 * 
 * @author Chris Brown
 *
 */
public final class Control {
	
	private static final ArrayList<Control> controls = new ArrayList<>();
	
	static BiFunction<Integer , Boolean , Boolean> checkPressedCallback;
	
	/**
	 * Default controls.
	 */
	public static final Control
		UNDO = new Control("Undo" , "Undos a previously done action" , GLFW_KEY_Z) ,
		REDO = new Control("Redo" , "Redos a previously undone action" , GLFW_KEY_Y) ,
		CAMERA_UP = new Control("Move Camera Up" , "Moves the camera up." , GLFW_KEY_W) ,
		CAMERA_DOWN = new Control("Move Camera Down" , "Moves the camera down." , GLFW_KEY_S) ,
		CAMERA_LEFT = new Control("Move Camera Left" , "Moves the camera left." , GLFW_KEY_A) ,
		CAMERA_RIGHT = new Control("Move Camera Right" , "Moves the camera right." , GLFW_KEY_D) ,
		TOGGLE_FULLSCREEN_HOTKEY = new Control("Toggle Fullscreen" , "Switches between fullscreen and nonfullscreen." , GLFW_KEY_F1) ,
		PRELIM = new Control(
			"Preliminary" , 
			"Used for some actions that require a key chord to be pressed rather than just one key." , 
			GLFW_KEY_LEFT_CONTROL
		),
		PRELIM2 = new Control(
			"Preliminary 2" , 
			"If present in addition to some key chords, a different action is performed." , 
			GLFW_KEY_LEFT_SHIFT
		) ,
		ARTBOARD_INTERACT = new Control(
			"Artboard Interact" , 
			"Interact with the artboard according to the type of brush selected." , 
			GLFW_MOUSE_BUTTON_LEFT , 
			false
		) ,
		GO_UP_ONE_LAYER = new Control(
			"Up One Layer" ,
			"Sets the active layer to the one above the current one." ,
			GLFW_MOUSE_BUTTON_4 ,
			false
		) ,
		GO_DOWN_ONE_LAYER = new Control(
			"Down One Layer" ,
			"Sets the active layer to the one below the current one." ,
			GLFW_MOUSE_BUTTON_5 ,
			false
		) ,
		MOVE_SELECTION_AREA = new Control(
			"Move Selection Area" , 
			"Moves the selection area if one is present." , 
			GLFW_MOUSE_BUTTON_RIGHT , 
			false
		)
	;
	
	/**
	 * Sets the callback to invoke to determine whether a key is pressed or not.
	 * 
	 * @param _checkPressedCallback — function who takes a keycode and {@code true} if the key is a keyboard key, {@code false} if the key
	 * 								  is a mouse button, and returns true if the key is pressed
	 */
	static void checkPressedCallback(final BiFunction<Integer , Boolean , Boolean> _checkPressedCallback) {
		
		checkPressedCallback = _checkPressedCallback;
		
	}
	
	/**
	 * Iterates over all controls and checks whether they are pressed or not. Also updates each control's {@code struck} field if necessary.
	 */
	static void updateAllControls() {
		
		for(Control x : controls) { 
			
			boolean wasPressed = x.pressed;
			
			x.pressed = checkPressedCallback.apply(x.key , x.isKeyboard);
			if(!wasPressed && x.pressed) x.struck = true;
			else x.struck = false;
			
		}
		
	}
	
	public final String 
		toolTip ,
		name
	;
	
	private int key;
	
	/**
	 * Represents whether this control is pressed this main loop iteration. Will only be true once per physical pressing of a key.
	 */
	private boolean struck = false;
	private boolean pressed = false;
	private boolean isKeyboard = true;
	
	/**
	 * Creates a new control with a given tooltip and a glfw key code which is assumed to be a keyboard keycode.
	 * 
	 * @param toolTipString — {@code String} representing a tooltip for this control
	 * @param glfwKeyCode — a GLFW keycode representing the key bound to this control
	 */
	public Control(final String name , final String toolTipString , final int glfwKeyCode) {
		
		controls.add(this);
		this.name = name;
		this.toolTip = toolTipString;
		this.key = glfwKeyCode;
		
	}

	/**
	 * Creates a new control with a given tooltip and no specific key assignment. This control will never be pressed or struck until a 
	 * key is assigned to it.
	 * 
	 * @param toolTipString — {@code String} representing a tooltip for this control
	 */ 
	public Control(final String name ,final String toolTipString) {
		
		controls.add(this);
		this.name = name;
		this.toolTip = toolTipString;
		
	}

	/**
	 * Creates a new control with a given tooltip, a glfw keycode, and which peripheral the key code belongs to.
	 * 
	 * @param toolTipString — {@code String} representing a tooltip for this control
	 * @param glfwKeyCode — a GLFW keycode representing the key bound to this control
	 * @param isKeyboard — {@code true} if {@code glfwKeyCode} is a keyboard key code, {@code false} if it is a mouse key code.
	 */
	public Control(final String name , final String toolTipString , final int glfwKeyCode , final boolean isKeyboard) {
		
		controls.add(this);
		this.name = name;
		this.toolTip = toolTipString;
		this.key = glfwKeyCode;
		this.isKeyboard = isKeyboard;
		
	}
	
	/**
	 * Returns the key code associated with this control.
	 * 
	 * @return Key code associated with this control.
	 */
	public int key() {
		
		return key;
		
	}

	/**
	 * Sets this control's keycode bound to it.
	 * 
	 * @param key — glfw keycode.
	 */
	public void key(int key) {
		
		this.key = key;
		
	}
	
	/**
	 * Assigns a key to this control, specifying whether the key is a keyboard key or a mouse key.
	 * 
	 * @param key — a glfw key code 
	 * @param isKeyboard — {@code true} if {@code key} is a keyboard key code, {@code false} if it is a mouse key code
	 */
	public void key(int key , boolean isKeyboard) {
		
		this.key = key;
		this.isKeyboard = isKeyboard;
		
	}
		
	/**
	 * Returns whether the key associated with this control is a keyboard key. If this method returns {@code true}, the key is a keyboard
	 * key, if {@code false}, it is a mouse key.
	 * 
	 * @return Whether the key associated with this control is a keyboard key.
	 */
	public boolean isKeyboard() {
		
		return isKeyboard;
		
	}
	
	/**
	 * Sets the state of this control's {@code isKeyboard} variable.
	 * 
	 * @param isKeyboard — boolean notating if this control is a keyboard control
	 */
	public void isKeyboard(boolean isKeyboard) {
		
		this.isKeyboard = isKeyboard;
		
	}
	
	/**
	 * Returns whether this key is struck. A struck key is a key who is pressed as of this main loop iteration, but was not the previous
	 * iteration. This means that if a key is held down, this method will only return {@code true} the first 'frame' of the pressing of
	 * this key.
	 * 
	 * @return Whether this key is struck.
	 */
	public boolean struck() {
		
		return struck;
		
	}

	/**
	 * Returns whether this key is pressed.
	 * 
	 * @return Whether this key is pressed.
	 */
	public boolean pressed() {
		
		return pressed;
		
	}
	
	public int keyFromString(String source) {
		
		source = source.toUpperCase();
		
		return switch(source) {
			case "SPACE" -> GLFW_KEY_SPACE;
			case "'" -> GLFW_KEY_APOSTROPHE; 
			case "," -> GLFW_KEY_COMMA;
			case "-" -> GLFW_KEY_MINUS;
			case "." -> GLFW_KEY_PERIOD;
			case "/" -> GLFW_KEY_SLASH;
			case "0" -> GLFW_KEY_0;
			case "1" -> GLFW_KEY_1;
			case "2" -> GLFW_KEY_2;
			case "3" -> GLFW_KEY_3;
			case "4" -> GLFW_KEY_4;
			case "5" -> GLFW_KEY_5;
			case "6" -> GLFW_KEY_6;
			case "7" -> GLFW_KEY_7;
			case "8" -> GLFW_KEY_8;
			case "9" -> GLFW_KEY_9;
			case ";" -> GLFW_KEY_SEMICOLON;
			case "=" -> GLFW_KEY_EQUAL;
			case "A" -> GLFW_KEY_A;
			case "B" -> GLFW_KEY_B;
			case "C" -> GLFW_KEY_C;
			case "D" -> GLFW_KEY_D;
			case "E" -> GLFW_KEY_E;
			case "F" -> GLFW_KEY_F;
			case "G" -> GLFW_KEY_G;
			case "H" -> GLFW_KEY_H;
			case "I" -> GLFW_KEY_I;
			case "J" -> GLFW_KEY_J;
			case "K" -> GLFW_KEY_K;
			case "L" -> GLFW_KEY_L;
			case "M" -> GLFW_KEY_M;
			case "N" -> GLFW_KEY_N;
			case "O" -> GLFW_KEY_O;
			case "P" -> GLFW_KEY_P;
			case "Q" -> GLFW_KEY_Q;
			case "R" -> GLFW_KEY_R;
			case "S" -> GLFW_KEY_S;
			case "T" -> GLFW_KEY_T;
			case "U" -> GLFW_KEY_U;
			case "V" -> GLFW_KEY_V;
			case "W" -> GLFW_KEY_W;
			case "X" -> GLFW_KEY_X;
			case "Y" -> GLFW_KEY_Y;
			case "Z" -> GLFW_KEY_Z;
			case "[" -> GLFW_KEY_LEFT_BRACKET;
			case "\\" -> GLFW_KEY_BACKSLASH;
			case "]" -> GLFW_KEY_RIGHT_BRACKET;
			case "~" -> GLFW_KEY_GRAVE_ACCENT;
			case "ESCAPE" -> GLFW_KEY_ESCAPE;
			case "ENTER" -> GLFW_KEY_ENTER;
			case "TAB" -> GLFW_KEY_TAB;
			case "BACKSPACE" -> GLFW_KEY_BACKSPACE;
			case "INSERT" -> GLFW_KEY_INSERT;
			case "DELETE" -> GLFW_KEY_DELETE;       
			case "RIGHT ARROW" -> GLFW_KEY_RIGHT;
			case "LEFT ARROW" -> GLFW_KEY_LEFT;
			case "DOWN ARROW" -> GLFW_KEY_DOWN;
			case "UP ARROW" -> GLFW_KEY_UP;
			case "PAGE UP" -> GLFW_KEY_PAGE_UP;
			case "PAGE DOWN" -> GLFW_KEY_PAGE_DOWN;
			case "HOME" -> GLFW_KEY_HOME;
			case "END" -> GLFW_KEY_END;
			case "F1" -> GLFW_KEY_F1;
			case "F2" -> GLFW_KEY_F2;
			case "F3" -> GLFW_KEY_F3;          
			case "F4" -> GLFW_KEY_F4;          
			case "F5" -> GLFW_KEY_F5;          
			case "F6" -> GLFW_KEY_F6;          
			case "F7" -> GLFW_KEY_F7;          
			case "F8" -> GLFW_KEY_F8;          
			case "F9" -> GLFW_KEY_F9;          
			case "F10" -> GLFW_KEY_F10;          
			case "F11" -> GLFW_KEY_F11;          
			case "F12" -> GLFW_KEY_F12;          
			case "F13" -> GLFW_KEY_F13;          
			case "F14" -> GLFW_KEY_F14;          
			case "F15" -> GLFW_KEY_F15;          
			case "F16" -> GLFW_KEY_F16;          
			case "F17" -> GLFW_KEY_F17;          
			case "F18" -> GLFW_KEY_F18;          
			case "F19" -> GLFW_KEY_F19;          
			case "F20" -> GLFW_KEY_F20;          
			case "F21" -> GLFW_KEY_F21;          
			case "F22" -> GLFW_KEY_F22;          
			case "F23" -> GLFW_KEY_F23;          
			case "F24" -> GLFW_KEY_F24;          
			case "F25" -> GLFW_KEY_F25;          
			case "KEY PAD 0" -> GLFW_KEY_KP_0;        
			case "KEY PAD 1" -> GLFW_KEY_KP_1;        
			case "KEY PAD 2" -> GLFW_KEY_KP_2;        
			case "KEY PAD 3" -> GLFW_KEY_KP_3;        
			case "KEY PAD 4" -> GLFW_KEY_KP_4;        
			case "KEY PAD 5" -> GLFW_KEY_KP_5;        
			case "KEY PAD 6" -> GLFW_KEY_KP_6;        
			case "KEY PAD 7" -> GLFW_KEY_KP_7;        
			case "KEY PAD 8" -> GLFW_KEY_KP_8;        
			case "KEY PAD 9" -> GLFW_KEY_KP_9;        
			case "KEY PAD ." -> GLFW_KEY_KP_DECIMAL;
			case "KEY PAD /" -> GLFW_KEY_KP_DIVIDE;
			case "KEY PAD *" -> GLFW_KEY_KP_MULTIPLY;
			case "KEY PAD -" -> GLFW_KEY_KP_SUBTRACT;
			case "KEY PAD +" -> GLFW_KEY_KP_ADD;
			case "KEY PAD ENTER" -> GLFW_KEY_KP_ENTER;
			case "KEY PAD =" -> GLFW_KEY_KP_EQUAL;
			case "LEFT SHIFT" -> GLFW_KEY_LEFT_SHIFT;
			case "LEFT CONTROL" -> GLFW_KEY_LEFT_CONTROL;
			case "LEFT ALT" -> GLFW_KEY_LEFT_ALT;
			case "RIGHT SHIFT" -> GLFW_KEY_RIGHT_SHIFT;
			case "RIGHT CONTROL" -> GLFW_KEY_RIGHT_CONTROL;
			case "RIGHT ALT" -> GLFW_KEY_RIGHT_ALT;
			case "LEFT MOUSE" -> GLFW_MOUSE_BUTTON_1;
			case "RIGHT MOUSE" -> GLFW_MOUSE_BUTTON_2;
			case "MIDDLE MOUSE" -> GLFW_MOUSE_BUTTON_3;
			case "MOUSE MACRO 1" -> GLFW_MOUSE_BUTTON_4;
			case "MOUSE MACRO 2" -> GLFW_MOUSE_BUTTON_5;
			case "MOUSE MACRO 3" -> GLFW_MOUSE_BUTTON_6;
			case "MOUSE MACRO 4" -> GLFW_MOUSE_BUTTON_7;
			case "MOUSE MACRO 5" -> GLFW_MOUSE_BUTTON_8;
			default -> -1;
		};
		
	}

	public String keyToString() {
								
		return switch(key) {
			case GLFW_KEY_SPACE -> "Space";
			case GLFW_KEY_APOSTROPHE -> "'";
			case GLFW_KEY_COMMA -> ",";
			case GLFW_KEY_MINUS -> "-";
			case GLFW_KEY_PERIOD -> ".";
			case GLFW_KEY_SLASH -> "/";
			case GLFW_KEY_0 -> "0";
			case GLFW_KEY_1 -> "1";
			case GLFW_KEY_2 -> "2";
			case GLFW_KEY_3 -> "3";
			case GLFW_KEY_4 -> "4";
			case GLFW_KEY_5 -> "5";
			case GLFW_KEY_6 -> "6";
			case GLFW_KEY_7 -> "7";
			case GLFW_KEY_8 -> "8";
			case GLFW_KEY_9 -> "9";
			case GLFW_KEY_SEMICOLON -> "'";
			case GLFW_KEY_EQUAL -> "=";
			case GLFW_KEY_A -> "A";
			case GLFW_KEY_B -> "B";
			case GLFW_KEY_C -> "C";
			case GLFW_KEY_D -> "D";
			case GLFW_KEY_E -> "E";
			case GLFW_KEY_F -> "F";
			case GLFW_KEY_G -> "G";
			case GLFW_KEY_H -> "H";
			case GLFW_KEY_I -> "I";
			case GLFW_KEY_J -> "J";
			case GLFW_KEY_K -> "K";
			case GLFW_KEY_L -> "L";
			case GLFW_KEY_M -> "M";
			case GLFW_KEY_N -> "N";
			case GLFW_KEY_O -> "O";
			case GLFW_KEY_P -> "P";
			case GLFW_KEY_Q -> "Q";
			case GLFW_KEY_R -> "R";
			case GLFW_KEY_S -> "S";
			case GLFW_KEY_T -> "T";
			case GLFW_KEY_U -> "U";
			case GLFW_KEY_V -> "V";
			case GLFW_KEY_W -> "W";
			case GLFW_KEY_X -> "X";
			case GLFW_KEY_Y -> "Y";
			case GLFW_KEY_Z -> "Z";
			case GLFW_KEY_LEFT_BRACKET -> "[";
			case GLFW_KEY_BACKSLASH -> "\\";
			case GLFW_KEY_RIGHT_BRACKET -> "]";
			case GLFW_KEY_GRAVE_ACCENT -> "~";
			case GLFW_KEY_ESCAPE -> "Escape";
			case GLFW_KEY_ENTER -> "Enter";
			case GLFW_KEY_TAB -> "Tab";
			case GLFW_KEY_BACKSPACE -> "Backspace";
			case GLFW_KEY_INSERT -> "Insert";
			case GLFW_KEY_DELETE -> "Delete";       
			case GLFW_KEY_RIGHT -> "Right Arrow";
			case GLFW_KEY_LEFT -> "Left Arrow";
			case GLFW_KEY_DOWN -> "Down Arrow";
			case GLFW_KEY_UP -> "Up Arrow";
			case GLFW_KEY_PAGE_UP -> "Page Up";
			case GLFW_KEY_PAGE_DOWN -> "Page Down";
			case GLFW_KEY_HOME -> "Home";
			case GLFW_KEY_END -> "End";
			case GLFW_KEY_F1 -> "F1";
			case GLFW_KEY_F2 -> "F2";
			case GLFW_KEY_F3 -> "F3";          
			case GLFW_KEY_F4 -> "F4";          
			case GLFW_KEY_F5 -> "F5";          
			case GLFW_KEY_F6 -> "F6";          
			case GLFW_KEY_F7 -> "F7";          
			case GLFW_KEY_F8 -> "F8";          
			case GLFW_KEY_F9 -> "F9";          
			case GLFW_KEY_F10 -> "F10";          
			case GLFW_KEY_F11 -> "F11";          
			case GLFW_KEY_F12 -> "F12";          
			case GLFW_KEY_F13 -> "F13";          
			case GLFW_KEY_F14 -> "F14";          
			case GLFW_KEY_F15 -> "F15";          
			case GLFW_KEY_F16 -> "F16";          
			case GLFW_KEY_F17 -> "F17";          
			case GLFW_KEY_F18 -> "F18";          
			case GLFW_KEY_F19 -> "F19";          
			case GLFW_KEY_F20 -> "F20";          
			case GLFW_KEY_F21 -> "F21";          
			case GLFW_KEY_F22 -> "F22";          
			case GLFW_KEY_F23 -> "F23";          
			case GLFW_KEY_F24 -> "F24";          
			case GLFW_KEY_F25 -> "F25";          
			case GLFW_KEY_KP_0 -> "Key Pad 0";        
			case GLFW_KEY_KP_1 -> "Key Pad 1";        
			case GLFW_KEY_KP_2 -> "Key Pad 2";        
			case GLFW_KEY_KP_3 -> "Key Pad 3";        
			case GLFW_KEY_KP_4 -> "Key Pad 4";        
			case GLFW_KEY_KP_5 -> "Key Pad 5";        
			case GLFW_KEY_KP_6 -> "Key Pad 6";        
			case GLFW_KEY_KP_7 -> "Key Pad 7";        
			case GLFW_KEY_KP_8 -> "Key Pad 8";        
			case GLFW_KEY_KP_9 -> "Key Pad 9";        
			case GLFW_KEY_KP_DECIMAL -> "Key Pad .";
			case GLFW_KEY_KP_DIVIDE -> "Key Pad /";
			case GLFW_KEY_KP_MULTIPLY -> "Key Pad *";
			case GLFW_KEY_KP_SUBTRACT -> "Key Pad -";
			case GLFW_KEY_KP_ADD -> "Key Pad +";
			case GLFW_KEY_KP_ENTER -> "Key Pad Enter";
			case GLFW_KEY_KP_EQUAL -> "Key Pad =";
			case GLFW_KEY_LEFT_SHIFT -> "Left Shift";
			case GLFW_KEY_LEFT_CONTROL -> "Left Control";
			case GLFW_KEY_LEFT_ALT -> "Left Alt";
			case GLFW_KEY_RIGHT_SHIFT -> "Right Shift";
			case GLFW_KEY_RIGHT_CONTROL -> "Right Control";
			case GLFW_KEY_RIGHT_ALT -> "Right Alt";
			case GLFW_MOUSE_BUTTON_1 -> "Left Mouse";
			case GLFW_MOUSE_BUTTON_2 -> "Right Mouse";
			case GLFW_MOUSE_BUTTON_3 -> "Middle Mouse";
			case GLFW_MOUSE_BUTTON_4 -> "Mouse Macro 1";
			case GLFW_MOUSE_BUTTON_5 -> "Mouse Macro 2";
			case GLFW_MOUSE_BUTTON_6 -> "Mouse Macro 3";
			case GLFW_MOUSE_BUTTON_7 -> "Mouse Macro 4";
			case GLFW_MOUSE_BUTTON_8 -> "Mouse Macro 5";
			default -> "Unknown Key";
		};
		
	}
	
	public static Iterator<Control> iterator() {

		return controls.iterator();
		
	}
	
}