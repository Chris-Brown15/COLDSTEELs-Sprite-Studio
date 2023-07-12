package cs.csss.editor.ui;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar.CSMenu;
import cs.csss.editor.DebugDisabledException;
import cs.csss.editor.Editor;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;

public class FilePanel {

	public FilePanel(Editor editor , CSNuklear nuklear) {
		
		CSUserInterface ui = nuklear.new CSUserInterface("Sprite Studio" , 0.001f , 0.001f , 0.999f , -1f);
		ui.setDimensions(ui.xPosition(), ui.yPosition(), ui.interfaceWidth() , 70);
		ui.options |= UI_TITLED|UI_BORDERED|UI_UNSCROLLABLE;
		
		CSMenuBar menuBar = ui.new CSDynamicRow().new CSMenuBar(); 
		CSMenu 
			fileMenu = menuBar.new CSMenu("File" , 100 , 400) ,
			editMenu = menuBar.new CSMenu("Edit" , 200 , 400) ,
			projectMenu = menuBar.new CSMenu("Project" , 230 , 400) ,
			optionsMenu = menuBar.new CSMenu("Options" , 349 , 400) ,
			debugMenu = menuBar.new CSMenu("Debug" , 300 , 400)
		;
		
		fileMenu.new CSDynamicRow().new CSButton("Save" , editor::saveCurrentProject);
		fileMenu.new CSDynamicRow().new CSButton("Save As" , editor::startSaveAs);
		fileMenu.new CSDynamicRow().new CSButton("Load" , editor::startProjectLoad);
		fileMenu.new CSDynamicRow().new CSButton("Exit" , editor::exit);
	
		editMenu.new CSDynamicRow().new CSButton("Undo" , editor::undo);
		editMenu.new CSDynamicRow().new CSButton("Redo" , editor::redo);
		editMenu.new CSDynamicRow().new CSButton("Run Artboard Script" , editor::startRunScript);
		editMenu.new CSDynamicRow().new CSButton("Run Project Script" , editor::startProjectScript);	
		
		projectMenu.new CSDynamicRow().new CSButton("New Project" , editor::startNewProject);
		projectMenu.new CSDynamicRow().new CSButton("Add Animation" , editor::startNewAnimation);
		projectMenu.new CSDynamicRow().new CSButton("Add Visual Layer" , editor::startNewVisualLayer);
		projectMenu.new CSDynamicRow().new CSButton("Add Nonvisual Layer" , editor::startNewNonVisualLayer);
		projectMenu.new CSDynamicRow().new CSButton("Add Artboard" , editor::startNewArtboard);
		projectMenu.new CSDynamicRow().new CSCheckBox("Show Animation Panel" , false , editor::toggleAnimationPanel);
		
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
		
		CSDynamicRow optionsRow1 = optionsMenu.new CSDynamicRow();
		optionsRow1.new CSButton("Controls" , editor::startEditingControls);
		optionsRow1.new CSButton("Background" , editor::startTransparentBackgroundSettings);
		
		optionsMenu.new CSDynamicRow().new CSButton("Simulation Framerate" , editor::startSetSimulationFrameRate);
		
		debugMenu.new CSDynamicRow().new CSButton("Toggle Realtime Mode" , () -> {
			
			try {
			
				editor.toggleRealtime();
				
			} catch (DebugDisabledException e) {

				e.printStackTrace();
				
			}
			
		});
		
	}
	
}
