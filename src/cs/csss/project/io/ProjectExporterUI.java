package cs.csss.project.io;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static sc.core.ui.SCUIConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;
import org.python.util.PythonInterpreter;

import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFolder;
import cs.csss.project.CSSSProject;
import cs.csss.ui.menus.DroppedFileAcceptingDialogue;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCCheckBox;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUI.SCLayout.SCText;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI element for selecting options and begining the export of the project.
 */
public class ProjectExporterUI extends DroppedFileAcceptingDialogue {
	
	/**
	 * String representing the absolute file path to export to when exporting.
	 */
	private String exportLocation = CSFolder.getRoot("exports").getRealPath(); 

	private static final int
		UIWidth = 517 ,
		UIHeight = 525;
	
	/**
	 * Sets the file path of export locations.
	 * 
	 * @param absoluteFilePath String representing absolute filep path to export to
	 */
	public static void registerExportLocation(String absoluteFilePath) {

		
	}
	
	private final SCTextEditor nameInput;
	
	private final SCCheckBox 
		exportPalette ,
		exportHiddenLayers ,
		hideCheckeredBackground ,
		exportNonVisualLayers ,
		exportAnimationFiles ,
		powerOfTwoSizes;
	
	private final SCRadio
		exportColor ,
		exportIndices;
	
	private ArrayList<ExportCallbackAndName> exporters = new ArrayList<>();
	
	private String scriptPath;
	
	private SCCheckBox doExportScript;
	
	/**
	 * Creates a project exporter UI.
	 * 
	 * @param engine the engine
	 * @param nuklear the Nuklear factory
	 * @param project the project to export
	 */
	public ProjectExporterUI(Engine engine , SCNuklear nuklear , final CSSSProject project) {
	
		int[] windowSize = engine.windowSize();
		SCUserInterface ui = new SCUserInterface(
			nuklear,
			"Exporter" , 
			(windowSize[0] / 2) - (UIWidth / 2) , 
			(windowSize[1] / 2) - (UIHeight / 2) , 
			UIWidth , 
			UIHeight
		);
		
		ui.flags = UI_TITLED|UI_BORDERED|UI_MOVABLE;
		
		Runnable onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			Engine.THE_TEMPORAL.onTrue(() -> true, ui::shutDown);
			super.onFinish();
			
		};
		
		String description = "Drag a folder into Sprite Studio to select it as the location to export to.";
		ui.new SCDynamicRow(20).new SCText(description , (byte)0x0 , (byte)0x79 , (byte) 0x0 , (byte)0xff);
				
		ui.new SCDynamicRow(20).new SCText("Export Destination:");
		ui.attachedLayout((context) -> {
			
			synchronized(exportLocation) {
				
				nk_layout_row_dynamic(context , 40 , 1);			
				nk_text_wrap(context , exportLocation);
							
			}
			
		});
		
		ui.new SCDynamicRow(20).new SCText("File Name:");
		nameInput = ui.new SCDynamicRow(25).new SCTextEditor(999);
				
		exportPalette = ui.new SCDynamicRow(25).new SCCheckBox("Export Palettes" , false , () -> {});
		exportHiddenLayers = ui.new SCDynamicRow(25).new SCCheckBox("Export Hidden Layers" , false , () -> {});
		exportNonVisualLayers = ui.new SCDynamicRow(25).new SCCheckBox("Export Nonvisual Layers" , false , () -> {});
		hideCheckeredBackground = ui.new SCDynamicRow(25).new SCCheckBox("Hide Checkered Background" , false , () -> {});
		exportAnimationFiles = ui.new SCDynamicRow(25).new SCCheckBox("Export Animation Files" , false , () -> {});
		powerOfTwoSizes = ui.new SCDynamicRow(25).new SCCheckBox("Power Of Two Sizes" , false , () -> {});
		
	 	SCDynamicRow freemoveRow = ui.new SCDynamicRow(25);
	 	SCCheckBox freemoveCheck = freemoveRow.new SCCheckBox("Freemove Mode" , false , project::toggleFreemoveMode);
	 	SCCheckBox freemoveCollisionCheck = freemoveRow.new SCCheckBox(
	 		"Check Collisions" , 
	 		project.freemoveCheckCollisions() , 
	 		project::toggleFreemoveCheckCollisions
	 	);
	 	
