package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap_colored;

import java.io.File;
import java.util.LinkedList;

import org.lwjgl.nuklear.NkColor;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSCheckBox;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.core.Engine;
import cs.csss.project.CSSSProject;

public class NewProjectMenu {

	private static final int 
		ONE_CHANNEL 	= 1 ,
		TWO_CHANNELS 	= 2 ,
		THREE_CHANNELS 	= 3 ,
		FOUR_CHANNELS 	= 4 
	;
	
	private static final LinkedList<String> existingProjects = new LinkedList<>();
	
	static {
		
		Engine.THE_THREADS.async(() -> {
			
			File[] projects = new File(CSSSProject.dataDir).listFiles();
			for(File x : projects) existingProjects.add(x.getName());			
						
		});
		
	}
	
	private volatile boolean canFinish = false;

	private volatile String projectName;
	private final CSUserInterface ui;
	private final CSTextEditor textInput;

	private final CSCheckBox palettedCheck;
	
	private final Lambda removeUIOnFinish;

	private int channelsPerPixel = -1;

	public NewProjectMenu(final CSNuklear nuklear) {

		this.ui = nuklear.new CSUserInterface("New Project" , 0.5f - (0.33f / 2) , 0.5f - (0.35f / 2) , 0.33f , 0.35f);
		ui.options = UI_TITLED|UI_BORDERED;

		palettedCheck = ui.new CSDynamicRow(20).new CSCheckBox("Paletted Rendering" , false , () -> {});
		
		ui.new CSDynamicRow(20).new CSText("Select Number of Channels:");
		CSDynamicRow channelsSelector = ui.new CSDynamicRow();
		CSRadio 
			oneChannel = channelsSelector.new CSRadio("Grayscale" , false , () -> channelsPerPixel = ONE_CHANNEL) ,
			twoChannels = channelsSelector.new CSRadio("Grayscale + Alpha" , false , () -> channelsPerPixel = TWO_CHANNELS) ,
			threeChannels = channelsSelector.new CSRadio("RGB" , false , () -> channelsPerPixel = THREE_CHANNELS) ,
			fourChannels = channelsSelector.new CSRadio("RGB + Alpha" , true , () -> channelsPerPixel = FOUR_CHANNELS)
		;
		
		CSRadio.groupAll(oneChannel , twoChannels , threeChannels , fourChannels);
				
		//not sure why the check is 
		fourChannels.uncheck();
		
		CSDynamicRow nameRow = ui.new CSDynamicRow();
		nameRow.new CSText("Project Name:");
		textInput = nameRow.new CSTextEditor(100);

	 	ui.attachedLayout((context , stack) -> {
	 		
	 		for(String x : existingProjects) if(x.equals(textInput.toString())) {
	 			
	 			nk_layout_row_dynamic(context , 40 , 1);
	 			NkColor red = NkColor.malloc(stack).set((byte)-1 , (byte)0 , (byte)0 , (byte)-1);
	 			nk_text_wrap_colored(
	 				context , 
	 				x + " already names a project. Overwrites and errors may occur if this name is chosen." , 
	 				red
	 			);
	 			
	 		}
	 		
	 	});
	 	
	 	CSDynamicRow finishAndCancelRow = ui.new CSDynamicRow();
	 	finishAndCancelRow.new CSButton("Finish" , this::finish);
	 	finishAndCancelRow.new CSButton("Cancel" , this::cancel);
	 	
	 	removeUIOnFinish = () -> {
	 		
	 		nuklear.removeUserInterface(ui);
	 		ui.shutDown();
	 		
	 	};
		
	}
	
	/**
	 * Attempts to finish, but may not! If the given name is satisfactory, this UI will be finished.
	 */
	private void finish() {
		
		String input = textInput.toString();
		if(CSSSProject.isValidProjectName(input) && channelsPerPixel != -1) {
			
			projectName = input;
			removeUIOnFinish.invoke();
			canFinish = true;
			existingProjects.add(input);
			
		}
		
	}
	
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}

	private void cancel() {
		
		canFinish = true;
		removeUIOnFinish.invoke();
		
	}
	
	public boolean paletted() {
		
		return palettedCheck.checked();
		
	}
	
	public boolean canFinish() {
		
		return canFinish;
		
	}
	
	public String get() {
		
		return projectName;
		
	}

}
