package cs.csss.ui.menus;

import java.io.File;
import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.utils.files.PNG;
import cs.csss.engine.Logging;
import cs.ext.steamworks.Friends;

/**
 * UI menu for uploading a script to the Steam workshop.
 */
public class SteamWorkshopItemUploadMenu extends EditWorkshopItemMenu{
	
	private static boolean menuOpen = false; 
	
	private static final float width = 0.28f , height = 0.75f;
	
	/**
	 * Returns whether an item upload menu is open.
	 * 
	 * @return Whether an item upload menu is open.
	 */
	public static boolean menuOpen() {
		
		return menuOpen;
		
	}
		
	/**
	 * Sets this menu to close. Called to notify of an item creation failure.
	 */
	public static void notifyItemCreationFailure() {
		
		menuOpen = false;
				
	}

	private boolean canFinish = false , finishedValidly = false;
	
	/**
	 * Creates a new Steam workshop Item upload menu. 
	 * 
	 * @param nuklear — the nuklear factory
	 * @param friends — the Steam Friends API
	 */
	public SteamWorkshopItemUploadMenu(CSNuklear nuklear , Friends friends) {
	
		super(nuklear , friends , width , height , true);
		
		menuOpen = true;
	
		//hack to detect invalid state
		ui.attachedLayout((context , stack) -> {
			
			if(!menuOpen) { 
				
				onFinish();
				finishedValidly = false;
				
			}
			
		});
		
		createNameInput();
		createDescriptionInput();
		createPreviewImageMessage();
		createScriptSelect();
		createVisibilitySelection();
		createWorkshopLegalAgreementButton();
		
		CSDynamicRow finishRow = ui.new CSDynamicRow();
		finishRow.new CSButton("Upload" , () -> {
			
			finishedValidly = canFinish = tryFinish();			
			if(finishedValidly) onFinish();
			
		});		
		
		finishRow.new CSButton("Cancel" , this::onFinish);
		
	}

	@Override public void onFinish() {
		
		super.onFinish();
		menuOpen = false;
		
	}
	
	/**
	 * Returns whether this UI was finished such that a new workshop item can be uploaded. 
	 * 
	 * @return {@code true} if this UI was finished such that a new workshop item can be uploaded.
	 */
	public boolean finishedValidly() { 
		
		return finishedValidly;
		
	}
	
	/**
	 * Returns whether this UI is finished, which will be the case if the user closes the menu or completes it validly.
	 * 
	 * @return Whether this UI is finished, which will be the case if the user closes the menu or completes it validly.
	 */
	public boolean finished() {
		
		return canFinish;
		
	}
	
	@Override public void acceptDroppedFilePath(String... filepaths) {
		
		super.defaultAcceptDroppedFilePath(filepaths);
		
		String path = filepaths[0];
		
		try {

			PNG image = new PNG(path);
			uiFileName = "Preview Image: " + path.substring(path.lastIndexOf(File.separator) + 1 , path.length());
			image.shutDown();
			
		} catch(Exception e) {
			
			e.printStackTrace();
			Logging.syserr(path + " is not a filepath to a png.");
			previewImageFilePath = null;
			uiFileName = null;
			
		}
		
		previewImageFilePath = path;
		
	}
	
}
