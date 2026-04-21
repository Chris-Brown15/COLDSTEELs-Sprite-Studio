/**
 * 
 */
package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import cs.csss.ui.utils.UIUtils;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * Different version of {@link NotificationBox} whose width is based on the longest line of text in {@code message}. Lines are separated by the newline 
 * character and are displayed one after another. 
 */
public class LineByLineNotificationBox extends Dialogue {

	/**
	 * Creates a new line by line notification box.
	 * 
	 * @param nuklear the nuklear ui factory
	 * @param title title of the ui 
	 * @param message message displayed on the ui
	 * @param onOK code to invoke when the OK button is pressed
	 */
	public LineByLineNotificationBox(
		SCNuklear nuklear , 
		String title , 
		String message , 
		Runnable onOK
	) {
		
		String[] lines = message.split("\n");
		
		int max = 0;
		for(String x : lines) {
			
			int length = UIUtils.textLength(x);
			if(length > max) max = length;
			
		}
		
		int height = 90 + (23 * lines.length);
		SCUserInterface ui = new SCUserInterface(nuklear , title , .4f  , .4f , max , height);
		ui.flags = UI_BORDERED|UI_TITLED|UI_UNSCROLLABLE;
		
		for(String x : lines) ui.new SCDynamicRow(20).new SCText(x , TEXT_LEFT|TEXT_MIDDLE);
		ui.new SCDynamicRow().new SCButton("Okay" , () -> {
			
			if(onOK != null) onOK.run();
			super.onFinish();
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		});
		
	}

}
