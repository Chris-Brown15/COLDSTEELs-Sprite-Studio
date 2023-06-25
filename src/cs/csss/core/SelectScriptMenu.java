package cs.csss.core;

import static cs.core.ui.CSUIConstants.*;

import java.awt.Desktop;
import java.io.IOException;
import java.util.LinkedList;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSButton;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.Directory;

public class SelectScriptMenu {

	private CSFile selectedFile;
	private CSUserInterface ui;
	
	public SelectScriptMenu(CSNuklear nuklear) {

		ui = nuklear.new CSUserInterface("Select a script" , 0.5f - (0.33f / 2) , 0.5f - (0.33f / 2) , 0.33f , 0.33f);
		ui.options = UI_TITLED|UI_BORDERED;
		
		Directory scriptsDir = Directory.getRoot("program").getSubdirectory("scripts");
		
		scriptsDir.seekExistingFiles();
		
		LinkedList<CSRadio> radios = new LinkedList<>();
		
		scriptsDir.files().forEachRemaining(file -> {
			
			CSRow row = ui.new CSRow(30);
			row.pushWidth(0.66f);
			row.pushWidth(0.25f);
			CSRadio radio = row.new CSRadio(file.name() , false , () -> selectedFile = file);
			CSButton openButton = row.new CSButton("Open" , () -> {
				
				try {
					
					Desktop.getDesktop().open(file.asFile());
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			});
			
			openButton.doLayout = Desktop::isDesktopSupported;
			
			radios.add(radio);
			
		});
		
		CSRadio[] radiosArray = new CSRadio[radios.size()];
		radios.toArray(radiosArray);
		CSRadio.groupAll(radiosArray);
		
		Lambda onFinish = () -> {
			
			ui.shutDown();
			nuklear.removeUserInterface(ui);			
			
		};
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		
		finishRow.new CSButton("Execute" , onFinish);
		finishRow.new CSButton("Cancel" , onFinish);
		
	}
	
	public boolean readyToFinish() {
		
		return ui.isFreed();
		
	}

	CSFile selectedScript() {
		
		return selectedFile;
		
	}
	
}
