/**
 * 
 */
package cs.csss.ui.menus;

import static sc.core.ui.SCUIConstants.*;
import static cs.csss.ui.utils.UIUtils.toByte;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;

import java.io.File;
import java.util.Collection;

import org.lwjgl.system.MemoryStack;

import com.codedisaster.steamworks.SteamRemoteStorage;
import com.codedisaster.steamworks.SteamFriends.OverlayToWebPageMode;
import com.codedisaster.steamworks.SteamRemoteStorage.PublishedFileVisibility;

import cs.csss.editor.ScriptType;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.ui.utils.UIUtils;
import cs.ext.steamworks.Friends;
import cs.ext.steamworks.UGC;
import sc.core.binary.SCPNG;
import sc.core.ui.SCElements.SCUI.SCDynamicRow;
import sc.core.ui.SCElements.SCUI.SCLayout.SCExtendedText;
import sc.core.ui.SCElements.SCUI.SCLayout.SCGroup;
import sc.core.ui.SCElements.SCUI.SCLayout.SCRadio;
import sc.core.ui.SCElements.SCUI.SCLayout.SCTextEditor;
import sc.core.ui.SCElements.SCUserInterface;
import sc.core.ui.SCNuklear;

/**
 * Contains common UI elements for creating and updating a Workshop item.
 */
abstract class EditWorkshopItemMenu extends DroppedFileAcceptingDialogue {
	
	protected static final int numberOfTypesOfItemsToUpload = 7 , numberOfVisibilityOptions = 3;
	
	protected SCTextEditor nameInput , descriptionEditor;

	protected SCRadio[] scriptTypeRadios , visibilityOptions;

	protected String name , description , scriptFilePath , previewImageFilePath = null , uiFileName = null;
	
	protected SCRadio visibilityType;

	protected ScriptType type = null;
	
	protected SteamRemoteStorage.PublishedFileVisibility visibility = SteamRemoteStorage.PublishedFileVisibility.Public;

	protected final SCUserInterface ui;

	private final SCNuklear nuklear;
	
	private final Friends friends;
	
	private boolean inputNameMatchesExisting;
	
	private final String[] existingNames;
	
	protected final boolean preventExistingNames; 
	
	EditWorkshopItemMenu(SCNuklear nuklear , Friends friends , float width , float height , boolean preventExistingNames) {
		
		this.nuklear = nuklear;
		this.friends = friends;
		ui = new SCUserInterface(nuklear , "Workshop Item Upload" , .5f - (width / 2) , .5f - (height / 2) , width , height);
		ui.flags = UI_TITLED|UI_BORDERED;

		CSFolder workshop = CSFolder.getRoot("program").getOrCreateSubdirectory("workshop");
		existingNames = workshop.getOrCreateSubdirectory("creations").asFile().list();
		this.preventExistingNames = preventExistingNames;
				
		
	}

	protected void createNameInput() {

		SCDynamicRow nameInputRow = ui.new SCDynamicRow();
		nameInputRow.new SCText("Workshop Item Name:" , TEXT_LEFT|TEXT_MIDDLE);
		nameInput = nameInputRow.new SCTextEditor(UGC.PUBLISHED_DOCUMENT_TITLE_MAX , SCNuklear.NO_FILTER);
		
		if(!preventExistingNames) return;
		
		//hack to ensure the nameInput does not match any existing name
		ui.attachedLayout((context) -> {
			
			name = nameInput.toString();
			for(String x : existingNames) if(x.equals(name)) { 
				
				inputNameMatchesExisting = true;
				return;
				
			}
			
			inputNameMatchesExisting = false;
			
		});
		
		SCDynamicRow warning = ui.new SCDynamicRow();
		warning.doLayout = () -> inputNameMatchesExisting;
		warning.new SCText(
			() -> name + " already names a workshop item." , 
			TEXT_LEFT|TEXT_CENTERED , 
			(byte)0xaa , 
			(byte)0xaa , 
			(byte)0x0 , 
			(byte)0xff
		);
		
	}
	
