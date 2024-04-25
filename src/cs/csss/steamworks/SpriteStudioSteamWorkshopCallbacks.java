/**
 * 
 */
package cs.csss.steamworks;

import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUGCCallback;
import com.codedisaster.steamworks.SteamUGCDetails;
import com.codedisaster.steamworks.SteamUGCQuery;

import cs.csss.engine.Engine;
import cs.csss.engine.Logging;
import cs.csss.ui.menus.SteamWorkshopItemUploadMenu;
import cs.ext.steamworks.SteamAPIHelper;
import cs.ext.steamworks.SteamApplicationData;
import cs.ext.steamworks.UGC;

/**
 * Contains implementations for some callbacks which Steam will make when interacting with the Steam API.
 */
public class SpriteStudioSteamWorkshopCallbacks implements SteamUGCCallback {

	private static final String failureNotificationTitile = "Cannot Create Item";

	private final Engine engine;
	private UGC ugc;
	
	/**
	 * Creates a new implementation of the Steam UGC callbacks. 
	 *  
	 * @param engine — the engine
	 */
	public SpriteStudioSteamWorkshopCallbacks(Engine engine) {
	
		this.engine = engine;
		
	}	
	
	/**
	 * Sets the UGC instance needed for some method implementations of this class.
	 * 
	 * @param ugc — existing ugc, preferably the one this instance was used to create
	 */
	public void setUGC(UGC ugc) {
		
		this.ugc = ugc;
		
	}
	
	@Override public void onUGCQueryCompleted(
		SteamUGCQuery query, 
		int numResultsReturned, 
		int totalMatchingResults,
		boolean isCachedData, 
		SteamResult result
	) {

		if(result != SteamResult.OK) {
			
			engine.newNotificationBox("Query Failed", "Steam query result is: " + result.name());
			return;
			
		}
		
		if(!query.isValid()) {
			
			engine.newNotificationBox("Query Failed", "Steam query is invalid.");
			return;
			
		}
		
		SteamUGCDetails[] allDetails = new SteamUGCDetails[numResultsReturned];
		
		for(int i = 0 ; i < numResultsReturned ; i++) {
			
			SteamUGCDetails x = new SteamUGCDetails(); 
			if(!ugc.getQueryUGCResult(query, totalMatchingResults, x)) {
			
				engine.newNotificationBox("Query Failed", "Steam query result task failed at index " + i + ".");
				return;
				
			}
			
			allDetails[i] = x;
						
		}
		
		Logging.sysDebugln("Query to Steam Workshop User Creations Length: " + allDetails.length);
		Logging.sysDebugln("Query Results:");
		for(SteamUGCDetails x : allDetails) Logging.sysDebugln("\t" + SteamAPIHelper.steamUGCDetailsDetailedString(x));
		WorkshopUploadHelper.acceptLastQueryForUGC(allDetails);
		
	}

	@Override public void onSubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) {

		Logging.sysDebugln("Item Subscribed Result: " + result.name().toLowerCase());
		if(result != SteamResult.OK) {
		
			engine.newNotificationBox(
				"Subscription Download Failed", "A subscription to a Workshop item was detected but an error occurred resulting in it not being "
			  + "downloadable."
			);
			return;
			
		}
		
		Engine.THE_THREADS.submit(() -> {
			
			WorkshopDownloadHelper.downloadUnDownloadedOrOutOfDateItems(ugc, WorkshopDownloadHelper.getSubscribedItems(ugc));
			
		});

	}

	@Override public void onUnsubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result) {

		Logging.sysDebugln("Item Unsubscribed Result: " + result.name().toLowerCase());
				
	}

	@Override public void onRequestUGCDetails(SteamUGCDetails details, SteamResult result) {}

	@Override public void onCreateItem(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {

		if(needsToAcceptWLA) engine.startWorkshopLegalAgreementPopup();
		
		switch(result) {
			case OK -> WorkshopUploadHelper.psuhNewPublishedFileID(publishedFileID);
			case InsufficientPrivilege -> notifyFailure("Your account is blocked, contact Steam Support.");
			case Banned -> notifyFailure("Your account is VAC banned so you cannot upload.");
			case Timeout -> notifyFailure("Timeout occurred, try again."); 
			case ServiceUnavailable -> notifyFailure("Workshop servers are unavailable, try again later.");
			case NotLoggedOn -> notifyFailure("You are not currently logged in to Steam. Log in first.");
			case InvalidParam -> notifyFailure("A submisison field contains invalid data.");
			case AccessDenied -> notifyFailure("There was a problem trying to save data; access was denied.");
			case LimitExceeded -> notifyFailure("You have uploaded more data than the Sprite Studio quota allows, contact Sprite Studio Support.");
			case FileNotFound -> notifyFailure("The uploaded file could not be found");
			case DuplicateRequest -> notifyFailure("A duplicate request was made, give the servers a moment to update.");
			case DuplicateName -> notifyFailure("You already have an item with the given name.");
			case ServiceReadOnly -> notifyFailure("You recently changed acount information so you need to try again in a few days.");
			default -> throw new IllegalArgumentException("Unexpected value: " + result);
			
		}

	}

	@Override public void onSubmitItemUpdate(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {
		
		if(result == SteamResult.OK) engine.newNotificationBox("Upload Successful", "Your item was uploaded or updated successfully.");
		else engine.newNotificationBox(failureNotificationTitile, "An error occurred in updating or uploading your item.");
		if(needsToAcceptWLA) engine.startWorkshopLegalAgreementPopup();
		
	}

	@Override public void onDownloadItemResult(int appID, SteamPublishedFileID publishedFileID, SteamResult result) {
		
		if(appID != SteamApplicationData.steamAppID()) return;
		
		if(result != SteamResult.OK) { 
			
			engine.newNotificationBox("Download Failed", "Downloading an item from the Workshop failed");
			return;
			
		}
	
		WorkshopDownloadHelper.addDownloadedItem(ugc , publishedFileID);
		
	}

	@Override public void onUserFavoriteItemsListChanged(SteamPublishedFileID publishedFileID, boolean wasAddRequest, SteamResult result) {}

	@Override public void onSetUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp, SteamResult result) {}

	@Override public void onGetUserItemVote(
		SteamPublishedFileID publishedFileID, 
		boolean votedUp, 
		boolean votedDown, 
		boolean voteSkipped, 
		SteamResult result
	) {}

	@Override public void onStartPlaytimeTracking(SteamResult result) {}

	@Override public void onStopPlaytimeTracking(SteamResult result) {}

	@Override public void onStopPlaytimeTrackingForAllItems(SteamResult result) {}

	@Override public void onDeleteItem(SteamPublishedFileID publishedFileID, SteamResult result) {
		
		if(result != SteamResult.OK) engine.newNotificationBox("Deletion Failed", "Item deletion failed.");
		WorkshopDownloadHelper.removeDownloadedItem(publishedFileID);
		
	}

	private void notifyFailure(String message) {
		
		engine.newNotificationBox(failureNotificationTitile, message);
		SteamWorkshopItemUploadMenu.notifyItemCreationFailure();
		
	}
	
}
