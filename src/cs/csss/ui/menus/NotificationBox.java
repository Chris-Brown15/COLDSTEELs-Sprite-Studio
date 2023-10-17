package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.csss.ui.utils.UIUtils;

/**
 * UI menu for notifying the user of something. 
 */
public class NotificationBox extends Dialogue {

	/**
	 * Constant width and height of notification boxes.
	 */
	public static final int
		width = 250 ,
		height = 130;
	
	/**
	 * Creates a new notification box.
	 * 
	 * @param title � title of this notification box
	 * @param message � message of this notification box
	 * @param nuklear � the Nuklear factory
	 */
	public NotificationBox(final String title , final String message , CSNuklear nuklear) {

		CSUserInterface ui = nuklear.new CSUserInterface(title , .4f , .4f , width , height);
		
		ui.options = UI_BORDERED|UI_TITLED;
		
		int length = UIUtils.textLength(message);
				
		int realHeight = height;
		
		int height = (int) Math.ceil((length / realHeight)) * 17;
		
		ui.new CSDynamicRow(height).new CSText(message);
		
		ui.new CSDynamicRow().new CSButton("Okay" , () -> {
			
			ui.shutDown();
			nuklear.removeUserInterface(ui);
			super.onFinish();
			
		});		
		
	}

}