	protected void createDescriptionInput() {
		
		ui.new SCDynamicRow(20).new SCText("Workshop Item Description:" , TEXT_LEFT|TEXT_MIDDLE);
		descriptionEditor = ui.new SCDynamicRow(175).new SCTextEditor(UGC.PUBLISHED_DOCUMENT_DESCRIPTION_MAX , SCNuklear.NO_FILTER); 
		descriptionEditor.flags |= EDIT_MULTILINE;
		
	}
	
	protected void createPreviewImageMessage() {

		SCDynamicRow previewImageMessageRow = ui.new SCDynamicRow(20);
		previewImageMessageRow.new SCText(
			() -> uiFileName != null ? uiFileName : "Drag a PNG into the window to set it as the preview image." , 
			TEXT_LEFT|TEXT_MIDDLE ,
			(byte)0x0 , 
			(byte)0x79 , 
			(byte)0x0 , 
			(byte)0xff
		);
				
	}
	
	protected void createScriptSelect() {

		SCDynamicRow groupsRow = ui.new SCDynamicRow(34 + (numberOfTypesOfItemsToUpload * 26));
		SCGroup radiosGroup = groupsRow.new SCGroup("Type of Script:");
		radiosGroup.ui.flags |= UI_TITLED|UI_UNSCROLLABLE;
			
		scriptTypeRadios = new SCRadio[numberOfTypesOfItemsToUpload];
		scriptTypeRadios[0] = radiosGroup.ui.new SCDynamicRow(20).new SCRadio("Artboard Script" , false , () -> type = ScriptType.ARTBOARD);
		scriptTypeRadios[5] = radiosGroup.ui.new SCDynamicRow(20).new SCRadio("Project Script" , false , () -> type = ScriptType.PROJECT);
		scriptTypeRadios[1] = radiosGroup.ui.new SCDynamicRow(20).new SCRadio("Simple Brush Script" , false , () -> type = ScriptType.SIMPLE);
		scriptTypeRadios[2] = radiosGroup.ui.new SCDynamicRow(20).new SCRadio("Modifying Brush Script" , false , () -> type = ScriptType.MODIFYING);
		scriptTypeRadios[3] = radiosGroup.ui.new SCDynamicRow(20).new SCRadio("Selecting Brush Script" , false , () -> type = ScriptType.SELECTING);
		scriptTypeRadios[4] = radiosGroup.ui.new SCDynamicRow(20).new SCRadio("Exporter Script" , false , () -> type = ScriptType.EXPORTER);
		scriptTypeRadios[6] = radiosGroup.ui.new SCDynamicRow(20).new SCRadio("Palette Script" , false , () -> type = ScriptType.PALETTE);
				
		SCRadio.groupAll(scriptTypeRadios);
		
		SCGroup selectScriptGroup = groupsRow.new SCGroup("Uploadable Scripts");
		selectScriptGroup.ui.flags |= UI_TITLED;
		selectScriptGroup.ui.attachedLayout((context) -> {

			if(type == null) return;
			CSFolder containingFolder = CSFolder.getRoot("program").getSubdirectory("scripts").getOrCreateSubdirectory(type.associatedFolderName);
			Collection<CSFile> files = containingFolder.files();
			files.stream()
				.filter(file -> !Engine.isReservedScriptName(file.name()))
				//TODO: also filter out elements that the user is subscribed to, i.e., things they didnt create 
				.forEach(file -> {
				
					try(MemoryStack stack = MemoryStack.stackPush()) {
						
						nk_layout_row_dynamic(context , 20 , 1);
						if(nk_selectable_text(
							context , 
							file.name() , 
							TEXT_LEFT|TEXT_MIDDLE , 
							toByte(stack , scriptFilePath != null && scriptFilePath.equals(file.getRealPath())))
						) { 
							
						if(scriptFilePath == null) scriptFilePath = file.getRealPath();
						//deselects
						else scriptFilePath = null;
							
					}
					
				}
					
			});
			
		});
		
	}
	