	 	freemoveCollisionCheck.doLayout = freemoveCheck::checked; 
	 		 	
		exportColor = ui.new SCDynamicRow(25).new SCRadio("Export As Colors" , true , () -> {});
		exportIndices = ui.new SCDynamicRow(25).new SCRadio("Export As Indices" , false , exportPalette::check);
			
		SCRadio.groupAll(exportColor , exportIndices);
		
		ui.new SCDynamicRow(20).new SCText("Export As:");
		
		ExportFileTypes[] types = ExportFileTypes.values();		
		for(int i = 0 ; i < types.length ; i ++) {
			
			int j = i;			 
			ui.new SCDynamicRow(25).new SCCheckBox(types[i].toString() , false , () -> {
				
				//if an item of the same extension was removed, break out, otherwise continue to add a new exporter
				if(exporters.removeIf(item -> item.extension().equals(types[j].ending))) return;
				exporters.add(new ExportCallbackAndName(types[j].callbackOf(), types[j].ending));
				
			});
			
		}
		
		SCDynamicRow scriptRow = ui.new SCDynamicRow(30);
		doExportScript = scriptRow.new SCCheckBox("Script" , false , () -> {});
		SCText text = scriptRow.new SCText(() -> "(" + new File(scriptPath).getName() + ")");
		text.doLayout = () -> scriptPath != null;
		
		ui.attachedLayout((context) -> {
			
			if(scriptPath == null) doExportScript.uncheck();
			
		});
		
		scriptRow.new SCButton("Select" , () -> engine.startSelectScriptMenu("exporters" , file -> {
			
			scriptPath = file.getAbsolutePath();
			String filename = file.getName();
			
			exporters.add(new ExportCallbackAndName((filepath , data , width , height , channels) -> {
				
				try(PythonInterpreter python = new PythonInterpreter()) {
					
					try {
						
						python.execfile(new FileInputStream(file));
						PyObject export = python.get("export");
						ClassicPyObjectAdapter adapter = new ClassicPyObjectAdapter();
						export.__call__(new PyObject[] {
							adapter.adapt(filepath) , 
							adapter.adapt(data) , 
							adapter.adapt(width) , 
							adapter.adapt(height) , 
							adapter.adapt(channels)
						});
						
					} catch (Exception e) {
					
						e.printStackTrace();
					
					}
					
				}
				
			} , filename.substring(0 , filename.length() - 3)));

		}));
		
		SCDynamicRow finishRow = ui.new SCDynamicRow();
		
		finishRow.new SCButton("Export" , () -> {
		
			if(!verifyExportable()) return;
			
			ProjectExporter exporter;
			try {
				
				exporter = new ProjectExporter(
					engine.renderer() , 
					engine::windowSwapBuffers ,
					project , 
					exporters , 
					exportLocation + CSFolder.separator ,
					nameInput.toString() ,
					engine.nanoVG() ,
					engine.windowSize() ,
					exportPalette.checked() ,
					exportHiddenLayers.checked() ,
					hideCheckeredBackground.checked() ,
					exportNonVisualLayers.checked() ,
					powerOfTwoSizes.checked() ,
					exportColor.checked() ,
					exportAnimationFiles.checked()
				);
				
			} catch (InterruptedException | ExecutionException e) {
				
				e.printStackTrace();
				return;
				
			}
			
			exporter.export();
			
			onFinish.run();
			
		});
		
		finishRow.new SCButton("Cancel" , onFinish);
		
	}
	
	private boolean verifyExportable() {
		
		boolean initiallyPossible = exporters.size() > 0 && !nameInput.toString().equals("");
		if(doExportScript.checked()) initiallyPossible = initiallyPossible && scriptPath != null;
		
		return initiallyPossible;
		
	}

	@Override public void acceptDroppedFilePath(String... filepaths) {

		super.defaultAcceptDroppedFilePath(filepaths);
		
		synchronized(exportLocation) {
			
			exportLocation = filepaths[0];
			
		}
	}

} 