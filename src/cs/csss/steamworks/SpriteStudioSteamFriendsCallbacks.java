/**
 * 
 */
package cs.csss.steamworks;

import com.codedisaster.steamworks.SteamFriends.PersonaChange;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;

/**
 * Implements various functions for the Steamworks Friends API.
 */
public class SpriteStudioSteamFriendsCallbacks implements SteamFriendsCallback {

	/**
	 * Creates a new SteamAPI friends callback set for Sprite Studio.
	 */
	public SpriteStudioSteamFriendsCallbacks() {}

	@Override public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result) {}

	@Override public void onPersonaStateChange(SteamID steamID, PersonaChange change) {}

	@Override public void onGameOverlayActivated(boolean active) {}

	@Override public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend) {}

	@Override public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height) {}

	@Override public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID) {}

	@Override public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect) {}

	@Override public void onGameServerChangeRequested(String server, String password) {}

}
