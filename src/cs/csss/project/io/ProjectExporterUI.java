package cs.csss.project.io;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap;
import static cs.core.ui.CSUIConstants.*;

import java.util.ArrayList;
import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSCheckBox;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.ui.CSNuklear.CSUserInterface;
import cs.core.utils.Lambda;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFolder;
import cs.csss.project.CSSSProject;

public class ProjectExporterUI {
	
	/**
	 * String representing the absolute file path to export to when exporting.
	 */
	private static String exportLocation = CSFolder.getRoot("exports").getRealPath(); 

	private static final int
		UIWidth = 517 ,
		UIHeight = 525;
	
	/**
	 * Adds a file path to the list of export locations.
	 * 
	 * @param absoluteFilePath — String representing absolute filep path to export to
	 */
	public static void registerExportLocation(String absoluteFilePath) {

		synchronized(exportLocation) {
			
			exportLocation = absoluteFilePath;
			
		}
		
	}
	
	private final CSTextEditor nameInput;
	
	private final CSCheckBox 
		exportPalette ,
		exportHiddenLayers ,
		hideCheckeredBackground ,
		exportNonVisualLayers ,
		powerOfTwoSizes
	;
	
	private final CSRadio
		exportColor ,
		exportIndices
	;
	
	private ArrayList<ExportCallbackAndName> exporters = new ArrayList<>();
	
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
			
		};
		
		project.padAnimationFrames(false);
		
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
				
				ExportCallbackAndName newExport = new ExportCallbackAndName(types[j].callbackOf(), types[j].ending);
				if(exporters.contains(newExport)) exporters.remove(newExport);
				exporters.add(newExport);
				
			});
			
		}
		
		ui.new CSDynamicRow(25).new CSCheckBox("Script" , false , () -> {});
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		
		finishRow.new CSButton("Export" , () -> {
		
			if(!verifyExportable()) return;
			
			new ProjectExporter(
				engine.renderer() , 
				engine::windowSwapBuffers ,
				project , 
				exporters , 
				exportLocation + CSFolder.separator ,
				nameInput.toString() ,
				exportPalette.checked() ,
				exportHiddenLayers.checked() ,
				hideCheckeredBackground.checked() ,
				exportNonVisualLayers.checked() ,
				powerOfTwoSizes.checked() ,
				exportColor.checked()
			).export();
			
			onFinish.invoke();
			
		});
		
		finishRow.new CSButton("Cancel" , onFinish);
		
	}
	
	/**
	 * Makes sure the name input is not empty.
	 * 
	 * @return {@code true} if the name input box is not empty.
	 */
	private boolean verifyExportable() {
		
		return !nameInput.toString().equals("");
		
	}

} 