package cs.csss.utils;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.csss.ui.utils.UIUtils;

public class NotificationBox {

	public static final int
		width = 250 ,
		height = 130
	;
	
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
			
		});		
		
	}

}
