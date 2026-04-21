package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import java.util.LinkedList;

import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFolder;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu for loading a project from disk.
 */
public class LoadProjectMenu extends Dialogue {

	private String selected = null;	
	private boolean readyToFinish = false;	
	
	/**
	 * Creates a load project menu.
	 * 
	 * @param nuklear the Nuklear factory
	 */
	public LoadProjectMenu(SCNuklear nuklear) {

		SCUserInterface ui = new SCUserInterface(nuklear , "Load Project" , 0.5f - (0.33f / 2) , 0.5f - (0.35f / 2) , 0.33f , 0.35f);
		
		ui.flags = UI_TITLED|UI_BORDERED;
		
		// load names of projects
		
		CSFolder projects = CSFolder.getRoot("data").getOrCreateSubdirectory("projects");		
		var subdirectories = projects.filesIterator();
		String[] entries = new String[projects.files().size()];
	 	
	 	int i = 0;
	 	while(subdirectories.hasNext()) entries[i++] = subdirectories.next().name(); 
	 	
		Runnable onFinish = () -> {
	 		
			nuklear.removeUserInterface(ui);
	 		readyToFinish = true;
			Engine.THE_TEMPORAL.onTrue(() -> true, ui::shutDown);
	 		super.onFinish();
	 		
	 	};
		
		LinkedList<SCRadio> radios = new LinkedList<>();
		
		for(String x : entries) radios.add(ui.new SCDynamicRow(20).new SCRadio(x , false , () -> selected = x));
		
		SCRadio.groupAll(radios.toArray(SCRadio[]::new));
		
	 	SCDynamicRow bottomRow = ui.new SCDynamicRow();
		
	 	bottomRow.new SCButton("Finish" , () -> {
	 	
	 		if(selected == null) return;
	 		onFinish.run();
	 		
	 	});
	 	
	 	bottomRow.new SCButton("Cancel" , () -> {
	 		
	 		selected = null;
	 		onFinish.run();
	 		
	 	});
	 	
	}
	
	/**
	 * Returns the selected project's name.
	 * 
	 * @return The selected project's name.
	 */
	public String get() {
		
		if(selected != null) {

			int extensionDotIndex = selected.lastIndexOf('.');
			if(extensionDotIndex != -1) return selected.substring(0 , extensionDotIndex);		
			
		}

		return selected;
				
	}
	
	/**
	 * Returns the extension of the file named {@link #get()}, or <code>null</code> if either no file was selected or the selected file did not appear to
	 * have an extension.
	 *  
	 * @return File extension of the returned file if any was found, or <code>null</code>.
	 */
	public String extension() {
		
		int extensionDotIndex;
		if(selected == null || (extensionDotIndex = selected.lastIndexOf('.')) == -1) return null;
		
		return selected.substring(extensionDotIndex);
		
	}
	
	/**
	 * Returns whether the UI is in a state to finish.
	 * 
	 * @return {@code true} if this UI is ready to finish.
	 */
	public boolean readyToFinish() {
		
		return readyToFinish;
		
	}

}
