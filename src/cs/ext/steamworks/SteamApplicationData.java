/**
 * 
 */
package cs.ext.steamworks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.codedisaster.steamworks.SteamException;

import cs.ext.misc.utils.AlreadyInitializedException;
import cs.ext.misc.utils.NotInitializedException;

/**
 * Container class for application-level data.
 */
public final class SteamApplicationData {

	private static boolean isInitialized = false;
	
	private static int applicationID;
	
	/**
	 * Registers needed steam application data with the program.
	 * 
	 * @param applicationID — Steam app ID of this program
	 */
	public static void registerApplicationData(int applicationID) {
		
		if(isInitialized) throw new AlreadyInitializedException("Steamworks Application Data cannot be registered more than once."); 
		isInitialized = true;
		SteamApplicationData.applicationID = applicationID;
		
	}

	/**
	 * Creates a text file containing only the Steam app ID, which can stand in for cases when the actual ID cannot be retrieved via Steam client.
	 * 
	 * @param appID — Steam app ID of the application
	 * @throws SteamException if an exception in Steamworks4J occurs.
	 */
	public static void create__steam_appid_txt__file(int appID) throws IOException {
		
		Path path = Paths.get("steam_appid.txt");
		
		if(Files.exists(path)) return;
		
		Files.createFile(path);
		Files.write(path, String.valueOf(appID).getBytes());
		
	}
	
	/**
	 * Returns whether {@link SteamApplicationData#registerApplicationData(int)} has been invoked.
	 * 
	 * @return Whether application data has been registered.
	 */
	public static boolean isSteamAppDataRegistered() {
		
		return isInitialized;
				
	}
	
	/**
	 * Returns the Steam application ID of this program
	 * 
	 * @return Steam App ID for this program.
	 */
	public static int steamAppID() {
	
		verifyInitialized();
		return applicationID;
		
	}

	private static void verifyInitialized() {

		if(!isInitialized) throw new NotInitializedException("Steamworks Application Data has not been registered yet.");
		
	}
	
}
