/**
 * 
 */
package cs.ext.steamworks;

import java.util.Objects;

import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamUGC.ItemInstallInfo;

import cs.csss.misc.utils.MiscUtils;

/**
 * Container for an ItemInstallInfo and a FileID.
 */
public class WorkshopItemIDAndInfo {

	public final SteamPublishedFileID ID;
	private ItemInstallInfo info;
	private String[] tags;
	
	/**
	 * Creates a new item info with the given ID and info.
	 */
	public WorkshopItemIDAndInfo(SteamPublishedFileID ID , ItemInstallInfo info , String[] tags) {
		
		Objects.requireNonNull(ID);
		Objects.requireNonNull(info);
		Objects.requireNonNull(tags);
		
		this.ID = ID;
		this.info = info;
		this.tags = tags;
		
	}

	/**
	 * Returns the file path to the folder containing this item's contents.
	 * 
	 * @return The file path to the folder containing this item's contents.
	 * 
	 * @see com.codedisaster.steamworks.SteamUGC.ItemInstallInfo#getFolder()
	 */
	public String folder() {
		
		return info.getFolder();
		
	}

	/**
	 * Returns the size on disk of the folder containing this item's contents.
	 * 
	 * @return The size on disk of the folder containing this item's contents.
	 * 
	 * @see com.codedisaster.steamworks.SteamUGC.ItemInstallInfo#getSizeOnDisk()
	 */
	public int sizeOnDisk() {
	
		return info.getSizeOnDisk();
		
	}

	/**
	 * Returns whether this {@code WorkshopItemIDAndInfo} instance has a tag equaling {@code tag}.
	 * 
	 * @param tag — a tag string
	 * @return {@code true} if this {@code WorkshopItemIDAndInfo} instance has the given tag.
	 */
	public boolean hasTag(String tag) {
		
		String retrieved = MiscUtils.get(x -> x.equals(tag), tags);
		return retrieved != null;
		
	}
	
	@Override public String toString() {
		
		return String.format("Workshop Item ID %s filepath, %s, size: %d", ID.toString() , info.getFolder() , info.getSizeOnDisk());
		
	}
	
	@Override public boolean equals(Object other) {
		
		if(other instanceof WorkshopItemIDAndInfo otherInfo) { 
			
			return otherInfo.ID.toString().equals(ID.toString()) && folder().equals(otherInfo.folder()) && otherInfo.sizeOnDisk() == sizeOnDisk();
		
		}
		
		return false;
		
	}
	
}
