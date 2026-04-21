package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import cs.csss.ui.utils.UIUtils;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

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
	 * @param title title of this notification box
	 * @param message message of this notification box
	 * @param nuklear the Nuklear factory
	 * @param onOK invoked when the user presses the OK button.
	 */
	public NotificationBox(String title , String message , SCNuklear nuklear , Runnable onOK) {

		SCUserInterface ui = new SCUserInterface(nuklear , title , .4f , .4f , width , height);
		
		ui.flags = UI_BORDERED|UI_TITLED;
		
		int length = UIUtils.textLength(message);
				
		int realHeight = height;
		
		int height = (int) Math.ceil((length / realHeight)) * 20;
				
		ui.new SCDynamicRow(height).new SCText(message);
		
		ui.new SCDynamicRow().new SCButton("Okay" , () -> {
			
			if(onOK != null) onOK.run();
			ui.shutDown();
			nuklear.removeUserInterface(ui);
			super.onFinish();
			
		});		
		
	}

	/**
	 * Creates a new notification box.
	 * 
	 * @param title title of this notification box
	 * @param message message of this notification box
	 * @param nuklear the Nuklear factory
	 */
	public NotificationBox(String title , String message , SCNuklear nuklear) {
		
		this(title , message , nuklear , null);
		
	}

}
