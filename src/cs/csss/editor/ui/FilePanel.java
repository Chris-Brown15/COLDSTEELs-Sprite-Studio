package cs.csss.editor.ui;

import static cs.core.utils.CSFileUtils.readAllCharacters;

import static cs.csss.ui.utils.UIUtils.toByte;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_text;

import java.util.Iterator;
import java.util.function.BooleanSupplier;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSMenuBar.CSMenu;
import cs.csss.editor.DebugDisabledException;
import cs.csss.editor.Editor;
import cs.csss.editor.palette.ColorPalette;
import cs.csss.engine.CSSSException;
import cs.csss.engine.Engine;
import cs.csss.misc.graphcs.memory.GPUMemoryViewer;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUI.CSRow;
import cs.core.ui.CSNuklear.CSUserInterface;

/**
 * File panel is the top bar panel. It contains some buttons and menus for doing things in Sprite Studio.
 */
public class FilePanel {

	private boolean showingCheckeredBackground = true;
	
	/**
	 * Creates a file panel.
	 * 
	 * @param editor — the editor
	 * @param nuklear — Nuklear factory
	 */
	public FilePanel(Editor editor , CSNuklear nuklear) {
		
		CSUserInterface ui = nuklear.new CSUserInterface("Sprite Studio" , 0.001f , 0.001f , 0.999f , -1f);
		ui.setDimensions(ui.xPosition(), ui.yPosition(), ui.interfaceWidth() , 70);
		ui.options |= UI_TITLED|UI_BORDERED|UI_UNSCROLLABLE;
		
		CSMenuBar menuBar = ui.new CSDynamicRow().new CSMenuBar(); 
		 
		CSMenu fileMenu = menuBar.new CSMenu("File" , 100 , 400),
			editMenu = menuBar.new CSMenu("Edit" , 200 , 400),
			projectMenu = menuBar.new CSMenu("Project" , 230 , 400),
			optionsMenu = menuBar.new CSMenu("Options" , 349 , 400);
						
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
		editMenu.new CSDynamicRow().new CSButton("Run Artboard Script" , editor::startRunArtboardScript);
		editMenu.new CSDynamicRow().new CSButton("Run Project Script" , editor::startRunProjectScript);
		editMenu.new CSDynamicRow().new CSButton("Run Palette Script" , editor::startRunPaletteScript);
		editMenu.attachedLayout((context , stack) -> {
			
			Iterator<ColorPalette> palettes = ColorPalette.palettes();
			while(palettes.hasNext()) {
				
				ColorPalette next = palettes.next();
				nk_layout_row_dynamic(context , 30 , 1);
				if(nk_checkbox_text(context , "Show " + next.name , toByte(stack , next.show()))) next.toggleShow();
				
			}
			
		});
				
		projectMenu.new CSDynamicRow().new CSButton("New Project" , editor::startNewProject);
		
		BooleanSupplier doLayoutProjectButtons = () -> editor.project() != null;
		
		CSDynamicRow addArtboardRow = projectMenu.new CSDynamicRow() , 
			addAnimationRow = projectMenu.new CSDynamicRow() ,
			addVisualLayerRow = projectMenu.new CSDynamicRow() ,
			addNonVisualLayerRow = projectMenu.new CSDynamicRow() ,
			toggleAnimationPanelRow = projectMenu.new CSDynamicRow();
		
		addArtboardRow.new CSButton("Add Artboard" , editor::startNewArtboard);
		addAnimationRow.new CSButton("Add Animation" , editor::startNewAnimation);
		addVisualLayerRow.new CSButton("Add Visual Layer" , editor::startNewVisualLayer);
		addNonVisualLayerRow.new CSButton("Add Nonvisual Layer" , editor::startNewNonVisualLayer);
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
		optionsRow1.new CSButton("Background" , editor::startCheckeredBackgroundSettings);
		
		optionsMenu.new CSDynamicRow().new CSButton("Simulation Framerate" , editor::startSetSimulationFrameRate);
		
		/*
		 * Steam workshop Menu
		 */
		
		if(editor.isSteamInitialized()) {
			
			 CSMenu steamMenu = menuBar.new CSMenu("Steam" , 349 , 400);
			 steamMenu.new CSDynamicRow().new CSButton("Post to Workshop" , editor::startSteamWorkshopItemUpload);
			 steamMenu.new CSDynamicRow().new CSButton("Update Workshop Item" , editor::startSteamWorkshopItemUpdate);
			
		}
		
		if(Engine.isDebug()) {
			
			CSMenu debugMenu = menuBar.new CSMenu("Debug" , 349 , 400);
			
			debugMenu.new CSDynamicRow().new CSButton("Toggle Realtime Mode" , () -> {
				
				try {
					
					editor.toggleRealtime();
					
				} catch (DebugDisabledException e) {
					
					e.printStackTrace();
					
				}
				
			});
			
			CSDynamicRow paletteRow1 = debugMenu.new CSDynamicRow(20);
			CSDynamicRow paletteRow2 = debugMenu.new CSDynamicRow(20);
			paletteRow1.doLayout = () -> editor.project() != null;
			paletteRow2.doLayout = paletteRow1.doLayout;
			paletteRow1.new CSText(() -> {
				
				return "Palette Size: " + editor.project().visualPalette().width() + ", " + editor.project().visualPalette().height();
				
			});
			
			paletteRow2.new CSText(() -> {
				
				return 
					"Palette Position: " + 
					editor.project().visualPalette().currentCol() + 
					", " + 
					editor.project().visualPalette().currentRow();
			});
			
			int conversion = 1024 * 1024;
			CSDynamicRow memoryRow1 = debugMenu.new CSDynamicRow(20);
			memoryRow1.new CSText(() -> String.format(
				"Heap Size: %d, Available: %d" ,
				Runtime.getRuntime().totalMemory() / conversion , 
				Runtime.getRuntime().freeMemory() / conversion
			));
			
			CSDynamicRow memoryRow2 = debugMenu.new CSDynamicRow(20);
			memoryRow2.new CSText(() -> String.format(
				"VRAM: %d, Remaining: %d", 
				editor.rendererMake(GPUMemoryViewer::getTotalAvailableVRAM).get() ,
				editor.rendererMake(GPUMemoryViewer::getCurrentAvailableVRAM).get()
			));
			
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
			
			debugMenu.new CSDynamicRow().new CSButton("Switch Shader" , () -> {
				
				CSSSProject.setTheCurrentShader(CSSSProject.theTextureShader());
				
			});
			
			debugMenu.new CSDynamicRow().new CSButton("Throw Exception" , () -> {
				
				throw new CSSSException(new RuntimeException());
				
			});
			
		}
		
	}
	
}