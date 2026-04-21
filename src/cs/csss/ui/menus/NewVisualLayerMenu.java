package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import cs.csss.engine.Engine;
import cs.csss.project.CSSSProject;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;
import sc.core.utils.SCIntReferencer;

/**
 * UI menu for creating a new visual layer prototype.
 */
public class NewVisualLayerMenu extends Dialogue {

	private volatile boolean isFinished = false;
	private final Runnable onFinish;
	
	private final SCTextEditor nameInput;
	
 	private final CSSSProject project;
	
 	/**
 	 * Creates a new visual layer prototype menu.
 	 * 
 	 * @param project the project to whom the new visual layer prototype will belong
 	 * @param nuklear the Nuklear factory
 	 */
	public NewVisualLayerMenu(CSSSProject project , SCNuklear nuklear) {

		SCUserInterface ui = new SCUserInterface(nuklear , "New Visual Layer" , 0.5f - (0.33f / 2) , 0.5f - (0.12f / 2) , 0.33f , 0.12f);
		ui.flags = UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			Engine.THE_TEMPORAL.onTrue(() -> true, ui::shutDown);
			isFinished = true;
			super.onFinish();
			
		};
		
		this.project = project;

		SCDynamicRow nameRow = ui.new SCDynamicRow();
		nameRow.new SCText("Layer Name", TEXT_CENTERED|TEXT_LEFT);
		nameInput = nameRow.new SCTextEditor(100);
		
		SCDynamicRow finishAndCancelRow = ui.new SCDynamicRow();
		finishAndCancelRow.new SCButton("Finish" , this::finish);
		finishAndCancelRow.new SCButton("Cancel" , onFinish);
		
	}
	
	private void finish() {
		
		String nameInputString = nameInput.toString();
		
		SCIntReferencer doNamesMatch = new SCIntReferencer(0);
		
		if(nameInputString.equals("")) return;
		
		project.forEachVisualLayerPrototype(prototype -> {
		
			if(prototype.name().equals(nameInputString)) { 
				
				doNamesMatch.set(1);
				return;
				
			}
			
		});
		
		if(doNamesMatch.get() == 1) return;
		
		onFinish.run();
		
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
