package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.UI_TITLED;
import static sc.core.ui.SCUIConstants.UI_BORDERED;

import cs.csss.project.CSSSProject;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu for creating a new nonvisual layer prototype. 
 */
public class NewNonVisualLayerMenu extends Dialogue {
	
	private volatile boolean isFinished = false;
	
	private final Runnable onFinish;
	
	private int channels = -1;
	private String name;
	
	private final SCTextEditor nameInput;

	/**
	 * Creates a new nonvisual layer prototype menu.
	 * 
	 * @param project the project to add the prototype to
	 * @param nuklear the Nuklear factory
	 */
	public NewNonVisualLayerMenu(CSSSProject project , SCNuklear nuklear) {
	
		SCUserInterface ui = new SCUserInterface(
			nuklear , 
			"New Nonvisual Layer" , 
			0.5f - (0.33f / 2) , 
			0.5f - (0.177f / 2) , 
			0.33f , 
			0.177f
		);
		
		ui.flags = UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			isFinished = true;
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			super.onFinish();
			
		};
		
		ui.new SCDynamicRow(20).new SCText("Size in bytes of each \"pixel:\"");
		
 		SCDynamicRow sizeRow = ui.new SCDynamicRow();
		SCRadio oneByte = sizeRow.new SCRadio("One Byte" , false , () -> channels = 1);
		SCRadio twoBytes = sizeRow.new SCRadio("Two Bytes" , false , () -> channels = 2);
		SCRadio threeBytes = sizeRow.new SCRadio("Three Bytes" , false , () -> channels = 3);
		SCRadio fourBytes = sizeRow.new SCRadio("Four Bytes" , false , () -> channels = 4);
		
		SCRadio.groupAll(oneByte , twoBytes , threeBytes , fourBytes);
		
		SCDynamicRow nameInputRow = ui.new SCDynamicRow();
		nameInputRow.new SCText("Layer Name");
		nameInput = nameInputRow.new SCTextEditor(100);
		
 		SCDynamicRow finishCancel = ui.new SCDynamicRow();
 		finishCancel.new SCButton("Finish" , this::tryFinish);
 		finishCancel.new SCButton("Cancel" , onFinish);
 	 		
	}
	
	private void tryFinish() {
		
		if(channels == -1) return;
		name = nameInput.toString();
		if(name.equals("")) return;
		
		onFinish.run();
		
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
