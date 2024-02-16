package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import java.util.function.Consumer;

import org.lwjgl.nuklear.NkPluginFilter;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.ui.utils.UIUtils;

/**
 * Generic class for input boxes with a more verbose description within the UI element.
 */
public class DetailedInputBox extends Dialogue {

	private CSUserInterface ui;
	
	/**
	 * Creates a new detailed input box.
	 * 
	 * @param nuklear — the Nuklear factory
	 * @param title — the title of the input box 
	 * @param description — the detailed description in the input box
	 * @param xPositionRatio — the position of the left coordinates of the box relative to the window's size; {@code 0 < xPositionRatio < 1f}
	 * @param yPositionRatio — the position of the top coordinates of the box relative to the window's size; {@code 0 < yPositionRatio < 1f}
	 * @param widthRatio — the width of the element as a ratio of the window's width; {@code 0 < windowWidth <= 1}
	 * @param heightRatio — the height of the element as a ratio of the window's height; {@code 0 < heightRatio <= 1}
	 * @param filter — a filter to apply to the text allowed to be input
	 * @param maxCharacters — the max amount of characters the user is allowed to input
	 * @param onAccept — code to invoke on completion
	 */
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
		Consumer<String> onAccept ,
		Lambda onCancel
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
		
		finishRow.new CSButton("Cancel" , () -> {
			
			onCancel.invoke();
			shutDown(nuklear , ui);
			
		});
		
	}

	private void shutDown(CSNuklear nuklear , CSUserInterface ui) {
		
		nuklear.removeUserInterface(ui);
		ui.shutDown();
		super.onFinish();
				
	}
	
}
