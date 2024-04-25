package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.UI_TITLED;
import static cs.core.ui.CSUIConstants.UI_BORDERED;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;

/**
 * UI menu for creating a new nonvisual layer prototype. 
 */
public class NewNonVisualLayerMenu extends Dialogue {
	
	private volatile boolean isFinished = false;
	
	private final Lambda onFinish;
	
	private int channels = -1;
	private String name;
	
	private final CSTextEditor nameInput;

	/**
	 * Creates a new nonvisual layer prototype menu.
	 * 
	 * @param project — the project to add the prototype to
	 * @param nuklear — the Nuklear factory
	 */
	public NewNonVisualLayerMenu(CSSSProject project , CSNuklear nuklear) {
	
		CSUserInterface ui = nuklear.new CSUserInterface("New Nonvisual Layer" , 0.5f - (0.33f / 2) , 0.5f - (0.177f / 2) , 0.33f , 0.177f);
		ui.options = UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			isFinished = true;
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			super.onFinish();
			
		};
		
		ui.new CSDynamicRow(20).new CSText("Size in bytes of each \"pixel:\"");
		
 		CSDynamicRow sizeRow = ui.new CSDynamicRow();
		CSRadio oneByte = sizeRow.new CSRadio("One Byte" , false , () -> channels = 1);
		CSRadio twoBytes = sizeRow.new CSRadio("Two Bytes" , false , () -> channels = 2);
		CSRadio threeBytes = sizeRow.new CSRadio("Three Bytes" , false , () -> channels = 3);
		CSRadio fourBytes = sizeRow.new CSRadio("Four Bytes" , false , () -> channels = 4);
		
		CSRadio.groupAll(oneByte , twoBytes , threeBytes , fourBytes);
		
		CSDynamicRow nameInputRow = ui.new CSDynamicRow();
		nameInputRow.new CSText("Layer Name");
		nameInput = nameInputRow.new CSTextEditor(100);
		
 		CSDynamicRow finishCancel = ui.new CSDynamicRow();
 		finishCancel.new CSButton("Finish" , this::tryFinish);
 		finishCancel.new CSButton("Cancel" , onFinish);
 	 		
	}
	
	private void tryFinish() {
		
		if(channels == -1) return;
		name = nameInput.toString();
		if(name.equals("")) return;
		
		onFinish.invoke();
		
	}
	
	/**
	 * Returns the number of channels of the project.
	 * 
	 * @return Number of channels of the project.
	 */
	public int channels() {
		
		return channels;
		
	}

	/**
	 * Returns the name of the project.
	 * 
	 * @return Name of the project.
	 */
	public String name() {
		
		return name;
		
	}

	/**
	 * Returns whether the project can be created by the current state of the UI.
	 * 
	 * @return Whether the project can be created by the current state of the UI.
	 */
	public boolean canCreate() {
		
		return channels != -1 && name != null && !name.equals("");
		
	}
	
	/**
	 * Returns whether the UI is finished.
	 * 
	 * @return Whether the UI is finished.
	 */
	public boolean isFinished() {
		
		return isFinished;
		
	}
	
}
