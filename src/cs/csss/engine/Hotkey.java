/**
 * 
 */
package cs.csss.engine;

import static java.lang.String.format;
import static org.lwjgl.glfw.GLFW.*;

import java.util.function.Supplier;

import cs.csss.editor.Editor;
import cs.csss.editor.brush.CSSSBrush;

/**
 * Representation of a hotkey, which is a type of control that sets a brush to the active brush.
 */
public class Hotkey extends Control {

	private static final String descFormat = "Makes the %s brush the active brush." , nameFormat = "%s Brush Hotkey" ;
	public static final Hotkey
		PENCIL_HOTKEY = new Hotkey("Pencil" , GLFW_KEY_P , () -> Editor.thePencilBrush) ,
		ERASER_HOTKEY = new Hotkey("Eraser" , GLFW_KEY_E , () -> Editor.theEraserBrush) ,
		EYE_DROPPER_HOTKEY = new Hotkey("Eye Dropper" , GLFW_KEY_Q , () -> Editor.theEyeDropper) ,
		FLOOD_FILL_HOTKEY = new Hotkey("Flood Fill" , GLFW_KEY_F , () -> Editor.theFloodFill) ,
		REPLACE_ALL_HOTKEY = new Hotkey("Replace All" , GLFW_KEY_U , () -> Editor.theReplaceAllBrush) ,
		MOVE_REGION_HOTKEY = new Hotkey("Move Region" , GLFW_KEY_M , () -> Editor.theRegionSelect) ,
		DELETE_REGION_HOTKEY = new Hotkey("Delete Region" , GLFW_KEY_X , () -> Editor.theDeleteRegion) ,
		ROTATE_REGION_HOTKEY = new Hotkey("Rotate Region" , GLFW_KEY_I , () -> Editor.theRotateBrush) ,
		SCALE_REGION_HOTKEY = new Hotkey("Scale Region" , GLFW_KEY_O , () -> Editor.theScaleBrush) ,
		COPY_REGION_HOTKEY = new Hotkey("Copy Region" , GLFW_KEY_C , () -> Editor.theCopyBrush) ,
		SCRIPT_REGION_HOTKEY = new Hotkey("Script" , GLFW_KEY_J , Editor::theScriptBrush),
		MODIFYING_SCRIPT_REGION_HOTKEY = new Hotkey("Script" , GLFW_KEY_K , Editor::theModifyingScriptBrush) ,
		SELECTING_SCRIPT_REGION_HOTKEY = new Hotkey("Script" , GLFW_KEY_L , Editor::theSelectingScriptBrush);
	
	/**
	 * Updates the hotkeys, setting controls based on whether the hotkey was pressed.
	 * 
	 * @param editor
	 */
	public static void updateHotkeys(Editor editor) {
		
		if(Control.PRELIM.pressed() || Control.PRELIM2.pressed()) return;
		
		controls.stream()
			.filter(element -> element instanceof Hotkey && element.struck)
			.map(control -> (Hotkey)control)
			.forEach(x -> editor.setBrushTo(x.mappedBrush.get()));
		
	}
	
	protected Supplier<CSSSBrush> mappedBrush;
	
	/**
	 * Creates a new hotkey with the given name and the given key, which is assumed to be a keyboard keycode.
	 * 
	 * @param name — name of the hotkey
	 * @param glfwKeyCode — keycode to map to this control 
	 */
	public Hotkey(String name , int glfwKeyCode , Supplier<CSSSBrush> mappedBrush) {

		this(name , glfwKeyCode , true , mappedBrush);

	}

	/**
	 * Creates a new hotkey with the given name and the given key, which is considered to be a keyboard key if {@code isKeyboard} is {@code true}.
	 * 
	 * @param name — name of the hotkey
	 * @param glfwKeyCode — keycode to map to this control 
	 * @param isKeyboard — {@code true} if {@code glfwKeyCode} corresponds to a keyboard key
	 */
	public Hotkey(String name, int glfwKeyCode, boolean isKeyboard , Supplier<CSSSBrush> mappedBrush) {

		super(format(nameFormat , name) , format(descFormat , name) , glfwKeyCode , isKeyboard);
		this.mappedBrush = mappedBrush;

	}

}
