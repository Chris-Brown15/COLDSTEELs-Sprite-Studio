package cs.csss.ui.menus;

import static cs.csss.utils.UIUtils.textLength;
import static cs.core.ui.CSUIConstants.*;

import java.util.Iterator;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSText;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.core.Control;
import cs.csss.core.Engine;

public class ModifyControlsMenu {

	private Lambda onFinish;
	
	public ModifyControlsMenu(CSNuklear nuklear , Engine engine) {

		CSUserInterface ui = nuklear.new CSUserInterface("Controls" , 0.5f - (0.33f / 2) , 0.5f - (0.35f / 2) , 0.33f , 0.35f);
		
		ui.options = UI_TITLED|UI_BORDERED;
		
		Iterator<Control> iterator = Control.iterator();
		
		while(iterator.hasNext()) {
			
			Control next = iterator.next();
			
			CSRow row = ui.new CSRow(30);
			row.pushWidth(.4f).pushWidth(0.20f).pushWidth(.2f).pushWidth(.15f);
			
			CSText controlName = row.new CSText(next.name);
			
			controlName.initializeToolTip(MOUSE_PRESSED|HOVERING , MOUSE_RIGHT , 0, textLength(nuklear.font().width() , next.toolTip));
			controlName.toolTip.new CSDynamicRow(20).new CSText(next.toolTip);
			
			row.new CSText(() -> "(" + next.keyToString() + ")");

			CSTextEditor newControlInput = row.new CSTextEditor(15);
			
			row.new CSButton("Set" , () -> {
				
				String result = newControlInput.toString().toUpperCase();
				next.key(next.keyFromString(result) , !result.contains("MOUSE"));
				
			});
			
		}
		
		onFinish = () -> { 
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		};
		
		ui.new CSDynamicRow().new CSButton("Finish" , onFinish);
		
	}

}