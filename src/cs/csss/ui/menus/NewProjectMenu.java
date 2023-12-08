package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap_colored;

import java.util.Iterator;
import java.util.LinkedList;

import org.lwjgl.nuklear.NkColor;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;

/**
 * UI menu for creating a new project.
 */
public class NewProjectMenu extends Dialogue {

	private static final LinkedList<String> existingProjects = new LinkedList<>();
	
	static {
		
		Engine.THE_THREADS.submit(() -> {
			
			CSFolder projectsFolder = CSFolder.getRoot("data").getOrCreateSubdirectory("projects");
			projectsFolder.seekExistingFiles();
		 	Iterator<CSFile> projects = projectsFolder.filesIterator();					
		 	while(projects.hasNext()) existingProjects.add(projects.next().name());
		 	
		});
		
	}
	
	private volatile boolean canFinish = false;

	private volatile String projectName;
	private final CSUserInterface ui;
	private final CSTextEditor textInput;

	private final Lambda removeUIOnFinish;

	private int channelsPerPixel = 4;

	/**
	 * Creates a new project menu.
	 * 
	 * @param nuklear — the Nuklear factory
	 */
	public NewProjectMenu(final CSNuklear nuklear) {

		this.ui = nuklear.new CSUserInterface("New Project" , 0.5f - (0.33f / 2) , 0.5f - (0.245f / 2) , 0.33f , 0.245f);
		ui.options = UI_TITLED|UI_BORDERED;

		CSDynamicRow nameRow = ui.new CSDynamicRow(30);
		nameRow.new CSText("Project Name:" , TEXT_CENTERED|TEXT_LEFT);
		textInput = nameRow.new CSTextEditor(100);
		
		ui.new CSDynamicRow(20).new CSText("Select Number of Channels:" , TEXT_CENTERED|TEXT_LEFT);		
		CSRadio oneChannel = ui.new CSDynamicRow(25).new CSRadio("Grayscale" , () -> channelsPerPixel == 1 , () -> channelsPerPixel = 1);
		CSRadio twoChannels = ui.new CSDynamicRow(25).new CSRadio("Grayscale + Alpha" , () -> channelsPerPixel == 2 , () -> channelsPerPixel = 2);
		CSRadio threeChannels = ui.new CSDynamicRow(25).new CSRadio("RGB" , () -> channelsPerPixel == 3 , () -> channelsPerPixel = 3);
		CSRadio fourChannels = ui.new CSDynamicRow(25).new CSRadio("RGB + Alpha" , () -> channelsPerPixel == 4, () -> channelsPerPixel = 4);
		
		channelsPerPixel = 4;
		
		CSRadio.groupAll(oneChannel , twoChannels , threeChannels , fourChannels);
		
	 	ui.attachedLayout((context , stack) -> {
	 		
	 		for(String x : existingProjects) if(x.equals(textInput.toString())) {
	 			
	 			nk_layout_row_dynamic(context , 40 , 1);
	 			NkColor red = NkColor.malloc(stack).set((byte)-1 , (byte)0 , (byte)0 , (byte)-1);
	 			String warningText = x + " already names a project. Overwrites and errors may occur if this name is chosen." ;
	 			nk_text_wrap_colored(context , warningText, red);
	 			
	 		}
	 		
	 	});
	 	
	 	CSDynamicRow finishAndCancelRow = ui.new CSDynamicRow();
	 	finishAndCancelRow.new CSButton("Finish" , this::finish);
	 	finishAndCancelRow.new CSButton("Cancel" , this::cancel);
	 	
	 	removeUIOnFinish = () -> {
	 		
	 		nuklear.removeUserInterface(ui);
	 		ui.shutDown();
	 		super.onFinish();
	 		
	 	};
		
	}
	
	/**
	 * Attempts to finish, but may not! If the given name is satisfactory, this UI will be finished.
	 */
	private void finish() {
		
		String input = textInput.toString();
		if(channelsPerPixel != -1) {
			
			projectName = input;
			removeUIOnFinish.invoke();
			canFinish = true;
			existingProjects.add(input);
			
		}
		
	}
	
	/**
	 * Returns the selected channels per pixel of the project.
	 * 
	 * @return Channels per pixel of the project.
	 */
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}

	private void cancel() {
		
		canFinish = true;
		removeUIOnFinish.invoke();
		
	}
	
	/**
	 * Returns whether this menu can finish based on its current state.
	 * 
	 * @return Whether this menu can finish based on its current state.
	 */
	public boolean canFinish() {
		
		return canFinish;
		
	}
	
	/**
	 * Returns the name of the project.
	 * 
	 * @return Name of the project.
	 */
	public String get() {
		
		return projectName;
		
	}

}
