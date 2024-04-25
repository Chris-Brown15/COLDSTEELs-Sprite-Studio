/**
 * 
 */
package cs.csss.steamworks;

import static cs.csss.misc.utils.MiscUtils.initialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamUGC.ItemInstallInfo;
import com.codedisaster.steamworks.SteamUGC.ItemState;

import cs.csss.engine.Logging;
import cs.ext.steamworks.UGC;
import cs.ext.steamworks.WorkshopItemIDAndInfo;

/**
 * Helper class for providing and handling downloaded Steam workshop items.
 */
public class WorkshopDownloadHelper {

	private static List<WorkshopItemIDAndInfo> installedItemInfo = Collections.synchronizedList(new ArrayList<>());
	
	/**
	 * Gets an array of workshop items that are subscribed. The resulting array contains all the accessable Workshop items returned by Steam.
	 * 
	 * @param ugc — the UGC 
	 * @return Array published file IDs for subscribed Workshop items.
	 */
	public static SteamPublishedFileID[] getSubscribedItems(UGC ugc) {
	
		int numberSubscriptions = ugc.getNumSubscribedItems();		
		SteamPublishedFileID[] subscriptions = new SteamPublishedFileID[numberSubscriptions];
	 	int numberAdded = ugc.getSubscribedItems(subscriptions);
		if(numberAdded < numberSubscriptions) {
			
			SteamPublishedFileID[] trimmed = new SteamPublishedFileID[numberAdded];
			System.arraycopy(subscriptions, 0, trimmed, 0, numberAdded);
			subscriptions = trimmed;
			
		}
	 	
		return subscriptions;
		
	}
	
	public static void downloadUnDownloadedOrOutOfDateItems(UGC ugc , SteamPublishedFileID[] files) {
		
		for(SteamPublishedFileID x : files) {
			
			ItemInstallInfo installInfo = new ItemInstallInfo();
			boolean installed = ugc.getItemInstallInfo(x, installInfo);
			if(!installed) {
				
				ugc.downloadItem(x, false);
				continue;
				
			}
			
			Collection<ItemState> states = ugc.getItemState(x);
			if(states.contains(ItemState.NeedsUpdate)) { 
				
				ugc.downloadItem(x , false);
				continue;
				
			}
			
		}
		
	}
	
	public static ItemInstallInfo[] initializeDownloads(UGC ugc) {
		
		SteamPublishedFileID[] downloads = getSubscribedItems(ugc);
		downloadUnDownloadedOrOutOfDateItems(ugc, downloads);
		ItemInstallInfo[] installInfo = initialize(ItemInstallInfo[]::new , ItemInstallInfo::new , downloads.length);
		for(int i = 0 ; i < downloads.length ; i++) { 
			
			ugc.getItemInstallInfo(downloads[i], installInfo[i]);			
			try {
				
				installedItemInfo.add(new WorkshopItemIDAndInfo(downloads[i] , installInfo[i] , tagsForInstallInfo(installInfo[i])));
				
			} catch (IOException e) {
				
				e.printStackTrace();
				Logging.syserrln("Failed to load a workshop item, continuing.");
				continue;
				
			}
		
		}

		return installInfo;
		
	}
	
	public static void addDownloadedItem(UGC ugc , SteamPublishedFileID ID) {
		
		//we assume that this item is validly installed and indeed pertains to this Steam application because this method should only be called from
		//the SpriteStudioSteamWorkshopCallbacks
		
		ItemInstallInfo forID = new ItemInstallInfo();
		ugc.getItemInstallInfo(ID, forID);
		WorkshopItemIDAndInfo info;
		try {
			
			info = new WorkshopItemIDAndInfo(ID, forID , tagsForInstallInfo(forID));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			Logging.syserrln("Failed to retrieve tags for downloaded item, cannot install now.");
			return;
			
		}
		
		for(int i = 0 ; i < installedItemInfo.size() ; i++) {
		
			WorkshopItemIDAndInfo x = installedItemInfo.get(i);
			if(x.ID.equals(ID)) {
				
				//remove old element and put new one in its place.
				installedItemInfo.remove(i);
				installedItemInfo.add(i, info);
				break;
				
			}
			
		}
		
		installedItemInfo.add(info);
		
	}
	
	public static void removeDownloadedItem(SteamPublishedFileID ID) {
		
		for(int i = 0 ; i < installedItemInfo.size() ; i++) if(installedItemInfo.get(i).ID.equals(ID)) {
			
			installedItemInfo.remove(i);
			return;
			
		}
		
	}
	
	public static void forEachDownload(Consumer<WorkshopItemIDAndInfo> callback) {
		
		installedItemInfo.forEach(callback);
		
	}
	
	private static String[] tagsForInstallInfo(ItemInstallInfo item) throws FileNotFoundException, IOException {
		
		File metaFile = new File(item.getFolder()).listFiles((directory , name) -> name.endsWith(WorkshopItemData.itemFileExtension))[0];
		return WorkshopItemData.loadItemMeta(metaFile.getAbsolutePath());
		
	}
	
	/**
	 * Private Constructor.
	 */
	private WorkshopDownloadHelper() {}

}
