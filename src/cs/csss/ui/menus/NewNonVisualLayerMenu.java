package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.UI_TITLED;
import static cs.core.ui.CSUIConstants.UI_BORDERED;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;

public class NewNonVisualLayerMenu {
	
	private volatile boolean isFinished = false;
	
	private final Lambda onFinish;
	
	private int pixelSize = -1;
	private String name;
	
	private final CSTextEditor nameInput;

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
		name = nameInput.toString();
		if(name.equals("")) return;
		
		onFinish.invoke();
		
	}
	
	public int pixelSize() {
		
		return pixelSize;
		
	}
	
	public String name() {
		
		return name;
		
	}

	public boolean canCreate() {
		
		return pixelSize != -1 && !name.equals("");
		
	}
	
	public boolean isFinished() {
		
		return isFinished;
		
	}
	
}
