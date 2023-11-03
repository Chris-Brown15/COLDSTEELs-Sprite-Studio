/**
 * 
 */
package cs.csss.steamworks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamRemoteStorage.PublishedFileVisibility;
import com.codedisaster.steamworks.SteamUGCDetails;
import com.codedisaster.steamworks.SteamUGCUpdateHandle;

import cs.core.utils.CSFileUtils;
import cs.csss.editor.ScriptType;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFolder;
import cs.csss.misc.files.FileOperations;
import cs.ext.steamworks.SteamLanguages;
import cs.ext.steamworks.SteamApplicationData;
import cs.ext.steamworks.UGC;

/**
 * Used to track and store workshop items created by the user, update new and existing item uploads, and store information from UGC queries.  
 */
public class WorkshopUploadHelper {

	private static final List<SteamPublishedFileID> workshopItems = new ArrayList<>();
	
	/**
	 * Stores the results of the last query for creations by the current user. This is used for the edit item menu.
	 */
	private static SteamUGCDetails[] lastQueryForCreations;
	
	private static boolean newItemUpdateInProgress = false;
	
	/**
	 * Returns whether a new item was created and is ready to be updated.
	 * 
	 * @return Whether a new item was created and is ready to be updated.
	 */
	public static boolean newItemUpdateInProgress() {
		
		return newItemUpdateInProgress;
		
	}
	
	/**
	 * Accepts and stores the results of the last query for items created by the current user.
	 * 
	 * @param lastQueryForCreationsResults — results of the last query for created UGC by the current user
	 */
	public static void acceptLastQueryForUGC(SteamUGCDetails[] lastQueryForCreationsResults) {
		
		lastQueryForCreations = lastQueryForCreationsResults;
		
	}
	
	/**
	 * Returns the array containing the last query for creations by the current user. 
	 * 
	 * @return Array containing the last query for Steam Workshop creations by the current user.
	 */
	public static SteamUGCDetails[] lastQueryForCreations() {
		
		return lastQueryForCreations;
		
	}
	
	private static CSFolder[] createItemFolders(CSFolder parent , WorkshopItemData item) throws IOException {
	
		CSFolder itemFolder = parent.createSubdirectory(item.title());
	 	CSFolder[] folders = itemFolder.createSubdirectories("upload" , "meta");

		FileOperations.copy(item.sourceScriptPath(), folders[0]);
		item.writeWorkshopMetaFile(folders[1]);
		item.writeItemMetaFile(folders[0]);
		
		return folders;
		
	}
	
	/**
	 * Updates a newly created item.
	 * 
	 * @param title — title of the item
	 * @param description — description of the item
	 * @param type — script type for the item
	 * @param visibility — visibility of the item
	 * @param tags — creator defined tags for the item
	 * @param previewImagePath — {@link cs.csss.annotation.Nullable @Nullable} filepath to an image to use for previews of the item
	 * @param scriptPath — filepath to the script to upload
	 */
	public static void updateNewItem(
		Engine engine ,
		UGC ugc ,
		String title ,
		String description ,
		ScriptType type ,
		PublishedFileVisibility visibility ,
		String[] tags ,
		String previewImagePath ,
		String scriptPath
	) {

		SteamPublishedFileID ID = workshopItems.get(workshopItems.size() - 1);
		SteamUGCUpdateHandle updateHandle = ugc.startItemUpdate(SteamApplicationData.steamAppID() , ID);
		
		CSFolder workshopFolder = CSFolder.getRoot("program").getOrCreateSubdirectory("workshop");
		
		newItemUpdateInProgress = false;
		
		WorkshopItemData item = new WorkshopItemData(
			ID , 
			title , 
			description , 
			SteamLanguages.English , 
			visibility , 
			tags , 
			scriptPath , 
			previewImagePath
		);
		
		String uploadFolderPath;
		
		try {
			
			uploadFolderPath = createItemFolders(workshopFolder.getOrCreateSubdirectory("creations") , item)[0].getRealPath();
			createItemFolders(workshopFolder.getOrCreateSubdirectory("backups") , item);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			engine.newNotificationBox("Save to disk failed", "Your Workshop item could not be uploaded because writing it to disk failed.");
			ugc.deleteItem(ID);			
			return;
			
		}
		
		ugc.setItemTitle(updateHandle, title);
		ugc.setItemDescription(updateHandle, description);
		ugc.setItemVisibility(updateHandle, visibility);
		ugc.setItemTags(updateHandle, tags);
		ugc.setItemContent(updateHandle, uploadFolderPath);
		if(previewImagePath != null) ugc.setItemPreview(updateHandle, previewImagePath);		
		ugc.submitItemUpdate(updateHandle, "Initial Upload");
		
	}
	
