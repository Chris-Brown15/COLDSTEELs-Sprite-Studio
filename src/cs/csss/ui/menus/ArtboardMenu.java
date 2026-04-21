package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import cs.csss.engine.Engine;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;


/**
 * UI menu for creating an artboard or resizing an existing one.
 */
public class ArtboardMenu extends Dialogue {

	public SCUserInterface ui;
	private final Runnable onFinish;
	
	private boolean isFinished = false;

	private int width = -1 , height = -1;
	
	private final SCTextEditor widthEditor , heightEditor;
		
	/**
	 * Creates a new artboard menu.
	 *
	 * @param currentProject the project to add the artboard to
	 * @param nuklear the Nuklear factory 
	 */
	public ArtboardMenu(SCNuklear nuklear , String title) {
		
		ui = new SCUserInterface(nuklear , title , 0.5f - (0.33f / 2) , 0.5f - (0.16f / 2) , 0.33f , 0.16f);
		ui.flags |= UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			Engine.THE_TEMPORAL.onTrue(() -> true, ui::shutDown);
			isFinished = true;
			super.onFinish();
			
		};		
		
		SCDynamicRow widthRow = ui.new SCDynamicRow() , heightRow = ui.new SCDynamicRow();
		
		widthRow.new SCText("Artboard Width" , TEXT_MIDDLE|TEXT_CENTERED);
		widthEditor = widthRow.new SCTextEditor(6 , SCNuklear.DECIMAL_FILTER);
		
		heightRow.new SCText("Artboard Height" , TEXT_MIDDLE|TEXT_CENTERED);
		heightEditor = heightRow.new SCTextEditor(6 , SCNuklear.DECIMAL_FILTER);
			
		SCDynamicRow buttons =  ui.new SCDynamicRow();
		buttons.new SCButton("Finish" , this::tryFinish);
		buttons.new SCButton("Cancel" , onFinish);
		
	}
	
	/**
	 * Returns whether this UI is ready to finish.
	 * 
	 * @return Whether this UI is ready to finish.
	 */
	public boolean finished() {
		
		return isFinished;
		
	}
				
	/**
	 * Returns the width of the new artboard.
	 * 
	 * @return Width of the new artboard.
	 */
	public int width() {
		
		return width;
		
	}
	
	/**
	* Returns the height of the new artboard.
	* 
	* @return Height of the new artboard.
	*/
	public int height() {
		
		return height;
		
	}
	
	private void tryFinish() {
		
		String widthInput = null, heightInput = null;
		
		if((widthInput = widthEditor.toString()).equals("") || (heightInput = heightEditor.toString()).equals("")) return;
		
		width = Integer.parseInt(widthInput);
		height = Integer.parseInt(heightInput);

		if(width < 0 || height < 0) return;
		
		onFinish.run();
		
	}
	
	/**
	 * Returns whether the state of this UI at the time it was finished was such that a new artboard could be created from its variables.
	 * 
	 * @return {@code true} if a new artboard could be created from the input values of this menu.
	 */
	public boolean finishedValidly() {
		
		return width != -1 & height != -1;
		
	}
	
}
