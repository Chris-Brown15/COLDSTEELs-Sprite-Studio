/**
 * 
 */
package cs.csss.ui.menus;

import static cs.core.ui.CSUIConstants.EDIT_MULTILINE;
import static cs.core.ui.CSUIConstants.TEXT_LEFT;
import static cs.core.ui.CSUIConstants.TEXT_MIDDLE;
import static cs.core.ui.CSUIConstants.TEXT_CENTERED;
import static cs.core.ui.CSUIConstants.UI_BORDERED;
import static cs.core.ui.CSUIConstants.UI_TITLED;
import static cs.core.ui.CSUIConstants.UI_UNSCROLLABLE;
import static cs.csss.ui.utils.UIUtils.toByte;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_selectable_text;

import java.util.Collection;

import com.codedisaster.steamworks.SteamRemoteStorage;
import com.codedisaster.steamworks.SteamFriends.OverlayToWebPageMode;
import com.codedisaster.steamworks.SteamRemoteStorage.PublishedFileVisibility;

import cs.core.ui.CSNuklear;
import cs.core.ui.CSNuklear.CSUI.CSDynamicRow;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSExtendedText;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSGroup;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSRadio;
import cs.core.ui.CSNuklear.CSUI.CSLayout.CSTextEditor;
import cs.core.utils.CSFileUtils;
import cs.core.utils.files.PNG;
import cs.csss.editor.ScriptType;
import cs.csss.engine.Engine;
import cs.csss.misc.files.CSFile;
import cs.csss.misc.files.CSFolder;
import cs.csss.ui.elements.FullAccessCSUI;
import cs.csss.ui.utils.UIUtils;
import cs.ext.steamworks.Friends;
import cs.ext.steamworks.UGC;

/**
 * Contains common UI elements for creating and updating a Workshop item.
 */
abstract class EditWorkshopItemMenu extends DroppedFileAcceptingDialogue {
	
	protected static final int numberOfTypesOfItemsToUpload = 7 , numberOfVisibilityOptions = 3;
	
	protected CSTextEditor nameInput , descriptionEditor;

	protected CSRadio[] scriptTypeRadios , visibilityOptions;

	protected String name , description , scriptFilePath , previewImageFilePath = null , uiFileName = null;
	
	protected CSRadio visibilityType;

	protected ScriptType type = null;
	
	protected SteamRemoteStorage.PublishedFileVisibility visibility = SteamRemoteStorage.PublishedFileVisibility.Public;

	protected final FullAccessCSUI ui;

	private final CSNuklear nuklear;
	
	private final Friends friends;
	
	private boolean inputNameMatchesExisting;
	
	private final String[] existingNames;
	
	protected final boolean preventExistingNames; 
	
	EditWorkshopItemMenu(CSNuklear nuklear , Friends friends , float width , float height , boolean preventExistingNames) {
		
		this.nuklear = nuklear;
		this.friends = friends;
		ui = new FullAccessCSUI(nuklear , "Workshop Item Upload" , .5f - (width / 2) , .5f - (height / 2) , width , height);
		ui.options = UI_TITLED|UI_BORDERED;

		existingNames = CSFolder.getRoot("program").getOrCreateSubdirectory("workshop").getOrCreateSubdirectory("creations").asFile().list();
		this.preventExistingNames = preventExistingNames;
		
	}

	protected void createNameInput() {

		CSDynamicRow nameInputRow = ui.new CSDynamicRow();
		nameInputRow.new CSText("Workshop Item Name:" , TEXT_LEFT|TEXT_MIDDLE);
		nameInput = nameInputRow.new CSTextEditor(UGC.PUBLISHED_DOCUMENT_TITLE_MAX , CSNuklear.NO_FILTER);
		
		if(!preventExistingNames) return;
		
		//hack to ensure the nameInput does not match any existing name
		ui.attachedLayout((context , stack) -> {
			
			name = nameInput.toString();
			for(String x : existingNames) if(x.equals(name)) { 
				
				inputNameMatchesExisting = true;
				return;
				
			}
			
			inputNameMatchesExisting = false;
			
		});
		
		CSDynamicRow warning = ui.new CSDynamicRow();
		warning.doLayout = () -> inputNameMatchesExisting;
		warning.new CSText(
			() -> name + " already names a workshop item." , 
			TEXT_LEFT|TEXT_CENTERED , 
			(byte)0xaa , 
			(byte)0xaa , 
			(byte)0x0 , 
			(byte)0xff
		);
		
	}
	
	protected void createDescriptionInput() {
		
		ui.new CSDynamicRow(20).new CSText("Workshop Item Description:" , TEXT_LEFT|TEXT_MIDDLE);
		descriptionEditor = ui.new CSDynamicRow(175).new CSTextEditor(UGC.PUBLISHED_DOCUMENT_DESCRIPTION_MAX , CSNuklear.NO_FILTER); 
		descriptionEditor.editorOptions |= EDIT_MULTILINE;
		
	}
	