	/**
	 * Updates an existing UGC item given by {@code item}. The file ID from {@code item} is used to pass the new values for the various item data.
	 * 
	 * @param engine — the engine
	 * @param ugc — the Steam UGC
	 * @param item — the item to update
	 * @param newTitle — the new title of the item
	 * @param newDescription — the new description of the item
	 * @param newVisibility — the new visibility of the item
	 * @param newTags — the new tags for the item
	 * @param newScriptPath — the filepath for the new script for the item
	 * @param newPreviewImagePath — the filepath for the new preview image for the item
	 * @param changeLogInput — a changelog message
	 */
	public static void updateExistingItem(
		Engine engine , 
		UGC ugc , 
		WorkshopItemData item ,
		String newTitle ,
		String newDescription ,
		PublishedFileVisibility newVisibility ,
		String[] newTags ,
		String newScriptPath ,
		String newPreviewImagePath ,
		String changeLogInput
	) {
		
		CSFolder workshop = CSFolder.getRoot("program").getSubdirectory("workshop");
		CSFolder creationsFolder = workshop.getSubdirectory("creations");

		CSFolder itemFolder = creationsFolder.getSubdirectory(item.title());
		CSFolder metaFolder = itemFolder.getSubdirectory("meta");
		CSFolder uploadFolder = itemFolder.getSubdirectory("upload");
				
		itemFolder.rename(newTitle);
				
		String uploadFolderPath = uploadFolder.getRealPath();
		
		WorkshopItemData newItemsData = new WorkshopItemData(
			item.ID(), 
			newTitle, 
			newDescription, 
			SteamLanguages.English, 
			newVisibility, 
			newTags, 
			newScriptPath, 
			newPreviewImagePath
		);
		
		boolean modifiedBackups = false;

		//the only time a backups folder may not be present prior to this method call would be if someone got a workshop item folder from someone
		//else but not the backups folder, in which case going to upload this item for the first time would fail for the new developer.
		CSFolder backups = workshop.getOrCreateSubdirectory("backups");
		
		try {
			
			uploadFolder.clear();
			metaFolder.clear();
			String scriptName = CSFileUtils.toExtendedName(newScriptPath);
			FileOperations.copy(newScriptPath , uploadFolder.getRealPath() + CSFolder.separator + scriptName);
			newItemsData.writeWorkshopMetaFile(metaFolder);
			newItemsData.writeItemMetaFile(uploadFolder);
			//update the backup
			
			CSFolder itemBackup = backups.getSubdirectory(item.title());
			CSFolder backupUpload = itemBackup.getSubdirectory("upload");
			backupUpload.clear();
			modifiedBackups = true;
			CSFolder backupMeta = itemBackup.getSubdirectory("meta");
			backupMeta.clear();

			FileOperations.copy(newScriptPath , backupUpload.getRealPath() + CSFolder.separator + scriptName);
			newItemsData.writeWorkshopMetaFile(backupMeta);
			newItemsData.writeItemMetaFile(backupUpload);
			itemBackup.rename(newTitle);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
			//revert to backup
			if(!modifiedBackups) {
				
				try {
					
					backups.copy(item.title() , workshop.getSubdirectory("creations"));
					
				} catch (IOException e1) {
					
					e1.printStackTrace();
					engine.newNotificationBox(
						"Failed to Update", 
						"Failed to update the item because an IO error occured. Restoring backup failed as well. Data loss likely occurred."
					);
					
					return;
					
				}
				
				engine.newNotificationBox("Failed to Update", "Failed to update the item because an IO error occured.");
				
			}
			
			return;
			
		}
		
		SteamUGCUpdateHandle updateHandle = ugc.startItemUpdate(SteamApplicationData.steamAppID() , item.ID());
		ugc.setItemTitle(updateHandle, newTitle);
		ugc.setItemDescription(updateHandle , newDescription);
		ugc.setItemUpdateLanguage(updateHandle, SteamLanguages.English.APILanguageCode);
		ugc.setItemVisibility(updateHandle, newVisibility);
		ugc.setItemTags(updateHandle, newTags);
		ugc.setItemContent(updateHandle, uploadFolderPath);
		if(newPreviewImagePath != null) ugc.setItemPreview(updateHandle , newPreviewImagePath);
		ugc.submitItemUpdate(updateHandle, changeLogInput);			
		
	}
	
	/**
	 * Creates and returns a {@code WorkshopItemdata} from the given {@code SteamPublishedFileID}.
	 * 
	 * @param publishedFileID — a handle for a published item
	 */
	public static void psuhNewPublishedFileID(SteamPublishedFileID publishedFileID) {
		
		newItemUpdateInProgress = true;
		workshopItems.add(publishedFileID);
				
	}
	
	private WorkshopUploadHelper() {}
	
}