package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import java.util.function.Consumer;

import org.lwjgl.nuklear.NkPluginFilter;

import cs.csss.ui.utils.UIUtils;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * Generic class for input boxes with a more verbose description within the UI element.
 */
public class DetailedInputBox extends Dialogue {

	private SCUserInterface ui;
	
	/**
	 * Creates a new detailed input box.
	 * 
	 * @param nuklear the Nuklear factory
	 * @param title the title of the input box 
	 * @param description the detailed description in the input box
	 * @param xPositionRatio the position of the left coordinates of the box relative to the window's size; {@code 0 < xPositionRatio < 1f}
	 * @param yPositionRatio the position of the top coordinates of the box relative to the window's size; {@code 0 < yPositionRatio < 1f}
	 * @param widthRatio the width of the element as a ratio of the window's width; {@code 0 < windowWidth <= 1}
	 * @param heightRatio the height of the element as a ratio of the window's height; {@code 0 < heightRatio <= 1}
	 * @param filter a filter to apply to the text allowed to be input
	 * @param maxCharacters the max amount of characters the user is allowed to input
	 * @param onAccept code to invoke on completion
	 */
	public DetailedInputBox(
		SCNuklear nuklear , 
		String title , 
		String description , 
		float xPositionRatio , 
		float yPositionRatio , 
		float widthRatio , 
		float heightRatio ,
		NkPluginFilter filter ,
		int maxCharacters ,
		Consumer<String> onAccept ,
		Runnable onCancel
	) {

		ui = new SCUserInterface(nuklear , title , xPositionRatio , yPositionRatio , widthRatio , heightRatio);
		ui.flags = UI_TITLED|UI_BORDERED;		

		int length = UIUtils.textLength(description);
				
		int realHeight = ui.positioner.height();
		
		int height = (int) Math.ceil((length / realHeight)) * 20;
		
		ui.new SCDynamicRow(height).new SCText(description);
		
		SCTextEditor input = ui.new SCDynamicRow().new SCTextEditor(maxCharacters , filter);
		
		SCDynamicRow finishRow = ui.new SCDynamicRow();
		
		finishRow.new SCButton("Finish" , () -> { 
		
			onAccept.accept(input.toString());
			shutDown(nuklear, ui);
			
		});
		
		finishRow.new SCButton("Cancel" , () -> {
			
			onCancel.run();
			shutDown(nuklear , ui);
			
		});
		
	}

	private void shutDown(SCNuklear nuklear , SCUserInterface ui) {
		
		nuklear.removeUserInterface(ui);
		ui.shutDown();
		super.onFinish();
				
	}
	
}
