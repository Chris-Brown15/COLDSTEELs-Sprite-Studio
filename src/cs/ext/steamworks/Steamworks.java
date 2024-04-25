/**
 * 
 */
package cs.ext.steamworks;

import static cs.csss.engine.Logging.sysDebugln;

import java.io.IOException;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamUGCCallback;
import com.codedisaster.steamworks.SteamUserCallback;

import cs.core.utils.ShutDown;
import cs.csss.engine.Engine;
import cs.ext.misc.utils.AlreadyInitializedException;
import cs.ext.misc.utils.NotInitializedException;
//appID: 2616440
/**
 * Wrapper over functions from the {@link com.codedisaster.steamworks.SteamAPI SteamAPI} class, and provides access to Steamworks API sublibraries.
 * 
 * <p>
 * Each sublibrary is independently initialized and accessed via its own methods. All sublibraries that are initialized via this class are also 
 * freed via this class's {@link Steamworks#shutDown() shutDown} so there is no need to free them onself.
 * </P>
 */
public class Steamworks implements ShutDown {
	
	private boolean isSteamworksInitialized; 
	private boolean isShutDown = false;	
	
	private UGC userGeneratedContentAPI;
	private Friends friendsAPI;
	private User userAPI;
	
	/**
	 * Creates a new instance of a SteamAPI manager. 
	 * 
	 * @param appID — Steam ID for an application
	 */
	public Steamworks(int appID) {

		try {
			
			if(Engine.isDebug()) try {
			
				SteamApplicationData.create__steam_appid_txt__file(appID);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
			} 
			
			SteamApplicationData.registerApplicationData(appID);
			
			SteamAPI.loadLibraries();
			sysDebugln("Was Steam Native API Loaded: " + SteamAPIHelper.wasNativeAPILoaded());
			isSteamworksInitialized = SteamAPI.init();

		} catch (SteamException e) {
			
			e.printStackTrace();
			sysDebugln("Error Initializing Steam");
						
		}
		
		sysDebugln("Was Steam Initialized: " + isSteamworksInitialized);
		
	}
	
	/**
	 * Returns whether this program should restart. If restarting should take place, it begins as well.
	 * 
	 * @return {@code true} if the program should restart.
	 * @throws SteamException if an exception occurs during the Steam API calls.
	 */
	public boolean restartIfNeeded() throws SteamException {

		return SteamAPI.restartAppIfNecessary(SteamApplicationData.steamAppID());
		
	}
	
	/**
	 * Returns whether SteamAPI was able to initialize.
	 * 
	 * @return Whether SteamAPI was able to initialize.
	 */
	public boolean initialized() {
		
		return isSteamworksInitialized;
		
	}
	
	/**
	 * Initalizes the Steam Friends API.
	 * 
	 * @param callbacks — callbacks for the Friends API.
	 */
	public void initializeFriendsAPI(SteamFriendsCallback callbacks) {
		
		if(friendsAPI != null) throw new AlreadyInitializedException("Friends API is already initialized.");
		friendsAPI = new Friends(callbacks);
		
	}

	/**
	 * Gives access to the Steam Friends API.
	 * 
	 * @return Friends API provider.
	 */
	public Friends friendsAPI() {
		
		if(friendsAPI == null) throw new NotInitializedException("Friends API is not initialized.");
		return friendsAPI;
		
	}

	/**
	 * Initalizes the Steam Friends API.
	 * 
	 * @param callbacks — callbacks for the Friends API.
	 */
	public void initializeUGCAPI(SteamUGCCallback callbacks) {
		
		if(userGeneratedContentAPI != null) throw new AlreadyInitializedException("UGC API is already initialized.");
		userGeneratedContentAPI = new UGC(callbacks);
				
	}

	/**
	 * Gives access to the Steam UGC API.
	 * 
	 * @return User generated content API provider.
	 */
	public UGC UGCAPI() {
		
		if(userGeneratedContentAPI == null) throw new NotInitializedException("UGC API is not initialized.");
		return userGeneratedContentAPI;
		
	}
	
	/**
	 * Initializes the Steam User API.
	 * 
	 * @param callbacks — callbacks for the User API
	 */
	public void initializeUserAPI(SteamUserCallback callbacks) {
		
		if(userAPI != null) throw new AlreadyInitializedException("User API is already initialized");
		userAPI = new User(callbacks);
		
	}
	
	/**
	 * Gives access to the Steam User API.
	 * 
	 * @return User API provider.
	 */
	public User userAPI() {
		
		return userAPI;
		
	}

	private void shutDownIfNonNull(ShutDown object) {
		
		if(object != null) object.shutDown();
		
	}
	
	@Override public void shutDown() {

		if(isFreed()) return;
		SteamAPI.shutdown();
		shutDownIfNonNull(friendsAPI);
		shutDownIfNonNull(userGeneratedContentAPI);
		shutDownIfNonNull(userAPI);
				
		isShutDown = true;
		
	}
	
	@Override public boolean isFreed() {

		return isShutDown;
		
	}
	
}
