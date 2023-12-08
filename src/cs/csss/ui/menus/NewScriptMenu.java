/**
 * 
 */
package cs.csss.ui.menus;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_colored;

import static cs.core.ui.CSUIConstants.*;

import java.util.LinkedList;

import org.lwjgl.nuklear.NkColor;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.editor.ScriptType;
import cs.csss.engine.Engine;

/**
 * 
 */
public class NewScriptMenu extends Dialogue {

	private static final float width = 0.30f , height = 0.35f;
	
	private CSTextEditor nameInput;
	private ScriptType type = null;
	private boolean finished = false;
	
	/**
	 * 
	 */
	public NewScriptMenu(CSNuklear nuklear) {
		
		CSUserInterface ui = nuklear.new CSUserInterface("Choose type for new script" , .5f - (width / 2) , .5f - (height / 2) , width , height);
		ui.options |= UI_TITLED|UI_BORDERED;
		
		ui.new CSDynamicRow(20).new CSText("Input name for new script.");
		nameInput = ui.new CSDynamicRow().new CSTextEditor(999 , CSNuklear.NO_FILTER);
		
		ui.attachedLayout((context , stack) -> {
			
			String nameInputString = nameInput.toString();
			if(Engine.isReservedScriptName(nameInputString)) {
				
				nk_layout_row_dynamic(context , 20 , 1);
				String warning = nameInputString.equals("") ? "Name required." : nameInputString + " is a reserved name.";
				nk_text_colored(context , warning , TEXT_LEFT , NkColor.malloc(stack).set((byte)0xee , (byte)0xee , (byte)0 , (byte)0xff));
									
			}
			
		});
		
		ScriptType[] types = ScriptType.values();
		LinkedList<CSRadio> radios = new LinkedList<>();
		
		for(ScriptType x : types) radios.add(ui.new CSDynamicRow(20).new CSRadio(x.asTagName(), () -> x == type , () -> type = x));
		
		CSRadio.groupAll(radios.toArray(CSRadio[]::new));
		
		Lambda onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			finished = true;
			
		};
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		finishRow.new CSButton("Finish" , () -> {
		
			if(types != null && !nameInput.toString().equals("")) onFinish.invoke();
			
		});
		
		finishRow.new CSButton("Cancel" , onFinish);
		
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
