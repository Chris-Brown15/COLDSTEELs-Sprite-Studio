package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.CSRefInt;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;

public class NewVisualLayerMenu {

	private int channelsPerPixel = -1;

	private volatile boolean isFinished = false;
	private final Lambda onFinish;
	
	private final CSTextEditor nameInput;
	
 	private final CSSSProject project;
	
	public NewVisualLayerMenu(CSSSProject project , CSNuklear nuklear) {

		CSUserInterface ui = nuklear.new CSUserInterface("New Visual Layer" , 0.5f - (0.33f / 2) , 0.5f - (0.46f / 2) , 0.33f , 0.46f);
		ui.options = UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			isFinished = true;
			
		};
		
		this.project = project;

		channelsPerPixel = project.channelsPerPixel();
		
		CSDynamicRow bpcDisplay = ui.new CSDynamicRow();
		bpcDisplay.new CSText("Bytes Per Channel");

		CSDynamicRow cppDisplay = ui.new CSDynamicRow();
		cppDisplay.new CSText("Channels per Pixel");
		cppDisplay.new CSText(() -> "" + channelsPerPixel);

		ui.new CSDynamicRow(40).new CSText("Default values for the channels of this layer:");
		
		CSDynamicRow nameRow = ui.new CSDynamicRow();
		nameRow.new CSText("Layer Name");
		nameInput = nameRow.new CSTextEditor(100);
		
		CSDynamicRow finishAndCancelRow = ui.new CSDynamicRow();
		finishAndCancelRow.new CSButton("Finish" , this::finish);
		finishAndCancelRow.new CSButton("Cancel" , onFinish);
		
	}
	
	private void finish() {
		
		String nameInputString = nameInput.toString();
		
		CSRefInt doNamesMatch = new CSRefInt(0);
		
		if(nameInputString.equals("")) return;
		
		project.forEachVisualLayerPrototype(prototype -> {
		
			if(prototype.name().equals(nameInputString)) { 
				
				doNamesMatch.set(1);
				return;
				
			}
			
		});
		
		if(doNamesMatch.intValue() == 1) return;
		
		onFinish.invoke();
		
	}

	public boolean isFinished() {
		
		return isFinished;
		
	}
	
	public boolean canCreate() {
		
		return !nameInput.toString().equals("");
		
	}
	
	public String name() {
		
		return nameInput.toString();
		
	}

}
