/**
 * 
 */
package cs.csss.steamworks;

import static cs.csss.misc.files.FileOperations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamRemoteStorage.PublishedFileVisibility;

import cs.csss.annotation.InDevelopment;
import cs.csss.misc.files.CSFolder;
import cs.csss.misc.utils.MiscUtils;
import cs.ext.steamworks.SteamLanguages;

/**
 * Container for all the data needed to publish or update a Workshop Item.
 */
@InDevelopment public record WorkshopItemData(
	SteamPublishedFileID ID , 
	String title , 
	String description , 
	SteamLanguages langauge , 
	PublishedFileVisibility visibility , 
	String[] tags , 
	String sourceScriptPath , 
	String previewImagePath
) {

	/**
	 * File extension for workshop item meta data files.
	 */
	public static final String metaFileExtension = ".ctswm" , itemFileExtension = ".ctsim";
	
	/**
	 * Writes a Workshop meta file to disk from the given parameters. 
	 * 
	 * @param metaFolder — folder to write the new file in
	 * @param ID — Steam-provided Workshop item ID
	 * @param title — title of the item
	 * @param description — description of the item
	 * @param language — language the item pertains to
	 * @param visibility — visibility of the item
	 * @param tags — tags for the item
	 * @param sourceScriptPath — file path to the script to upload
	 * @param previewImagePath — file path to a preview image for this item
	 * @throws IOException if an IO error occurs while writing this file to disk.
	 */
	public static void writeWorkshopMeta(
		CSFolder metaFolder , 
		SteamPublishedFileID ID , 
		String title , 
		String description , 
		SteamLanguages language , 
		PublishedFileVisibility visibility ,
		String[] tags ,
		String sourceScriptPath ,
		String previewImagePath
	) throws IOException {
		
		File sourceFile = new File(metaFolder.getRealPath() + CSFolder.separator + title + metaFileExtension);
		
		try(FileOutputStream writer = new FileOutputStream(sourceFile)) {
			
			putLong(MiscUtils.parseHexLong(ID.toString() , 0) , writer);
			putString(title , writer);
			putString(description, writer);
			putInt(language.ordinal() , writer);
			putString(visibility.name() , writer);
			putStringArray(tags , writer);
			putString(sourceScriptPath , writer);
			putString(previewImagePath , writer);
			
		} 
		
		sourceFile.setWritable(false);
		
	}
	
	/**
	 * Writes an item meta file to the given folder. This file simply contains tags of the script.
	 * 
	 * @param uploadFolder — folder to be uploaded to Steam
	 * @param title — title of the item being written for
	 * @param tags — the array of tags defining the item being uploaded
	 * @throws IOException if an IO error occurs while writing to the file.
	 */
	public static void writeItemMeta(CSFolder uploadFolder , String title , String[] tags) throws IOException {
		
		File sourceFile = new File(uploadFolder.getRealPath() + CSFolder.separator + title + itemFileExtension);
		
		try(FileOutputStream writer = new FileOutputStream(sourceFile)) {
			
			putStringArray(tags , writer);
			
		}
		
	}
	
	/**
	 * Loads and returns a {@code WorkshopItemData} from {@code metaFilePath}, a filepath to a {@code .ctswm} file.
	 * 
	 * @param metaFilePath — filepath to a workshop meta file
	 * @return {@code WorkshopItemData} whose fields are given by the file at {@code metaFilePath}.
	 * @throws NullPointerException if {@code metaFilePath} is {@code null}.
	 * @throws FileNotFoundException if {@code metaFilePath} does not exist.
	 * @throws IllegalArgumentException if {@code metaFilePath} does not end with the workshop meta file extension.
	 * @throws IOException if an IO error occurs while reading the file.
	 */
	public static WorkshopItemData loadWorkshopMeta(String metaFilePath) throws FileNotFoundException, IOException {
		
		Objects.requireNonNull(metaFilePath);
		if(!metaFilePath.endsWith(metaFileExtension)) throw new IllegalArgumentException(metaFilePath + " is not Workshop Meta File.");
		 
		try(FileInputStream reader = new FileInputStream(metaFilePath)) {
		
			return new WorkshopItemData(
				new SteamPublishedFileID(getLong(reader)) , 
				getString(reader) , 
				getString(reader) , 
				SteamLanguages.values()[getInt(reader)] ,
				PublishedFileVisibility.valueOf(getString(reader)) , 
				getStringArray(reader) ,
				getString(reader) ,
				getString(reader));
			
		} 		
				
	}
	
	/**
	 * Reads and returns the item metadata located at {@code metaFilePath}.
	 * 
	 * @param metaFilePath — filepath to a {@code .ctsim} file
	 * @return Array of strings containing metadata.
	 * @throws NullPointerException if {@code metaFilePath} is null.
	 * @throws FileNotFoundException if {@code metaFilePath} does not point to a file.
	 * @throws IllegalArgumentException if {@code metaFilePath} is not a {@code .ctsim} file.
	 * @throws IOException if an IO error occurs during reading.
	 */
	public static String[] loadItemMeta(String metaFilePath) throws FileNotFoundException, IOException {
		
		Objects.requireNonNull(metaFilePath);
		if(!metaFilePath.endsWith(itemFileExtension)) throw new IllegalArgumentException(metaFilePath + " is not an Item Meta file.");
		
		try(FileInputStream reader = new FileInputStream(metaFilePath)) {
			
			return getStringArray(reader);
			
		}
		
	}
	
	/**
	 * Writes this workshop item data to disk as a {@code .ctswm} file.
	 * 
	 * @param destination — write destination
	 * @throws IOException if an IO error occurs while writing this file to disk.
	 */
	public void writeWorkshopMetaFile(CSFolder destination) throws IOException {
		
		writeWorkshopMeta(destination, ID, title, description, langauge, visibility, tags, sourceScriptPath, previewImagePath);
				
	}
	
	/**
	 * Writes this {@code WorkshopItemData}'s tags to disk as a form of identifying what type of script it is. 
	 * 
	 * @param destination — write destination folder
	 * @throws IOException if an IO error occurs while writing this file to disk.
	 */
	public void writeItemMetaFile(CSFolder destination) throws IOException {
		
		writeItemMeta(destination, title, tags);
				
	}
	
	@Override public boolean equals(Object obj) {

		return obj instanceof WorkshopItemData asID && asID.ID.equals(ID);
		
	}

}
