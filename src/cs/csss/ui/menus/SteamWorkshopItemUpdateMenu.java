/**
 * 
 */
package cs.csss.ui.menus;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;

import java.io.IOException;

import com.codedisaster.steamworks.SteamPublishedFileID;

import static cs.core.ui.CSUIConstants.*;

import static cs.csss.ui.utils.UIUtils.toByte;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSGroup;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.utils.CSFileUtils;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFolder;
import cs.csss.steamworks.WorkshopItemData;
import cs.ext.steamworks.Friends;

/**
 * Menu for updating a Steam workshop item.
 */
public class SteamWorkshopItemUpdateMenu extends EditWorkshopItemMenu {

	private static CSFolder creations = CSFolder.getRoot("program").getOrCreateSubdirectory("workshop").getSubdirectory("creations");
	
	private static final float width = 0.35f , height = 0.75f;
	
	private boolean readyToClose = false , finishedValidly;
	
	private WorkshopItemData item;
		
	private CSTextEditor changelogInput;
	
	/**
	 * 
	 * @param nuklear — the Nuklear factory
	 */
	public SteamWorkshopItemUpdateMenu(CSNuklear nuklear , Friends friends , Engine engine) {

		super(nuklear , friends , width , height , false);
		
//		engine.getCreatedWorkshopItems();
		
		CSGroup selectItemGroup = ui.new CSDynamicRow(175).new CSGroup("Select a workshop item to update.");
		selectItemGroup.ui.options |= UI_BORDERED|UI_TITLED;
		selectItemGroup.ui.attachedLayout((context , stack) -> {
			
			CSFolder creationsFolder = CSFolder.getRoot("program").getOrCreateSubdirectory("workshop").getOrCreateSubdirectory("creations");
			creationsFolder.subdirectories().forEachRemaining(x -> {
				
				nk_layout_row_dynamic(context , 20 , 1);				
				if(nk_selectable_text(context , x.name , TEXT_LEFT|TEXT_MIDDLE , toByte(stack , item != null && x.name.equals(item.title())))) {
					
					try {
						
						CSFolder uploadFolder = creations.getSubdirectory(x.name).getSubdirectory("meta");						
						item = WorkshopItemData.loadWorkshopMeta(uploadFolder.getRealPath() + CSFolder.separator + x.name + WorkshopItemData.metaFileExtension);
						nameInput.setStringBuffer(item.title());
						descriptionEditor.setStringBuffer(item.description());
						previewImageFilePath = item.previewImagePath();
						if(item.previewImagePath().equals("null")) previewImageFilePath = null;
						else uiFileName = CSFileUtils.toExtendedName(previewImageFilePath);
						scriptFilePath = item.sourceScriptPath();						
						
					} catch (IOException e) {
						
						e.printStackTrace();
						
					}
					
				}
				
			});
			
		});
		
		createNameInput();
		createDescriptionInput();
		createPreviewImageMessage();
		createScriptSelect();
		createVisibilitySelection();

		CSDynamicRow changelogRow = ui.new CSDynamicRow();
		changelogRow.new CSText("Change Note:" , TEXT_LEFT|TEXT_CENTERED);
		changelogInput = changelogRow.new CSTextEditor(99 , CSNuklear.NO_FILTER);
		
		createWorkshopLegalAgreementButton();		
				
	 	CSDynamicRow finishRow = ui.new CSDynamicRow();
		finishRow.new CSButton("Update" , () -> {
			
			readyToClose = finishedValidly = tryFinish();
			if(readyToClose) onFinish();
			
		});
		
		finishRow.new CSButton("Cancel" , () -> {
			
			readyToClose = true;
			super.onFinish();
			
		});
	 	
	}
	
	/**
	 * Returns whether this menu is ready to close, which will be the case if the Cancel button was pressed or the Update button was pressed and the
	 * menu is in a state to return all the data needed to update the item.
	 * 
	 * @return Whether this menu is ready to close.
	 */
	public boolean readyToClose() {
		
		return readyToClose;
		
	}
	
	/**
	 * Returns the published file ID associated with this workshop item. 
	 * 
	 * @return File ID object associated with this item.
	 */
	public SteamPublishedFileID selectedItem() {
		
		return item.ID();
		
	}
	
	/**
	 * Returns the selected workshop item.
	 * 
	 * @return The selected workshop item.
	 */
	public WorkshopItemData item() {
		
		return item;
		
	}
	
	/**
	 * Returns whether this menu finished in such a state that it could produce an update.
	 *  
	 * @return Whether this menu finished in such a state that it could produce an update.
	 */
	public boolean finishedValidly() {
		
		return finishedValidly;
				
	}
	
	/**
	 * Gets and returns the input to the changelog field. This can also return {@code null} if no input was given, which the Steam API takes to mean
	 * no changelog should be attached to this update.
	 * 
	 * @return Changelog input.
	 */
	public String changeLogInput() {
		
		String input = changelogInput.toString();
		if(input.equals("")) return null;
		return input;
		
	}
	
}
