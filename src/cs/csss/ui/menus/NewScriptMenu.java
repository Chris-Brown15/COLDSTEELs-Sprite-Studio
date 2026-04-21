/**
 * 
 */
package cs.csss.ui.menus;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_colored;

import static sc.core.ui.SCUIConstants.*;

import java.util.LinkedList;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.system.MemoryStack;

import cs.csss.editor.ScriptType;
import cs.csss.engine.Engine;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * 
 */
public class NewScriptMenu extends Dialogue {

	private static final float width = 0.30f , height = 0.35f;
	
	private SCTextEditor nameInput;
	private ScriptType type = null;
	private boolean finished = false;
	
	/**
	 * 
	 */
	public NewScriptMenu(SCNuklear nuklear) {
		
		SCUserInterface ui = new SCUserInterface(
			nuklear , 
			"Choose type for new script" , 
			.5f - (width / 2) , 
			.5f - (height / 2) , 
			width , 
			height
		);
		
		ui.flags |= UI_TITLED|UI_BORDERED;
		
		ui.new SCDynamicRow(20).new SCText("Input name for new script.");
		nameInput = ui.new SCDynamicRow().new SCTextEditor(999 , SCNuklear.NO_FILTER);
		
		ui.attachedLayout((context) -> {
			
			String nameInputString = nameInput.toString();
			if(Engine.isReservedScriptName(nameInputString)) {
				
				try(MemoryStack stack = MemoryStack.stackPush()) {
					
					nk_layout_row_dynamic(context , 20 , 1);
					String warning = nameInputString.equals("") ? "Name required." : nameInputString + " is a reserved name.";
					nk_text_colored(
						context , 
						warning , 
						TEXT_LEFT , 
						NkColor.malloc(stack).set((byte)0xee , (byte)0xee , (byte)0 , (byte)0xff)
					);
				
				}
									
			}
			
		});
		
		ScriptType[] types = ScriptType.values();
		LinkedList<SCRadio> radios = new LinkedList<>();
		
		for(ScriptType x : types) radios.add(ui.new SCDynamicRow(20).new SCRadio(x.asTagName(), () -> x == type , () -> type = x));
		
		SCRadio.groupAll(radios.toArray(SCRadio[]::new));
		
		Runnable onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			finished = true;
			
		};
		
		SCDynamicRow finishRow = ui.new SCDynamicRow();
		finishRow.new SCButton("Finish" , () -> {
		
			if(types != null && !nameInput.toString().equals("")) onFinish.run();
			
		});
		
		finishRow.new SCButton("Cancel" , onFinish);
		
	}

	/**
	 * Returns whether this UI is ready to finish and close.
	 * 
	 * @return Whether this UI is ready to finish and close.
	 */
	public boolean finished() {
		
		return finished;
		
	}

	/**
	 * Returns the contents of the name input box.
	 * 
	 * @return String representation of the contents of the name input box.
	 */
	public String nameInput() {
		
		return nameInput.toString();
		
	}
	
	/**
	 * Returns the type of script selected.
	 * 
	 * @return Type of script selected in this element.
	 */
	public ScriptType type() {
		
		return type;
		
	}
	
}