	protected void createPreviewImageMessage() {

		CSDynamicRow previewImageMessageRow = ui.new CSDynamicRow(20);
		previewImageMessageRow.new CSText(
			() -> uiFileName != null ? uiFileName : "Drag a PNG into the window to set it as the preview image." , 
			TEXT_LEFT|TEXT_MIDDLE ,
			(byte)0x0 , 
			(byte)0x79 , 
			(byte)0x0 , 
			(byte)0xff
		);
				
	}
	
	protected void createScriptSelect() {

		CSDynamicRow groupsRow = ui.new CSDynamicRow(34 + (numberOfTypesOfItemsToUpload * 26));
		CSGroup radiosGroup = groupsRow.new CSGroup("Type of Script:");
		radiosGroup.ui.options |= UI_TITLED|UI_UNSCROLLABLE;
			
		scriptTypeRadios = new CSRadio[numberOfTypesOfItemsToUpload];
		scriptTypeRadios[0] = radiosGroup.ui.new CSDynamicRow(20).new CSRadio("Artboard Script" , false , () -> type = ScriptType.ARTBOARD);
		scriptTypeRadios[5] = radiosGroup.ui.new CSDynamicRow(20).new CSRadio("Project Script" , false , () -> type = ScriptType.PROJECT);
		scriptTypeRadios[1] = radiosGroup.ui.new CSDynamicRow(20).new CSRadio("Simple Brush Script" , false , () -> type = ScriptType.SIMPLE);
		scriptTypeRadios[2] = radiosGroup.ui.new CSDynamicRow(20).new CSRadio("Modifying Brush Script" , false , () -> type = ScriptType.MODIFYING);
		scriptTypeRadios[3] = radiosGroup.ui.new CSDynamicRow(20).new CSRadio("Selecting Brush Script" , false , () -> type = ScriptType.SELECTING);
		scriptTypeRadios[4] = radiosGroup.ui.new CSDynamicRow(20).new CSRadio("Exporter Script" , false , () -> type = ScriptType.EXPORTER);
		scriptTypeRadios[6] = radiosGroup.ui.new CSDynamicRow(20).new CSRadio("Palette Script" , false , () -> type = ScriptType.PALETTE);
				
		CSRadio.groupAll(scriptTypeRadios);
		
		CSGroup selectScriptGroup = groupsRow.new CSGroup("Uploadable Scripts");
		selectScriptGroup.ui.options |= UI_TITLED;
		selectScriptGroup.ui.attachedLayout((context , stack) -> {

			if(type == null) return;
			CSFolder containingFolder = CSFolder.getRoot("program").getSubdirectory("scripts").getSubdirectory(type.associatedFolderName);
			Collection<CSFile> files = containingFolder.files();
			files.stream()
				.filter(file -> !Engine.isReservedScriptName(file.name()))
				//TODO: also filter out elements that the user is subscribed to, i.e., things they didnt create 
				.forEach(file -> {
				
					nk_layout_row_dynamic(context , 20 , 1);
					if(nk_selectable_text(
						context , 
						file.name() , 
						TEXT_LEFT|TEXT_MIDDLE , 
						toByte(stack , scriptFilePath != null && scriptFilePath.equals(file.getRealPath())))
					) { 
						
						scriptFilePath = file.getRealPath();
						
					}
					
			});
			
		});
		
	}
	
	protected void createVisibilitySelection() {

		ui.new CSDynamicRow(20).new CSText("Visibility:" , TEXT_LEFT|TEXT_MIDDLE);
		visibilityOptions = new CSRadio[numberOfVisibilityOptions];		
		visibilityOptions[0] = ui.new CSDynamicRow(20).new CSRadio("Public" , true , () -> visibility = PublishedFileVisibility.Public);		
		visibilityOptions[1] = ui.new CSDynamicRow(20).new CSRadio("Friends" , false , () -> visibility = PublishedFileVisibility.FriendsOnly);		
		visibilityOptions[2] = ui.new CSDynamicRow(20).new CSRadio("Private" , false , () -> visibility = PublishedFileVisibility.Private);
		
		CSRadio.groupAll(visibilityOptions);

		//visibility tooltips
		UIUtils.toolTip(visibilityOptions[0], "This item is \"visible to everyone.\"");
		UIUtils.toolTip(visibilityOptions[1], "This item is \"visible to friends only.\"");
		UIUtils.toolTip(visibilityOptions[2], "This item is \"only visible to the creator...\"");

	}
	
	protected void createWorkshopLegalAgreementButton() {

		CSDynamicRow workshopLegalAgreementAcceptRow = ui.new CSDynamicRow(20);
		CSExtendedText agreeText = workshopLegalAgreementAcceptRow.new CSExtendedText(
			"By submitting this item, you agree to the [WLA]."
		);
		
		agreeText.colorLast("[WLA].", 0x5555ffff);
		agreeText.makeLastClickable("[WLA].", () -> {

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
		for(CSRadio x : visibilityOptions) if(x.checked()) visibilityType = x;
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
			
			PNG loadedPNG = new PNG(path);
			loadedPNG.shutDown();
			
		} catch(Exception e ) {
			
			if(Engine.isDebug()) e.printStackTrace();
			return;
		}
		
		previewImageFilePath = path;
		uiFileName = CSFileUtils.toExtendedName(path);
		
	}

}
