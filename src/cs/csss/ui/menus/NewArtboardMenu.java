package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSExtendedText;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.project.CSSSProject;

public class NewArtboardMenu {

	public CSUserInterface ui;
	private final Lambda onFinish;
	
	private boolean isFinished = false;

	private int 
		width = -1 ,
		height = -1
	;
	
	private final CSTextEditor    
		widthEditor ,
		heightEditor
	;
		
	public NewArtboardMenu(CSSSProject currentProject , CSNuklear nuklear) {
		
		ui = nuklear.new CSUserInterface("New Artboard" , 0.5f - (0.33f / 2) , 0.5f - (0.25f / 2) , 0.33f , 0.25f);
		ui.options |= UI_TITLED|UI_BORDERED;
		
		onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			isFinished = true;
			
		};		
		
	 	CSDynamicRow warningRow = ui.new CSDynamicRow(20);
		CSExtendedText noVisualLayerWarning = warningRow.new CSExtendedText("Warning: No Visual Layer has been created for this project.");
	 	
		CSDynamicRow warningRow2 = ui.new CSDynamicRow(20);
		warningRow2.new CSText("A default will be created for this artboard and all others.");
		
	 	noVisualLayerWarning.colorFirst("Warning:" , 0xffee00ff);
		
	 	warningRow.doLayout = () -> currentProject.visualLayerPrototypeSize() == 0;
	 	warningRow2.doLayout = () -> currentProject.visualLayerPrototypeSize() == 0;
	 	
		CSDynamicRow 
			widthRow = ui.new CSDynamicRow() ,
			heightRow = ui.new CSDynamicRow()
		;
		
		widthRow.new CSText("Artboard Width" , TEXT_MIDDLE|TEXT_CENTERED);
		widthEditor = widthRow.new CSTextEditor(6 , CSNuklear.DECIMAL_FILTER);
		
		heightRow.new CSText("Artboard Height" , TEXT_MIDDLE|TEXT_CENTERED);
		heightEditor = heightRow.new CSTextEditor(6 , CSNuklear.DECIMAL_FILTER);
			
		CSDynamicRow buttons =  ui.new CSDynamicRow();
		buttons.new CSButton("Finish" , this::tryFinish);
		buttons.new CSButton("Cancel" , onFinish);
		
	}
	
	public boolean isFinished() {
		
		return isFinished;
		
	}
				
	public int width() {
		
		return width;
		
	}
	
	public int height() {
		
		return height;
		
	}
	
	private void tryFinish() {
		
		String 
			widthInput = null, 
			heightInput = null
		;
		
		if((widthInput = widthEditor.toString()).equals("") || (heightInput = heightEditor.toString()).equals("")) return;
		
		width = Integer.parseInt(widthInput);
		height = Integer.parseInt(heightInput);
		
		onFinish.invoke();
		
	}
	
	public boolean finishedValidly() {
		
		return width != -1 & height != -1;
		
	}
	
}
