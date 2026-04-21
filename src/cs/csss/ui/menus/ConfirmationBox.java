package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;


/**
 * UI menu for confirming or cancelling something.
 */
public class ConfirmationBox extends Dialogue {

	/**
	 * Constants for the width and height of created confirmation boxes.
	 */
	public static final int
		width = 250 ,
		height = 130;
	
	private final SCNuklear nuklear;
	private final SCUserInterface ui;
	
	/**
	 * Creates a new confirmation box.
	 * 
	 * @param nuklear the Nuklear factory 
	 * @param title title of this menu
	 * @param description description within this menu
	 * @param xRatio x position of the left side of this box as as ratio of the window width; {@code 0 < xRatio < 1}
	 * @param yRatio y position of the top of this box as as ratio of the window width; {@code 0 < xRatio < 1}
	 * @param onConfirm code to invoke when confirmed
	 * @param onDecline code to invoke when cancelled
	 */
	public ConfirmationBox(
		SCNuklear nuklear , 
		final String title , 
		final String description ,
		final float xRatio , 
		final float yRatio , 
		final Runnable onConfirm , 
		final Runnable onDecline
	) {
	
		this.nuklear = nuklear;
		ui = new SCUserInterface(nuklear , title , xRatio , yRatio , width , height);
		ui.flags |= UI_BORDERED|UI_TITLED|UI_UNSCROLLABLE;

		ui.new SCDynamicRow(45).new SCText(description);
		
		SCDynamicRow row = ui.new SCDynamicRow(30);
		row.new SCButton("Confirm" , () -> {
			
			onConfirm.run();
			shutDown();
			
		});
		
		row.new SCButton("Cancel" , () -> {
			
			onDecline.run();
			shutDown();
			
		});
		
	}
	
	private void shutDown() {

		nuklear.removeUserInterface(ui);
		ui.shutDown();
		super.onFinish();
		
	}

}
