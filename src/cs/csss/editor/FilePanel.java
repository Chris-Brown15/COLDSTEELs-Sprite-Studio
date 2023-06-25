package cs.csss.editor;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar.CSMenu;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;

public class FilePanel {

	FilePanel(Editor editor , CSNuklear nuklear) {
		
		CSUserInterface ui = nuklear.new CSUserInterface("File" , 0.001f , 0.001f , 0.999f , -1f);
		ui.setDimensions(ui.xPosition(), ui.yPosition(), ui.interfaceWidth() , 70);
		ui.options |= UI_TITLED|UI_BORDERED|UI_UNSCROLLABLE;
		
		CSMenuBar menuBar = ui.new CSDynamicRow().new CSMenuBar(); 
		CSMenu 
			fileMenu = menuBar.new CSMenu("File" , 100 , 400) ,
			editMenu = menuBar.new CSMenu("Edit" , 200 , 400) ,
			projectMenu = menuBar.new CSMenu("Project" , 230 , 400) ,
			optionsMenu = menuBar.new CSMenu("Options" , 349 , 400)			
		;
		
		fileMenu.new CSDynamicRow().new CSButton("Save" , editor::saveCurrentProject);
		fileMenu.new CSDynamicRow().new CSButton("Save As" , editor::startSaveAs);
		fileMenu.new CSDynamicRow().new CSButton("Load" , editor::startProjectLoad);
		fileMenu.new CSDynamicRow().new CSButton("Exit" , editor::exit);
	
		editMenu.new CSDynamicRow().new CSButton("Undo" , editor::undo);
		editMenu.new CSDynamicRow().new CSButton("Redo" , editor::redo);
		editMenu.new CSDynamicRow().new CSButton("Run Script" , editor::startRunScript);
		
//		CSDynamicRow changeGrayscaleShadeRow = editMenu.new CSDynamicRow();
//		changeGrayscaleShadeRow.doLayout = () -> {
//			
//			return editor.currentProject() != null && editor.currentProject().getChannelsPerPixelOfCurrentLayer() <= 2;
//			
//		};
//		
//		changeGrayscaleShadeRow.new CSButton("Change Grayscale Shade" , editor::startNewGrayscaleShadeMenu);
		
		
		projectMenu.new CSDynamicRow().new CSButton("New Project" , editor::startNewProject);
		projectMenu.new CSDynamicRow().new CSButton("Add Animation" , editor::startNewAnimation);
		projectMenu.new CSDynamicRow().new CSButton("Add Visual Layer" , editor::startNewVisualLayer);
		projectMenu.new CSDynamicRow().new CSButton("Add Nonvisual Layer" , editor::startNewNonVisualLayer);
		projectMenu.new CSDynamicRow().new CSButton("Add Artboard" , editor::startNewArtboard);
		
		optionsMenu.new CSDynamicRow().new CSButton("Toggle Fullscreen" , editor::toggleFullscreen);
		
		CSRow undoRedoSizeConfig = optionsMenu.new CSRow(30);
		undoRedoSizeConfig
			.pushWidth(190)
			.pushWidth(100)
			.pushWidth(40)
		;
		
		undoRedoSizeConfig.new CSText(() -> "Undo/Redo Size (" + editor.undoCapacity() + ", " + editor.redoCapacity() + ")");
		CSTextEditor sizeInput = undoRedoSizeConfig.new CSTextEditor(4 , CSNuklear.DECIMAL_FILTER);
		undoRedoSizeConfig.new CSButton("Set" , () -> editor.setUndoAndRedoCapacity(Integer.parseInt(sizeInput.toString())));
		
		optionsMenu.new CSDynamicRow().new CSIntProperty(
			"Camera Move Rate" , 
			1 , 
			1 ,
			1 ,
			999 ,  
			editor.setCameraMoveRate , 
			editor.getCameraMoveRate
		);
		
		optionsMenu.new CSDynamicRow().new CSButton("Configure Controls" , editor::startEditingControls);

		optionsMenu.new CSDynamicRow().new CSButton("Transparent Background Settings" , editor::startTransparentBackgroundSettings);
		
	}
	
}
