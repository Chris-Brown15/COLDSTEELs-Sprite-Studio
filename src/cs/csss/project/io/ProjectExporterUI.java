package cs.csss.project.io;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static cs.core.ui.CSUIConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;
import org.python.util.PythonInterpreter;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSCheckBox;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSText;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFolder;
import cs.csss.project.CSSSProject;
import cs.csss.ui.menus.DroppedFileAcceptingDialogue;

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
	 * @param absoluteFilePath — String representing absolute filep path to export to
	 */
	public static void registerExportLocation(String absoluteFilePath) {

		
	}
	
	private final CSTextEditor nameInput;
	
	private final CSCheckBox 
		exportPalette ,
		exportHiddenLayers ,
		hideCheckeredBackground ,
		exportNonVisualLayers ,
		exportAnimationFiles ,
		powerOfTwoSizes;
	
	private final CSRadio
		exportColor ,
		exportIndices;
	
	private ArrayList<ExportCallbackAndName> exporters = new ArrayList<>();
	
	private String scriptPath;
	
	private CSCheckBox doExportScript;
	
	/**
	 * Creates a project exporter UI.
	 * 
	 * @param engine — the engine
	 * @param nuklear — the Nuklear factory
	 * @param project — the project to export
	 */
	public ProjectExporterUI(final Engine engine , CSNuklear nuklear , final CSSSProject project) {
	
		int[] windowSize = engine.windowSize();
		CSUserInterface ui = nuklear.new CSUserInterface(
			"Exporter" , 
			(windowSize[0] / 2) - (UIWidth / 2) , 
			(windowSize[1] / 2) - (UIHeight / 2) , 
			UIWidth , 
			UIHeight
		);
		
		ui.options = UI_TITLED|UI_BORDERED|UI_MOVABLE;
		
		Lambda onFinish = () -> {
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			super.onFinish();
			
		};
		
		String description = "Drag a folder into Sprite Studio to select it as the location to export to.";
		ui.new CSDynamicRow(20).new CSText(description , (byte)0x0 , (byte)0x79 , (byte) 0x0 , (byte)0xff);
				
		ui.new CSDynamicRow(20).new CSText("Export Destination:");
		ui.attachedLayout((context , stack) -> {
			
			synchronized(exportLocation) {
				
				nk_layout_row_dynamic(context , 40 , 1);			
				nk_text_wrap(context , exportLocation);
							
			}
			
		});
		
		ui.new CSDynamicRow(20).new CSText("File Name:");
		nameInput = ui.new CSDynamicRow(25).new CSTextEditor(999);
				
		exportPalette = ui.new CSDynamicRow(25).new CSCheckBox("Export Palettes" , false , () -> {});
		exportHiddenLayers = ui.new CSDynamicRow(25).new CSCheckBox("Export Hidden Layers" , false , () -> {});
		exportNonVisualLayers = ui.new CSDynamicRow(25).new CSCheckBox("Export Nonvisual Layers" , false , () -> {});
		hideCheckeredBackground = ui.new CSDynamicRow(25).new CSCheckBox("Hide Checkered Background" , false , () -> {});
		exportAnimationFiles = ui.new CSDynamicRow(25).new CSCheckBox("Export Animation Files" , false , () -> {});
		powerOfTwoSizes = ui.new CSDynamicRow(25).new CSCheckBox("Power Of Two Sizes" , false , () -> {});
		
	 	CSDynamicRow freemoveRow = ui.new CSDynamicRow(25);
	 	CSCheckBox freemoveCheck = freemoveRow.new CSCheckBox("Freemove Mode" , false , project::toggleFreemoveMode);
	 	CSCheckBox freemoveCollisionCheck = freemoveRow.new CSCheckBox(
	 		"Check Collisions" , 
	 		project.freemoveCheckCollisions() , 
	 		project::toggleFreemoveCheckCollisions
	 	);
	 	
	 	freemoveCollisionCheck.doLayout = freemoveCheck::checked; 
	 		 	
		exportColor = ui.new CSDynamicRow(25).new CSRadio("Export As Colors" , true , () -> {});
		exportIndices = ui.new CSDynamicRow(25).new CSRadio("Export As Indices" , false , exportPalette::check);
			
		CSRadio.groupAll(exportColor , exportIndices);
		
		ui.new CSDynamicRow(20).new CSText("Export As:");
		
		ExportFileTypes[] types = ExportFileTypes.values();		
		for(int i = 0 ; i < types.length ; i ++) {
			
			int j = i;			 
			ui.new CSDynamicRow(25).new CSCheckBox(types[i].toString() , false , () -> {
				
				//if an item of the same extension was removed, break out, otherwise continue to add a new exporter
				if(exporters.removeIf(item -> item.extension().equals(types[j].ending))) return;
				exporters.add(new ExportCallbackAndName(types[j].callbackOf(), types[j].ending));
				
			});
			
		}
		
		CSDynamicRow scriptRow = ui.new CSDynamicRow(30);
		doExportScript = scriptRow.new CSCheckBox("Script" , false , () -> {});
		CSText text = scriptRow.new CSText(() -> "(" + new File(scriptPath).getName() + ")");
		text.doLayout = () -> scriptPath != null;
		
		ui.attachedLayout((context , stack) -> {
			
			if(scriptPath == null) doExportScript.uncheck();
			
		});
		
		scriptRow.new CSButton("Select" , () -> engine.startSelectScriptMenu("exporters" , file -> {
			
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
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		
		finishRow.new CSButton("Export" , () -> {
		
			if(!verifyExportable()) return;
			
			ProjectExporter exporter = new ProjectExporter(
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
			
			exporter.export();
			
			onFinish.invoke();
			
		});
		
		finishRow.new CSButton("Cancel" , onFinish);
		
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