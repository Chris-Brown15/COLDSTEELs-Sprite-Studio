package cs.csss.ui.prefabs;

import static cs.core.ui.CSUIConstants.*;

import java.util.function.Consumer;

import org.lwjgl.nuklear.NkPluginFilter;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.csss.ui.utils.UIUtils;

public class DetailedInputBox {

	private static final void shutDown(CSNuklear nuklear , CSUserInterface ui) {
		
		nuklear.removeUserInterface(ui);
		ui.shutDown();
				
	}
	
	private CSUserInterface ui;
	
	public DetailedInputBox(
		CSNuklear nuklear , 
		String title , 
		String description , 
		float xPositionRatio , 
		float yPositionRatio , 
		float widthRatio , 
		float heightRatio ,
		NkPluginFilter filter ,
		int maxCharacters ,
		Consumer<String> onAccept
	) {

		ui = nuklear.new CSUserInterface(title , xPositionRatio , yPositionRatio , widthRatio , heightRatio);
		ui.options = UI_TITLED|UI_BORDERED;		

		int length = UIUtils.textLength(description);
				
		int realHeight = ui.interfaceHeight();
		
		int height = (int) Math.ceil((length / realHeight)) * 20;
		
		ui.new CSDynamicRow(height).new CSText(description);
		
		CSTextEditor input = ui.new CSDynamicRow().new CSTextEditor(maxCharacters , filter);
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		
		finishRow.new CSButton("Finish" , () -> { 
		
			onAccept.accept(input.toString());
			shutDown(nuklear, ui);
			
		});
		
		finishRow.new CSButton("Cancel" , () -> shutDown(nuklear , ui));
		
	}

}
