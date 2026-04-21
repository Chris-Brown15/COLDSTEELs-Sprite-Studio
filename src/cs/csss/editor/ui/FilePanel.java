package cs.csss.editor.ui;

import static cs.csss.ui.utils.UIUtils.toByte;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_checkbox_text;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.function.BooleanSupplier;

import org.lwjgl.system.MemoryStack;

import static sc.core.ui.SCUIConstants.*;

import cs.csss.editor.DebugDisabledException;
import cs.csss.editor.Editor;
import cs.csss.editor.event.RasterizeAllShapesEvent;
import cs.csss.editor.palette.ColorPalette;
import cs.csss.engine.CSSSException;
import cs.csss.engine.Engine;
import cs.csss.misc.graphics.memory.GPUMemoryViewer;
import cs.csss.project.ArtboardPalette;
import cs.csss.project.CSSSProject;
import cs.csss.ui.utils.UIUtils;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCCheckBox;
import sc.core.ui.SCElements.SCUI.SCLayout.SCMenuBar;
import sc.core.ui.SCElements.SCUI.SCLayout.SCMenuBar.SCMenu;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUI.SCRow;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * File panel is the top bar panel. It contains some buttons and menus for doing things in Sprite Studio.
 */
public class FilePanel {
	
	private boolean showingCheckeredBackground = true;

	
	/**
	 * Creates a file panel.
	 * 
	 * @param editor the editor
	 * @param nuklear Nuklear factory
	 */
	public FilePanel(Editor editor , SCNuklear nuklear) {
		
		SCUserInterface ui = new SCUserInterface(nuklear , "Sprite Studio" , 0.001f , 0.001f , 0.999f , 0.0f);
		ui.positioner.height(70);
		ui.flags |= UI_TITLED|UI_BORDERED|UI_UNSCROLLABLE;
		
		SCMenuBar menuBar = ui.new SCDynamicRow().new SCMenuBar(); 
		 
		SCMenu fileMenu = menuBar.new SCMenu(nuklear , "File" , 100 , 400),
			editMenu = menuBar.new SCMenu(nuklear , "Edit" , 200 , 400),
			projectMenu = menuBar.new SCMenu(nuklear , "Project" , 230 , 400),
			optionsMenu = menuBar.new SCMenu(nuklear , "Options" , 349 , 400);
						
		SCDynamicRow saveButtonRow = fileMenu.new SCDynamicRow() ; saveButtonRow.new SCButton("Save" , editor::saveProject);
		SCDynamicRow saveAsButtonRow = fileMenu.new SCDynamicRow() ; saveAsButtonRow.new SCButton("Save As" , editor::startProjectSaveAs);
		fileMenu.new SCDynamicRow().new SCButton("Load" , editor::startLoadProject);
		
		SCDynamicRow exportButtonRow = fileMenu.new SCDynamicRow() ; exportButtonRow.new SCButton("Export" , editor::startExport);		
		fileMenu.new SCDynamicRow().new SCButton("Exit" , editor::exit);
		
		saveButtonRow.doLayout = () -> editor.project() != null;
		saveAsButtonRow.doLayout = () -> editor.project() != null;
		exportButtonRow.doLayout = () -> editor.project() != null;
		
		editMenu.new SCDynamicRow().new SCButton("Undo" , editor::undo);
		editMenu.new SCDynamicRow().new SCButton("Redo" , editor::redo);
		editMenu.new SCDynamicRow().new SCButton("Create New Script" , editor::startCreateNewScript);
//		editMenu.new SCDynamicRow().new SCButton("Add Text" , editor::startAddText);
		editMenu.new SCDynamicRow().new SCButton("Run Artboard Script" , editor::startRunArtboardScript2);
		editMenu.new SCDynamicRow().new SCButton("Run Project Script" , editor::startRunProjectScript2);
		editMenu.new SCDynamicRow().new SCButton("Run Palette Script" , editor::startRunPaletteScript2);
		
		SCDynamicRow rasterizeAllShapesRow = editMenu.new SCDynamicRow();
		rasterizeAllShapesRow.new SCButton("Rasterize All Shapes" , () -> editor.eventPush(new RasterizeAllShapesEvent(editor.project())));
		rasterizeAllShapesRow.doLayout = () -> editor.project() != null;
		
		SCCheckBox colorInputTypeCheck = editMenu.new SCDynamicRow(20).new SCCheckBox(
			"Color Inputs Are Hex" , 
			editor::colorInputsAreHex , 
			editor::toggleColorInputsAreHex
		);
		
		UIUtils.toolTip(colorInputTypeCheck, "Determines whether color input dialogues will expect hexadecimal or decimal input formats.");
		editMenu.attachedLayout((context) -> {
			
			try(MemoryStack stack = MemoryStack.stackPush()) {
				
				Iterator<ColorPalette> palettes = ColorPalette.palettes();
				while(palettes.hasNext()) {
					
					ColorPalette next = palettes.next();
					nk_layout_row_dynamic(context , 20 , 1);
					if(nk_checkbox_text(context , "Show " + next.name , toByte(stack , next.show()))) next.toggleShow();
					
				}
			
			}
			
		});
				
		projectMenu.new SCDynamicRow().new SCButton("New Project" , editor::startNewProject);
		
		BooleanSupplier doLayoutProjectButtons = () -> editor.project() != null;
		
		SCDynamicRow addArtboardRow = projectMenu.new SCDynamicRow() , 
			addAnimationRow = projectMenu.new SCDynamicRow() ,
			addVisualLayerRow = projectMenu.new SCDynamicRow() ,
			addNonVisualLayerRow = projectMenu.new SCDynamicRow() ,
			toggleAnimationPanelRow = projectMenu.new SCDynamicRow(),
			togglePaletteUIRow = projectMenu.new SCDynamicRow();
		
		addArtboardRow.new SCButton("Add Artboard" , editor::startNewArtboard);
		addAnimationRow.new SCButton("Add Animation" , editor::startNewAnimation);
		addVisualLayerRow.new SCButton("Add Visual Layer" , editor::startNewVisualLayer);
		addNonVisualLayerRow.new SCButton("Add Nonvisual Layer" , editor::startNewNonVisualLayer);
		toggleAnimationPanelRow.new SCCheckBox("Animation Panel" , false , editor::toggleAnimationPanel);
		togglePaletteUIRow.new SCCheckBox("Palette Panel" , false , editor::togglePaletteReferenceMode);
		
		addAnimationRow.doLayout = doLayoutProjectButtons;
		addVisualLayerRow.doLayout = doLayoutProjectButtons;
		addNonVisualLayerRow.doLayout = doLayoutProjectButtons;
		addArtboardRow.doLayout = doLayoutProjectButtons;
		toggleAnimationPanelRow.doLayout = doLayoutProjectButtons;
		togglePaletteUIRow.doLayout = doLayoutProjectButtons; 
		
		optionsMenu.new SCDynamicRow().new SCButton("Toggle Fullscreen" , editor::toggleFullscreen);
		
		SCRow undoRedoSizeConfig = optionsMenu.new SCRow(30);
		undoRedoSizeConfig.pushWidth(190).pushWidth(100).pushWidth(40);
		
		undoRedoSizeConfig.new SCText(() -> "Undo/Redo Size (" + editor.undoCapacity() + ", " + editor.redoCapacity() + ")");
		SCTextEditor sizeInput = undoRedoSizeConfig.new SCTextEditor(4 , SCNuklear.DECIMAL_FILTER);
		undoRedoSizeConfig.new SCButton("Set" , () -> editor.setUndoAndRedoCapacity(Integer.parseInt(sizeInput.toString())));
		
		optionsMenu.new SCDynamicRow().new SCIntProperty(
			"Camera Move Rate" , 
			1 , 
			1 , 
			1 , 
			999 , 
			editor.setCameraMoveRate , 
			editor.getCameraMoveRate
		);
		
		SCDynamicRow optionsRow1 = optionsMenu.new SCDynamicRow();
		optionsRow1.new SCButton("Controls" , editor::startEditingControls);
		optionsRow1.new SCButton("Background" , editor::startCheckeredBackgroundSettings);
		
		optionsMenu.new SCDynamicRow().new SCButton("Simulation Framerate" , editor::startSetSimulationFrameRate);
		
		optionsMenu.new SCDynamicRow().new SCButton("Select UI Theme" , editor::startSelectUITheme);
		
		/*
		 * Steam workshop Menu
		 */
		
		if(editor.isSteamInitialized()) {
			
			 SCMenu steamMenu = menuBar.new SCMenu(nuklear , "Steam" , 349 , 400);
			 steamMenu.new SCDynamicRow().new SCButton("Post to Workshop" , editor::startSteamWorkshopItemUpload);
			 steamMenu.new SCDynamicRow().new SCButton("Update Workshop Item" , editor::startSteamWorkshopItemUpdate);
			
		}
		
		if(Engine.isDebug()) {
			
			SCMenu debugMenu = menuBar.new SCMenu(nuklear , "Debug" , 349 , 400);
			
			debugMenu.new SCDynamicRow().new SCButton("Toggle Realtime Mode" , () -> {
				
				try {
					
					editor.toggleRealtime();
					
				} catch (DebugDisabledException e) {
					
					e.printStackTrace();
					
				}
				
			});
			
			SCDynamicRow paletteRow1 = debugMenu.new SCDynamicRow(20);
			SCDynamicRow paletteRow2 = debugMenu.new SCDynamicRow(20);
			paletteRow1.doLayout = () -> editor.project() != null;
			paletteRow2.doLayout = paletteRow1.doLayout;
			paletteRow1.new SCText(() -> {
				
				return "Palette Size: " + editor.project().visualPalette().width() + ", " + editor.project().visualPalette().height();
				
			});
			
			paletteRow2.new SCText(() -> {
				
				return 
					"Palette Position: " + 
					editor.project().visualPalette().currentCol() + 
					", " + 
					editor.project().visualPalette().currentRow();
			});
			
			SCDynamicRow undoRedoStatsRow = debugMenu.new SCDynamicRow(20);
			undoRedoStatsRow.new SCText(() -> "Undo Size: " + editor.undoSize());
			undoRedoStatsRow.new SCText(() -> "Redo Size: " + editor.redoSize());
			
			int conversion = 1024 * 1024;
			SCDynamicRow memoryRow1 = debugMenu.new SCDynamicRow(20);
			memoryRow1.new SCText(() -> String.format(
				"Heap Size: %d, Available: %d" ,
				Runtime.getRuntime().totalMemory() / conversion , 
				Runtime.getRuntime().freeMemory() / conversion
			));
			
			SCDynamicRow memoryRow2 = debugMenu.new SCDynamicRow(20);
			memoryRow2.new SCText(() -> getGPUResourceString(editor));
			
			debugMenu.new SCDynamicRow().new SCButton("Toggle Transparent Background" , () -> {
				
				showingCheckeredBackground = !showingCheckeredBackground;
				editor.rendererPost(() -> {
					
					if(!showingCheckeredBackground) editor.project().forEachPalette(ArtboardPalette::hideCheckeredBackground);
					else editor.project().forEachPalette(ArtboardPalette::showCheckeredBackground);
					
				});
				
			});
			
			debugMenu.new SCDynamicRow().new SCButton("Switch Shader" , () -> {
				
				CSSSProject.setTheCurrentShader(CSSSProject.theTextureShader());
				
			});
			
			debugMenu.new SCDynamicRow().new SCButton("Throw Exception" , () -> {
				
				throw new CSSSException(new RuntimeException());
				
			});
			
			debugMenu.new SCDynamicRow().new SCButton("Open Styler" , () -> {
				
				editor.startUICustomizer();
				
			});
		
			debugMenu.new SCDynamicRow().new SCButton(
				"Dump Current Palette" , 
				() -> editor.rendererPost(() -> editor.project().currentPalette().dumpToFile())
			);
		
			debugMenu.new SCDynamicRow().new SCButton("Hide lines" , () -> editor.rendererPost(() -> editor.project().currentArtboard().undoAllLines()));
			
		}
		
	}
	
	private String getGPUResourceString(Editor editor) {
		
		try {
			
			return String.format(
				"VRAM: %d, Remaining: %d", 
				editor.rendererMake(GPUMemoryViewer::getTotalAvailableVRAM).get() , 
				editor.rendererMake(GPUMemoryViewer::getCurrentAvailableVRAM).get()
			);
			
		} catch (InterruptedException | ExecutionException e) {

			e.printStackTrace();
			return null;
			
		}		
		
	}
	
}