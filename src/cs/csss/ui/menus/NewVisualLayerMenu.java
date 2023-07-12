package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSCheckBox;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.CSRefInt;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;
import cs.csss.project.VisualLayerPrototype;

public class NewVisualLayerMenu {

	private int channelsPerPixel = -1;

	private volatile boolean isFinished = false;
	private VisualLayerPrototype newPrototype;
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

		CSCheckBox box = ui.new CSDynamicRow(20).new CSCheckBox("Paletted Rendering" , project.paletted() , () -> {});
		
		ui.attachedLayout((context , stack) -> {
			
			if(project.paletted()) box.check();
		
		});
		
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
		
		newPrototype = new VisualLayerPrototype(nameInputString);
		onFinish.invoke();
		
	}

	public boolean isFinished() {
		
		return isFinished;
		
	}
	
	public VisualLayerPrototype get() {
		
		return newPrototype;
		
	}

}
