package cs.csss.ui.menus;

import static cs.csss.ui.utils.UIUtils.textLength;
import static sc.core.ui.SCUIConstants.*;

import java.util.Iterator;

import cs.csss.engine.Control;
import cs.csss.engine.Engine;
import sc.core.ui.SCElements.SCUI.SCLayout.SCText;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUI.SCRow;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu for modifying the controls of Sprite Studio.
 */
public class ModifyControlsMenu extends Dialogue {

	private static final float w = 0.44f , h = .35f;
		
	private Runnable onFinish;
	
	/**
	 * Creates a modify controls menu.
	 * 
	 * @param nuklear the Nuklear factory
	 * @param engine the engine
	 */
	public ModifyControlsMenu(SCNuklear nuklear , Engine engine) {

		SCUserInterface ui = new SCUserInterface(nuklear , "Controls" , 0.5f - (w / 2) , 0.5f - (h / 2) , w , h);
		
		ui.flags = UI_TITLED|UI_BORDERED;
		
		Iterator<Control> iterator = Control.iterator();
		
		while(iterator.hasNext()) {
			
			Control next = iterator.next();
			
			SCRow row = ui.new SCRow(30);
			row.pushWidth(.35f).pushWidth(0.25f).pushWidth(.2f).pushWidth(.15f);
			
			SCText controlName = row.new SCText(next.name);
			
			controlName.initializeToolTip(TOOLTIP_MOUSE_PRESSED|TOOLTIP_HOVERING , MOUSE_RIGHT , 0, textLength(next.toolTip));
			controlName.toolTip.new SCDynamicRow(20).new SCText(next.toolTip);
			
			row.new SCText(() -> next.keyToString());

			SCTextEditor newControlInput = row.new SCTextEditor(15);
			
			row.new SCButton("Set" , () -> {
				
				String result = newControlInput.toString().toUpperCase();
				next.key(next.keyFromString(result) , !result.contains("MOUSE"));
				
			});
			
		}
		
		onFinish = () -> { 
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			super.onFinish();
			
		};
		
		ui.new SCDynamicRow().new SCButton("Finish" , onFinish);
		
	}

}