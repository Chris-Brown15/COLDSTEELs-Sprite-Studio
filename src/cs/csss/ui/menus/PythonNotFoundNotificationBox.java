/**
 * 
 */
package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSExtendedText;
import cs.csss.ui.utils.UIUtils;
import cs.core.ui.CSNuklear.CSUserInterface;

/**
 * UI menu used to notify users that Python was not installed on their computer, and offers them a link to download Python.
 */
public class PythonNotFoundNotificationBox {

	private static final float 
		widthRatio = 0.3f ,
		heightRatio = 0.4f;
	
	private final CSUserInterface ui;
	private final String 
		link = "https://www.python.org/downloads/" ,
		message = "Invoking scripts requires Python 3.5 or newer to be installed, and it was not found at startup. You can "
		+ "install it at: " + link;
	
	public PythonNotFoundNotificationBox(CSNuklear nuklear , int windowHeight) {
		
		ui = nuklear.new CSUserInterface("Python Not Found" , widthRatio , heightRatio , 0.5f - (widthRatio / 2f) , 0.5f - (heightRatio / 2f));
		ui.options |= UI_BORDERED|UI_TITLED;

		int length = UIUtils.textLength(message);
				
		int realHeight = (int) (heightRatio * windowHeight);
		
		int height = (int) Math.ceil((length / realHeight)) * 20;
		CSExtendedText text = ui.new CSDynamicRow(height + 20).new CSExtendedText(message);

		text.colorLast(link, 0x5555ffff);
		if(Desktop.isDesktopSupported()) {
			
			text.makeLastClickable(link , () -> {
				
				try {
					
					Desktop.getDesktop().browse(new URI(link));
					
				} catch (IOException e) {

					e.printStackTrace();
					
				} catch (URISyntaxException e) {

					e.printStackTrace();
					
				}
				
			});

		}
		
		ui.new CSDynamicRow().new CSButton("Finish" , () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		});
		
	}
	
}