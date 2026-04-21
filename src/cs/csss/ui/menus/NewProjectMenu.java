package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_text_wrap_colored;

import java.util.Iterator;
import java.util.LinkedList;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.system.MemoryStack;

import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.project.ArtboardPalette;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUI.SCRow;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * UI menu for creating a new project.
 */
public class NewProjectMenu extends Dialogue {

	private final LinkedList<String> existingProjects = new LinkedList<>();
	
	private volatile boolean canFinish = false;

	private volatile String projectName;
	private final SCUserInterface ui;
	private final SCTextEditor nameInput;

	private final Runnable removeUIOnFinish;

	private int channelsPerPixel = 4;

	private SCTextEditor widthEditor , heightEditor;

	private int paletteWidth = ArtboardPalette.MAX_WIDTH;
	private int paletteHeight = ArtboardPalette.MAX_HEIGHT;
	
	/**
	 * Creates a new project menu.
	 * 
	 * @param nuklear the Nuklear factory
	 */
	public NewProjectMenu(SCNuklear nuklear) {

		this.ui = new SCUserInterface(nuklear , "New Project" , 0.5f - (0.33f / 2) , 0.5f - (0.33f / 2) , 0.33f , 0.33f);
		ui.flags = UI_TITLED|UI_BORDERED;

		SCRow nameRow = ui.new SCRow(30).pushWidth(.15f).pushWidth(.8f);
		nameRow.new SCText("Name:" , TEXT_CENTERED|TEXT_LEFT);
		nameInput = nameRow.new SCTextEditor(100);
		
		ui.new SCDynamicRow(20).new SCText("Pixel Format:" , TEXT_CENTERED|TEXT_LEFT);		
		SCDynamicRow row1 =  ui.new SCDynamicRow(25);
		SCRadio oneChannel = row1.new SCRadio("Grayscale" , () -> channelsPerPixel == 1 , () -> channelsPerPixel = 1);
		SCRadio threeChannels = row1.new SCRadio("RGB" , () -> channelsPerPixel == 3 , () -> channelsPerPixel = 3);
		SCDynamicRow row2 =  ui.new SCDynamicRow(25);
		SCRadio twoChannels = row2.new SCRadio("Grayscale + Alpha" , () -> channelsPerPixel == 2 , () -> channelsPerPixel = 2);
		SCRadio fourChannels = row2.new SCRadio("RGB + Alpha" , () -> channelsPerPixel == 4, () -> channelsPerPixel = 4);
		
		channelsPerPixel = 4;
		
		SCRadio.groupAll(oneChannel , twoChannels , threeChannels , fourChannels);
		
	 	ui.attachedLayout((context) -> {
	 		
	 		for(String x : existingProjects) if(x.equals(nameInput.toString())) {
	 			
	 			try(MemoryStack stack = MemoryStack.stackPush()) {
	 				
	 				nk_layout_row_dynamic(context , 40 , 1);
	 				NkColor red = NkColor.malloc(stack).set((byte)-1 , (byte)0 , (byte)0 , (byte)-1);
	 				String warningText = x + " already names a project. Overwrites and errors may occur if this name is chosen." ;
	 				nk_text_wrap_colored(context , warningText, red);
	 			
	 			}
	 			
	 		}
	 		
	 	});
	 	
	 	SCDynamicRow paletteSizeInputTextRow = ui.new SCDynamicRow(20);
	 	paletteSizeInputTextRow.new SCText("Palette size for this project, leave blank for defaults.");
	 	
	 	SCRow paletteSizeWidthInputRow = ui.new SCRow(30);
	 	paletteSizeWidthInputRow.pushWidth(0.15f).pushWidth(0.80f);
	 	paletteSizeWidthInputRow.new SCText("Width:" , TEXT_CENTERED|TEXT_LEFT);
	 	widthEditor = paletteSizeWidthInputRow.new SCTextEditor(4 , SCNuklear.DECIMAL_FILTER);
	 		 	
	 	SCRow paletteSizeHeightInputRow = ui.new SCRow(30);
	 	paletteSizeHeightInputRow.pushWidth(0.15f).pushWidth(0.80f);
	 	paletteSizeHeightInputRow.new SCText("Height:" , TEXT_CENTERED|TEXT_LEFT);
	 	heightEditor = paletteSizeHeightInputRow.new SCTextEditor(4 , SCNuklear.DECIMAL_FILTER);
	 	
	 	SCDynamicRow totalPaletteSizeRow = ui.new SCDynamicRow(20);
	 	totalPaletteSizeRow.new SCText(() -> "Total Palette Space (Pixels): " + getTotalPixelsFromInputs() , TEXT_LEFT|TEXT_MIDDLE);
	 	
	 	SCDynamicRow finishAndCancelRow = ui.new SCDynamicRow();
	 	finishAndCancelRow.new SCButton("Finish" , this::finish);
	 	finishAndCancelRow.new SCButton("Cancel" , this::cancel);
	 	
	 	removeUIOnFinish = () -> {
	 		
	 		nuklear.removeUserInterface(ui);
	 		Engine.THE_TEMPORAL.onTrue(() -> true, ui::shutDown);	 		
	 		super.onFinish();
	 		
	 	};
		
		Engine.THE_THREADS.submit(() -> {
			
			CSFolder projectsFolder = CSFolder.getRoot("data").getOrCreateSubdirectory("projects");
			projectsFolder.seekExistingFiles();
		 	Iterator<CSFile> projects = projectsFolder.filesIterator();					
		 	while(projects.hasNext()) existingProjects.add(projects.next().name());
		 	
		});
		
	
	}
	
