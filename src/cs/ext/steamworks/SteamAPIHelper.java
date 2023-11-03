/**
 * 
 */
package cs.ext.steamworks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamUGC.ItemInstallInfo;
import com.codedisaster.steamworks.SteamUGCDetails;

/**
 * Provides access and wrapping over reflection for the {@link com.codedisaster.steamworks.SteamAPI SteamAPI} class.
 */
public final class SteamAPIHelper {

	private static Method isIsNativeAPILoaded;
	
	/**
	 * Returns whether the native APIs for Steamworks were loaded correctly.
	 * 
	 * @return Whether the native APIs for Steamworks were loaded correctly.
	 */
	public static boolean wasNativeAPILoaded() {

		if(isIsNativeAPILoaded == null) isIsNativeAPILoaded = getAndMakeAccessable("isIsNativeAPILoaded");
		
		try {
			
			return (boolean) isIsNativeAPILoaded.invoke(null);
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

			throw new IllegalStateException(e);
			
		}
		
	}
	
	private static Method getAndMakeAccessable(String name) {

		Method[] methods = SteamAPI.class.getDeclaredMethods();
		for(Method x : methods) {
			
			if(x.getName().equals(name)) {
				
				x.setAccessible(true);
				return x;
				
			}
			
		}
		
		return null;
		
	}
	
	public static String steamUGCDetailsDetailedString(SteamUGCDetails detail) {
		
		return String.format(
			"SteamUGCDetails with ID: %s , result: %s, fileType: %s, title: %s, description: %s, timeCreated: %d, timeUpdated: %d, tags: %s, "
			+ "fileSize: %d, previewFileSize: %d",
			detail.getPublishedFileID().toString() ,
			detail.getResult().toString() ,
			detail.getFileType().toString() ,
			detail.getTitle() ,
			detail.getDescription() ,
			detail.getTimeCreated() , 
			detail.getTimeUpdated() ,
			detail.getTags() ,
			detail.getFileSize() ,
			detail.getPreviewFileSize()
		);
		
	}
	
	public static String itemInstallInfoDetailedString(ItemInstallInfo info) {
		
		return String.format("ItemInstallInfo with file path: %s, size on disk: %d", info.getFolder() , info.getSizeOnDisk());
		
	}
	
	public static boolean compareItemInstallInfos(ItemInstallInfo info1 , ItemInstallInfo info2) {
		
		return info1.getFolder().equals(info2.getFolder()) && info1.getSizeOnDisk() == info2.getSizeOnDisk();
		
	}
	
	private SteamAPIHelper() {}
	
}
