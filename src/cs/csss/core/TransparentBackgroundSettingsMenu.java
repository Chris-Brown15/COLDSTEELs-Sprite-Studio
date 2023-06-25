package cs.csss.core;

import static cs.core.ui.CSUIConstants.*;

import cs.core.ui.CSNuklear;
import cs.csss.artboard.ArtboardTexture;
import cs.csss.editor.Editor;
import cs.csss.editor.events.ChangeBackgroundCheckerSizeEvent;

public class TransparentBackgroundSettingsMenu extends OptionMenu {

	private int 
		backgroundWidth = ArtboardTexture.backgroundCheckerWidth ,
		backgroundHeight = ArtboardTexture.backgroundCheckerHeight
	;
	
	public TransparentBackgroundSettingsMenu(Editor editor , CSNuklear nuklear , CSSSProject project) {

		super(nuklear.new CSUserInterface(
			"Transparent Background Options" , 
			0.5f - (0.33f / 2) , 
			0.5f - (0.35f / 2) , 
			0.33f , 
			0.35f
		));
		
		ui.options = UI_TITLED|UI_BORDERED|UI_UNSCROLLABLE;		
		
		super.addOptionEntry(
			"Width:" , 
			() -> backgroundWidth + "" , 
			4 , 
			CSNuklear.DECIMAL_FILTER , 
			res -> backgroundWidth = Integer.parseInt(res)
		);
		
		super.addOptionEntry(
			"Height:" , 
			() -> backgroundHeight + "" , 
			4 , 
			CSNuklear.DECIMAL_FILTER , 
			res -> backgroundHeight = Integer.parseInt(res)
		);
		
		ui.new CSDynamicRow().new CSButton("Finish" , () -> {

			if(project != null) editor.eventPush(new ChangeBackgroundCheckerSizeEvent(project , backgroundWidth , backgroundHeight));
			
			nuklear.removeUserInterface(ui);
			ui.shutDown();
			
		});
		
	}

}