	protected void createVisibilitySelection() {

		ui.new SCDynamicRow(20).new SCText("Visibility:" , TEXT_LEFT|TEXT_MIDDLE);
		visibilityOptions = new SCRadio[numberOfVisibilityOptions];		
		visibilityOptions[0] = ui.new SCDynamicRow(20).new SCRadio("Public" , true , () -> visibility = PublishedFileVisibility.Public);		
		visibilityOptions[1] = ui.new SCDynamicRow(20).new SCRadio("Friends" , false , () -> visibility = PublishedFileVisibility.FriendsOnly);		
		visibilityOptions[2] = ui.new SCDynamicRow(20).new SCRadio("Private" , false , () -> visibility = PublishedFileVisibility.Private);
		
		SCRadio.groupAll(visibilityOptions);

		//visibility tooltips
		UIUtils.toolTip(visibilityOptions[0], "This item is \"visible to everyone.\"");
		UIUtils.toolTip(visibilityOptions[1], "This item is \"visible to friends only.\"");
		UIUtils.toolTip(visibilityOptions[2], "This item is \"only visible to the creator...\"");

	}
	
	protected void createWorkshopLegalAgreementButton() {

		SCDynamicRow workshopLegalAgreementAcceptRow = ui.new SCDynamicRow(20);
		SCExtendedText agreeText = workshopLegalAgreementAcceptRow.new SCExtendedText(
			"By submitting this item, you agree to the [WLA]."
		);
		
		agreeText.colorLast("[WLA].", 0x5555ffff);
		agreeText.changeLastButtonStatus("[WLA]." , () -> {

			friends.activateGameOverlayToWebPage("https://steamcommunity.com/sharedfiles/workshoplegalagreement", OverlayToWebPageMode.Default);
						
		});
		
	}

	protected boolean tryFinish() {
		
		if(preventExistingNames && inputNameMatchesExisting) return false;
		
		name = nameInput.toString();
		if(name.equals("")) return false;
		description = descriptionEditor.toString();
		if(description.equals("")) return false;
		
		if(type == null) return false;
				
		visibilityType = null;
		for(SCRadio x : visibilityOptions) if(x.checked()) visibilityType = x;
		if(visibilityType == null) return false;
		
		if(scriptFilePath == null) return false;
		
		return true;
		
	}
	
	/**
	 * Returns the name the user input in the item name field.
	 * 
	 * @return The name the user input in the item name field.
	 */
	public String name() {
		
		return name;
		
	}

	/**
	 * Returns the description the user input in the item name field.
	 * 
	 * @return The description the user input in the item name field.
	 */
	public String description() {
		
		return description;
		
	}
	
	/**
	 * Returns the type of script the user selected.
	 * 
	 * @return The type of script the user selected.
	 */
	public ScriptType scriptType() {
		
		return type;
		
	}
	
	/**
	 * Returns the visibility value of the workshop item.
	 * 
	 * @return The visibility value of the workshop item.
	 */
	public SteamRemoteStorage.PublishedFileVisibility visibility() {
		
		return visibility;
		
	}
	
	/**
	 * Returns the file path to the preview image for the workshop item.
	 * 
	 * @return File path to the preview image for the workshop item.
	 */
	public String previewFilePath() {
		
		return previewImageFilePath;
		
	}
	
	/**
	 * Returns the file path the script to upload is located at.
	 * 
	 * @return File path the script to upload is located at.
	 */
	public String scriptFilePath() {
		
		return scriptFilePath;
		
	}
	
	/**
	 * Returns the list of tags the user has added to the item.
	 * 
	 * @return Array of strings mapping to the tags the user has added to their item.
	 */
	public String[] tags() {
		
		String[] asStrings = new String[2];
		asStrings[0] = type.asTagName();
		asStrings[asStrings.length - 1] = "Script";
		return asStrings;
		
	}
	
	@Override public void onFinish() {
		
		super.onFinish();
		nuklear.removeUserInterface(ui);
		ui.shutDown();
		
	}
	
	@Override public void acceptDroppedFilePath(String... filepaths) {

		defaultAcceptDroppedFilePath(filepaths);

		String path = filepaths[0];
		if(!path.endsWith("png")) return;
		try {
			
			SCPNG loadedPNG = new SCPNG(path);
			loadedPNG.shutDown();
			
		} catch(Exception e ) {
			
			if(Engine.isDebug()) e.printStackTrace();
			return;
		}
		
		previewImageFilePath = path;		
		uiFileName = new File(path).getName();
				
	}

}
