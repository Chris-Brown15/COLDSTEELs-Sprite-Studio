package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.UI_BORDERED;
import static cs.core.ui.CSUIConstants.UI_TITLED;
import static cs.core.ui.CSUIConstants.UI_UNSCROLLABLE;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;

/**
 * UI menu for confirming or cancelling something.
 */
public class ConfirmationBox {

	/**
	 * Constants for the width and height of created confirmation boxes.
	 */
	public static final int
		width = 250 ,
		height = 130;
	
	private final CSNuklear nuklear;
	private final CSUserInterface ui;
	
	/**
	 * Creates a new confirmation box.
	 * 
	 * @param nuklear — the Nuklear factory 
	 * @param title — title of this menu
	 * @param description — description within this menu
	 * @param xRatio — x position of the left side of this box as as ratio of the window width; {@code 0 < xRatio < 1}
	 * @param yRatio — y position of the top of this box as as ratio of the window width; {@code 0 < xRatio < 1}
	 * @param onConfirm — code to invoke when confirmed
	 * @param onDecline — code to invoke when cancelled
	 */
	public ConfirmationBox(
		CSNuklear nuklear , 
		final String title , 
		final String description ,
		final float xRatio , 
		final float yRatio , 
		final Lambda onConfirm , 
		final Lambda onDecline
	) {
	
		this.nuklear = nuklear;
		ui = nuklear.new CSUserInterface(title , xRatio , yRatio , width , height);
		ui.options |= UI_BORDERED|UI_TITLED|UI_UNSCROLLABLE;

		ui.new CSDynamicRow(45).new CSText(description);
		
		CSDynamicRow row = ui.new CSDynamicRow(30);
		row.new CSButton("Confirm" , () -> {
			
			onConfirm.invoke();
			shutDown();
			
		});
		
		row.new CSButton("Cancel" , () -> {
			
			onDecline.invoke();
			shutDown();
			
		});
		
	}
	
	private void shutDown() {

		nuklear.removeUserInterface(ui);
		ui.shutDown();
		
	}

}
