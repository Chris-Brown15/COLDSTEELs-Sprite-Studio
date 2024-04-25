/**
 * 
 */
package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.ui.utils.UIUtils;

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
		CSNuklear nuklear , 
		String title , 
		String message , 
		Lambda onOK
	) {
		
		String[] lines = message.split("\n");
		
		int max = 0;
		for(String x : lines) {
			
			int length = UIUtils.textLength(x);
			if(length > max) max = length;
			
		}
		
		int height = 90 + (23 * lines.length);
		CSUserInterface ui = nuklear.new CSUserInterface(title , .4f  , .4f , max , height);
		ui.options = UI_BORDERED|UI_TITLED|UI_UNSCROLLABLE;
		
		for(String x : lines) ui.new CSDynamicRow(20).new CSText(x , TEXT_LEFT|TEXT_MIDDLE);
		ui.new CSDynamicRow().new CSButton("Okay" , () -> {
			
			if(onOK != null) onOK.invoke();
			super.onFinish();
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		});
		
	}

}
