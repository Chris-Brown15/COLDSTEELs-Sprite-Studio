package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.UI_TITLED;
import static cs.core.ui.CSUIConstants.UI_BORDERED;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;
import cs.csss.project.NonVisualLayerPrototype;

public class NewNonVisualLayerMenu {
	
	private volatile boolean isFinished = false;
	
	private final Lambda onFinish;
	
	private int pixelSize = -1;
	
	private final CSTextEditor nameInput;
	
	private NonVisualLayerPrototype prototype;
	
	public NewNonVisualLayerMenu(CSSSProject project , CSNuklear nuklear) {
	
		CSUserInterface ui = nuklear.new CSUserInterface("New Nonvisual Layer" , 0.5f - (0.33f / 2) , 0.5f - (0.25f / 2) , 0.33f , 0.25f);
		ui.options = UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			isFinished = true;
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		};
		
		ui.new CSDynamicRow(20).new CSText("Size in bytes of each \"pixel\"");
		
 		CSDynamicRow sizeRow = ui.new CSDynamicRow();
		sizeRow.new CSRadio("One Byte" , false , () -> pixelSize = 1);
		sizeRow.new CSRadio("Two Bytes" , false , () -> pixelSize = 2);
		sizeRow.new CSRadio("Three Bytes" , false , () -> pixelSize = 3);
		sizeRow.new CSRadio("Four Bytes" , false , () -> pixelSize = 4);
		
		CSDynamicRow nameInputRow = ui.new CSDynamicRow();
		nameInputRow.new CSText("Layer Name");
		nameInput = nameInputRow.new CSTextEditor(100);
		
 		CSDynamicRow finishCancel = ui.new CSDynamicRow();
 		finishCancel.new CSButton("Finish" , this::tryFinish);
 		finishCancel.new CSButton("Cancel" , onFinish);
 	 		
	}
	
	private void tryFinish() {
		
		if(pixelSize == -1) return;
		String name = nameInput.toString();
		if(name.equals("")) return;
		
		prototype = new NonVisualLayerPrototype(pixelSize , name);
		onFinish.invoke();
		
	}
	
	public NonVisualLayerPrototype get() {
		
		return prototype;
		
	}

	public boolean isFinished() {
		
		return isFinished;
		
	}
	
}
