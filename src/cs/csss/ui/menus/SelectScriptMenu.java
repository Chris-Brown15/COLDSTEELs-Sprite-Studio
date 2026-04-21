package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cs.csss.editor.ScriptType;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFolder;
import cs.csss.steamworks.WorkshopDownloadHelper;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCButton;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUI.SCRow;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu used to select a script. This is used for all instances of selecting a script.
 */
public class SelectScriptMenu extends Dialogue {

	private File selectedFile;
	private SCUserInterface ui;
	
	/**
	 * Creates a new select script menu.
	 * 
	 * @param nuklear the Nuklear factory
	 * @param scriptDirectory directory to look in for script files to choose from
	 */
	public SelectScriptMenu(SCNuklear nuklear , String scriptDirectory) {

		ui = new SCUserInterface(nuklear , "Select a script" , 0.5f - (0.33f / 2) , 0.5f - (0.33f / 2) , 0.33f , 0.33f);
		ui.flags = UI_TITLED|UI_BORDERED;
		
		CSFolder scriptsDir = CSFolder.getRoot("program").getOrCreateSubdirectory("scripts").getOrCreateSubdirectory(scriptDirectory);
		
		File scriptsDirFile = scriptsDir.asFile();

		scriptsDir.seekExistingFiles();

		ArrayList<SCRadio> radios = new ArrayList<>();
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
		
		SCRadio[] radiosArray = new SCRadio[radios.size()];
		radios.toArray(radiosArray);
		SCRadio.groupAll(radiosArray);
		
		Runnable onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			Engine.THE_TEMPORAL.onTrue(() -> true, ui::shutDown);
			super.onFinish();
			
		};
		
		SCDynamicRow finishRow = ui.new SCDynamicRow();
		
		finishRow.new SCButton("Execute" , onFinish);
		finishRow.new SCButton("Cancel" , () -> {
			
			selectedFile = null; 
			onFinish.run();
			
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
	
	private void newRadio(File file , List<SCRadio> radios) {
		
		SCRow row = ui.new SCRow(30);
		row.pushWidth(0.66f);
		row.pushWidth(0.25f);
		SCRadio radio = row.new SCRadio(file.getName() , false , () -> selectedFile = file);
		SCButton openButton = row.new SCButton("Open" , () -> {
			
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
