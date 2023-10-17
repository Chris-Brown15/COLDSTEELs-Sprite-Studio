package cs.csss.ui.menus;

import static cs.csss.ui.utils.UIUtils.textLength;
import static cs.core.ui.CSUIConstants.*;

import java.util.Iterator;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSText;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.engine.Control;
import cs.csss.engine.Engine;

/**
 * UI menu for modifying the controls of Sprite Studio.
 */
public class ModifyControlsMenu extends Dialogue {

	private static final float w = 0.44f , h = .35f;
	
	
	private Lambda onFinish;
	
	/**
	 * Creates a modify controls menu.
	 * 
	 * @param nuklear — the Nuklear factory
	 * @param engine — the engine
	 */
	public ModifyControlsMenu(CSNuklear nuklear , Engine engine) {

		CSUserInterface ui = nuklear.new CSUserInterface("Controls" , 0.5f - (w / 2) , 0.5f - (h / 2) , w , h);
		
		ui.options = UI_TITLED|UI_BORDERED;
		
		Iterator<Control> iterator = Control.iterator();
		
		while(iterator.hasNext()) {
			
			Control next = iterator.next();
			
			CSRow row = ui.new CSRow(30);
			row.pushWidth(.35f).pushWidth(0.25f).pushWidth(.2f).pushWidth(.15f);
			
			CSText controlName = row.new CSText(next.name);
			
			controlName.initializeToolTip(MOUSE_PRESSED|HOVERING , MOUSE_RIGHT , 0, textLength(next.toolTip));
			controlName.toolTip.new CSDynamicRow(20).new CSText(next.toolTip);
			
			row.new CSText(() -> next.keyToString());

			CSTextEditor newControlInput = row.new CSTextEditor(15);
			
			row.new CSButton("Set" , () -> {
				
				String result = newControlInput.toString().toUpperCase();
				next.key(next.keyFromString(result) , !result.contains("MOUSE"));
				
			});
			
		}
		
		onFinish = () -> { 
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			super.onFinish();
			
		};
		
		ui.new CSDynamicRow().new CSButton("Finish" , onFinish);
		
	}

}