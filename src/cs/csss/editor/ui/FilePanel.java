package cs.csss.editor.ui;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import java.io.IOException;
import java.util.function.BooleanSupplier;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar.CSMenu;
import cs.csss.editor.DebugDisabledException;
import cs.csss.editor.Editor;
import cs.csss.misc.files.CSFolder;
import cs.csss.project.Animation;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.project.io.CTSAFile;
import cs.csss.project.io.CTSAFile.FrameChunk;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;

public class FilePanel {

	private boolean showingCheckeredBackground = true;
	
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
			debugMenu = menuBar.new CSMenu("Debug" , 300 , 800)
		;
		
		CSDynamicRow saveButtonRow = fileMenu.new CSDynamicRow() ; saveButtonRow.new CSButton("Save" , editor::saveProject);
		CSDynamicRow saveAsButtonRow = fileMenu.new CSDynamicRow() ; saveAsButtonRow.new CSButton("Save As" , editor::startProjectSaveAs);
		fileMenu.new CSDynamicRow().new CSButton("Load" , editor::startLoadProject);
		
		CSDynamicRow exportButtonRow = fileMenu.new CSDynamicRow() ; exportButtonRow.new CSButton("Export" , editor::startExport);
		fileMenu.new CSDynamicRow().new CSButton("Exit" , editor::exit);
		
		saveButtonRow.doLayout = () -> editor.project() != null;
		saveAsButtonRow.doLayout = () -> editor.project() != null;
		exportButtonRow.doLayout = () -> editor.project() != null;
		
		editMenu.new CSDynamicRow().new CSButton("Undo" , editor::undo);
		editMenu.new CSDynamicRow().new CSButton("Redo" , editor::redo);
//		editMenu.new CSDynamicRow().new CSButton("Add Text" , editor::startAddText);
		editMenu.new CSDynamicRow().new CSButton("Run Artboard Script" , editor::startRunScript);
		editMenu.new CSDynamicRow().new CSButton("Run Project Script" , editor::startProjectScript);
	
		projectMenu.new CSDynamicRow().new CSButton("New Project" , editor::startNewProject);
		
		BooleanSupplier doLayoutProjectButtons = () -> editor.project() != null;
		
		CSDynamicRow
			addAnimationRow = projectMenu.new CSDynamicRow() ,
			addVisualLayerRow = projectMenu.new CSDynamicRow() ,
			addNonVisualLayerRow = projectMenu.new CSDynamicRow() ,
			addArtboardRow = projectMenu.new CSDynamicRow() ,
			toggleAnimationPanelRow = projectMenu.new CSDynamicRow();
		
		addAnimationRow.new CSButton("Add Animation" , editor::startNewAnimation);
		addVisualLayerRow.new CSButton("Add Visual Layer" , editor::startNewVisualLayer);
		addNonVisualLayerRow.new CSButton("Add Nonvisual Layer" , editor::startNewNonVisualLayer);
		addArtboardRow.new CSButton("Add Artboard" , editor::startNewArtboard);
		toggleAnimationPanelRow.new CSCheckBox("Animation Panel" , false , editor::toggleAnimationPanel);
		
		addAnimationRow.doLayout = doLayoutProjectButtons;
		addVisualLayerRow.doLayout = doLayoutProjectButtons;
		addNonVisualLayerRow.doLayout = doLayoutProjectButtons;
		addArtboardRow.doLayout = doLayoutProjectButtons;
		toggleAnimationPanelRow.doLayout = doLayoutProjectButtons; 
		
		optionsMenu.new CSDynamicRow().new CSButton("Toggle Fullscreen" , editor::toggleFullscreen);
		
		CSRow undoRedoSizeConfig = optionsMenu.new CSRow(30);
		undoRedoSizeConfig.pushWidth(190).pushWidth(100).pushWidth(40);
		
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
		
		CSDynamicRow paletteRow1 = debugMenu.new CSDynamicRow();
		CSDynamicRow paletteRow2 = debugMenu.new CSDynamicRow();
		paletteRow1.doLayout = () -> editor.project() != null;
		paletteRow2.doLayout = paletteRow1.doLayout;
		paletteRow1.new CSText(() -> {
			
			return "Palette Size: " + editor.project().visualPalette().width() + ", " + editor.project().visualPalette().height();
			
		});
		
		paletteRow2.new CSText(() -> {
			
			return "Palette Position: " + editor.project().visualPalette().currentCol() + ", " + editor.project().visualPalette().currentRow();
			
		});
				
		debugMenu.new CSDynamicRow().new CSButton("Toggle Transparent Background" , () -> {
			
			showingCheckeredBackground = !showingCheckeredBackground;
			editor.rendererPost(() -> {
				
				if(!showingCheckeredBackground) editor.project().forEachPalette(ArtboardPalette::hideCheckeredBackground);
				else editor.project().forEachPalette(ArtboardPalette::showCheckeredBackground);
				
			});
			
		});
		
		debugMenu.new CSDynamicRow().new CSButton("Reload Shaders" , () -> {
			
			editor.rendererPost(() -> CSSSProject.thePaletteShader().reload(
				readAllCharacters("assets/shaders/vertexShader.glsl") , 
				readAllCharacters("assets/shaders/fragmentPaletteShader.glsl")
			));
						
		});
		
		debugMenu.new CSDynamicRow().new CSButton("Export Animation" , () -> {
			
		 	Animation a = editor.project().currentAnimation();
			CTSAFile animFile = new CTSAFile(a , editor.project());
		 	try {
		 		
				animFile.write(CSFolder.getRoot("debug").getRealPath());
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		});

		debugMenu.new CSDynamicRow().new CSButton("Load Animation" , () -> {
			
			String filePath = CSFolder.getRoot("debug").getFile("Default Animation" + CTSAFile.FILE_EXTENSION).getRealPath();
			CTSAFile animFile = new CTSAFile(filePath);
			
		 	try {
		 		
				animFile.read();
				System.out.println(animFile.getAnimationName());		
				System.out.println(animFile.getNumberFrames());
				System.out.println(animFile.getLeftU());
				System.out.println(animFile.getBottomV());
				System.out.println(animFile.getTopV());
				System.out.println(animFile.getWidthU());
				for(FrameChunk x : animFile.getFrames()) System.out.println(x);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
		});
		
		debugMenu.new CSDynamicRow().new CSButton("Switch Shader" , () -> {
			
			CSSSProject.setTheCurrentShader(CSSSProject.theTextureShader());
			
		});
		
	}
	
}
