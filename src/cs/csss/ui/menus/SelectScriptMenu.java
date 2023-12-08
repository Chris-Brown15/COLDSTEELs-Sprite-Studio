package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSButton;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.editor.ScriptType;
import cs.csss.misc.files.CSFolder;
import cs.csss.steamworks.WorkshopDownloadHelper;

/**
 * UI menu used to select a script. This is used for all instances of selecting a script.
 */
public class SelectScriptMenu extends Dialogue {

	private File selectedFile;
	private CSUserInterface ui;
	
	/**
	 * Creates a new select script menu.
	 * 
	 * @param nuklear — the Nuklear factory
	 * @param scriptDirectory — directory to look in for script files to choose from
	 */
	public SelectScriptMenu(CSNuklear nuklear , String scriptDirectory) {

		ui = nuklear.new CSUserInterface("Select a script" , 0.5f - (0.33f / 2) , 0.5f - (0.33f / 2) , 0.33f , 0.33f);
		ui.options = UI_TITLED|UI_BORDERED;
		
		CSFolder scriptsDir = CSFolder.getRoot("program").getOrCreateSubdirectory("scripts").getOrCreateSubdirectory(scriptDirectory);
		File scriptsDirFile = scriptsDir.asFile();
		
		scriptsDir.seekExistingFiles();
		
		ArrayList<CSRadio> radios = new ArrayList<>();
		File[] files = scriptsDirFile.listFiles((file , name) -> name.endsWith(".py"));
		for(File x : files) newRadio(x , radios);
		
		ScriptType typeFromDirectoryName = ScriptType.getTypeFromDirectoryName(scriptDirectory);
		String asTag = typeFromDirectoryName.asTagName();
		
		//check with the workshop scripts here
		//only include an item from the workshop if it does not match a file in the local installation. Prefer to show locally installed versions
		WorkshopDownloadHelper.forEachDownload(item -> {
			
			if(item.hasTag(asTag)) {
				
				File script = new File(item.folder()).listFiles((director , name) -> name.endsWith(".py"))[0];
				for(File x : files) if(x.getName().equals(script.getName())) return;
				newRadio(script , radios);
				
			}			
			
		});
		
		CSRadio[] radiosArray = new CSRadio[radios.size()];
		radios.toArray(radiosArray);
		CSRadio.groupAll(radiosArray);
		
		Lambda onFinish = () -> {
			
			ui.shutDown();
			nuklear.removeUserInterface(ui);	
			super.onFinish();
			
		};
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		
		finishRow.new CSButton("Execute" , onFinish);
		finishRow.new CSButton("Cancel" , () -> {
			
			selectedFile = null; 
			onFinish.invoke();
			
		});
		
	}
	
	/**
	 * Returns whether this UI is ready to finish.
	 * 
	 * @return Whether this UI is ready to finish.
	 */
	public boolean readyToFinish() {
		
		return ui.isFreed();
		
	}

	/**
	 * Returns the file containing the selected script.
	 * 
	 * @return File containing the selected script.
	 */
	public File selectedScript() {
		
		return selectedFile;
		
	}
	
	private void newRadio(File file , List<CSRadio> radios) {
		
		CSRow row = ui.new CSRow(30);
		row.pushWidth(0.66f);
		row.pushWidth(0.25f);
		CSRadio radio = row.new CSRadio(file.getName() , false , () -> selectedFile = file);
		CSButton openButton = row.new CSButton("Open" , () -> {
			
			try {
				
				Desktop.getDesktop().open(file);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		});
		
		openButton.doLayout = Desktop::isDesktopSupported;
		
		radios.add(radio);
		
	}
	
}
