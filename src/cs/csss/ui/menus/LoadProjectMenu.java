package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import java.util.LinkedList;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.misc.files.CSFolder;

/**
 * UI menu for loading a project from disk.
 */
public class LoadProjectMenu extends Dialogue {

	private String selected = null;
	private String[] entries;
	
	private boolean readyToFinish = false;	
	
	/**
	 * Creates a load project menu.
	 * 
	 * @param nuklear — the Nuklear factory
	 */
	public LoadProjectMenu(CSNuklear nuklear) {

		CSUserInterface ui = nuklear.new CSUserInterface("Load Project" , 0.5f - (0.33f / 2) , 0.5f - (0.35f / 2) , 0.33f , 0.35f);
		
		ui.options = UI_TITLED|UI_BORDERED;
		
		// load names of projects
		
		CSFolder projects = CSFolder.getRoot("data").getOrCreateSubdirectory("projects");
		System.out.println(projects.asFile().exists());
		
		var subdirectories = projects.filesIterator();
	 	entries = new String[projects.files().size()];
	 	System.out.println(projects.files().size());
	 	int i = 0;
	 	while(subdirectories.hasNext()) entries[i++] = subdirectories.next().name(); 
	 	
		Lambda onFinish = () -> {
	 		
			nuklear.removeUserInterface(ui);
	 		ui.shutDown();
	 		readyToFinish = true;
	 		super.onFinish();
	 		
	 	};
		
		LinkedList<CSRadio> radios = new LinkedList<>();
		
		for(String x : entries) radios.add(ui.new CSDynamicRow(20).new CSRadio(x , false , () -> selected = x));
		
		CSRadio.groupAll(radios.toArray(CSRadio[]::new));
		
	 	CSDynamicRow bottomRow = ui.new CSDynamicRow();
		
	 	bottomRow.new CSButton("Finish" , () -> {
	 	
	 		if(selected == null) return;
	 		onFinish.invoke();
	 		
	 	});
	 	
	 	bottomRow.new CSButton("Cancel" , () -> {
	 		
	 		selected = null;
	 		onFinish.invoke();
	 		
	 	});
	 	
	}
	
	/**
	 * Returns the selected project.
	 * 
	 * @return The selected project
	 */
	public String get() {
		
		return selected;
		
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