	/**
	 * Attempts to finish, but may not! If the given name is satisfactory, this UI will be finished.
	 */
	private void finish() {
		
		String input = nameInput.toString();
		
		if(channelsPerPixel != -1) {
			
			projectName = input;
			canFinish = true;
			existingProjects.add(input);
			
			String widthString = widthEditor.toString();
			String heightString = heightEditor.toString();
			
			if(!widthString.equals("")) paletteWidth = Integer.parseInt(widthString);
			if(paletteWidth <= 0 || paletteWidth > ArtboardPalette.MAX_WIDTH) paletteWidth = ArtboardPalette.MAX_WIDTH;
			
			if(!heightString.equals("")) paletteHeight = Integer.parseInt(heightString);
			if(paletteHeight <= 0 || paletteHeight > ArtboardPalette.MAX_HEIGHT) paletteHeight = ArtboardPalette.MAX_HEIGHT;

			removeUIOnFinish.run();
			
		}
		
	}
	
	/**
	 * Returns the selected channels per pixel of the project.
	 * 
	 * @return Channels per pixel of the project.
	 */
	public int channelsPerPixel() {
		
		return channelsPerPixel;
		
	}

	private void cancel() {
		
		canFinish = true;
		removeUIOnFinish.run();
		
	}
	
	/**
	 * Returns whether this menu can finish based on its current state.
	 * 
	 * @return Whether this menu can finish based on its current state.
	 */
	public boolean canFinish() {
		
		return canFinish;
		
	}
	
	/**
	 * Returns the name of the project.
	 * 
	 * @return Name of the project.
	 */
	public String get() {
		
		return projectName;
		
	}
	
	/**
	 * Returns the input width of the palette, or the default value of a palette if none is given or an invalid value is given.
	 * 
	 * @return The width of the palette.
	 */
	public int paletteWidth() {
		
		return paletteWidth;
		
	}

	/**
	 * Returns the input height of the palette, or the default value of a palette if none is given or an invalid value is given.
	 * 
	 * @return The height of the palette.
	 */
	public int paletteHeight() {
		
		return paletteHeight;
		
	}

	private int getTotalPixelsFromInputs() {

		String widthString = widthEditor.toString();
		if(widthString.equals("")) return ArtboardPalette.MAX_WIDTH * ArtboardPalette.MAX_HEIGHT;
		
		int width;
		
		try {
			
			width = Integer.parseInt(widthString);
			
		} catch(NumberFormatException e) {
			
			return ArtboardPalette.MAX_WIDTH * ArtboardPalette.MAX_HEIGHT;
			
		}
		
		if(width > ArtboardPalette.MAX_WIDTH) {
			
			width = ArtboardPalette.MAX_WIDTH;
			widthEditor.setStringBuffer(Integer.toString(ArtboardPalette.MAX_WIDTH));
			
		} else if (width <= 0) {
			
			width = 1;
			widthEditor.setStringBuffer("1");
			
		}
		
		String heightString = heightEditor.toString();			
		if(heightString.equals("")) return width * ArtboardPalette.MAX_HEIGHT;
		int height;
		
		try {
			
			height = Integer.parseInt(heightString);
			 
		} catch(NumberFormatException e) {
			
			return width * ArtboardPalette.MAX_HEIGHT;
			
		}
		
		if(height > ArtboardPalette.MAX_HEIGHT) {
			
			height = ArtboardPalette.MAX_HEIGHT;
			heightEditor.setStringBuffer(Integer.toString(ArtboardPalette.MAX_HEIGHT));
			
		} else if (height <= 0) {
			
			height = 1;
			heightEditor.setStringBuffer("1");
			
		}
		
		return width * height;
		
	}
	
}
