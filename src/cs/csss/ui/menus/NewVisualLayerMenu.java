package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.CSRefInt;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;

/**
 * UI menu for creating a new visual layer prototype.
 */
public class NewVisualLayerMenu extends Dialogue {

	private volatile boolean isFinished = false;
	private final Lambda onFinish;
	
	private final CSTextEditor nameInput;
	
 	private final CSSSProject project;
	
 	/**
 	 * Creates a new visual layer prototype menu.
 	 * 
 	 * @param project — the project to whom the new visual layer prototype will belong
 	 * @param nuklear — the Nuklear factory
 	 */
	public NewVisualLayerMenu(CSSSProject project , CSNuklear nuklear) {

		CSUserInterface ui = nuklear.new CSUserInterface("New Visual Layer" , 0.5f - (0.33f / 2) , 0.5f - (0.12f / 2) , 0.33f , 0.12f);
		ui.options = UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			isFinished = true;
			super.onFinish();
			
		};
		
		this.project = project;

		CSDynamicRow nameRow = ui.new CSDynamicRow();
		nameRow.new CSText("Layer Name", TEXT_CENTERED|TEXT_LEFT);
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

	/**
	 * Returns whether this menu can finish based on its current state.
	 * 
	 * @return Whether this menu can finish based on its current state.
	 */
	public boolean isFinished() {
		
		return isFinished;
		
	}
	
	/**
	 * Returns whether a new visual layer prototype can be created.
	 * 
	 * @return Whether a new visual layer prototype can be created.
	 */
	public boolean canCreate() {
		
		return !nameInput.toString().equals("");
		
	}
	
	/**
	 * Returns the name of the new visual layer prototype.
	 * 
	 * @return Name of the new visual layer prototype.
	 */
	public String name() {
		
		return nameInput.toString();
		
	}

}
