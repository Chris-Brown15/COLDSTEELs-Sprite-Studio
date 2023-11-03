/**
 * 
 */
package cs.ext.steamworks;

import java.util.Collection;

import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriends.FriendFlags;
import com.codedisaster.steamworks.SteamFriends.FriendGameInfo;
import com.codedisaster.steamworks.SteamFriends.FriendRelationship;
import com.codedisaster.steamworks.SteamFriends.OverlayDialog;
import com.codedisaster.steamworks.SteamFriends.OverlayToStoreFlag;
import com.codedisaster.steamworks.SteamFriends.OverlayToUserDialog;
import com.codedisaster.steamworks.SteamFriends.OverlayToWebPageMode;
import com.codedisaster.steamworks.SteamFriends.PersonaState;

import cs.core.utils.ShutDown;

import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;

/**
 * 
 */
public class Friends implements ShutDown {

	private final SteamFriends friends;
	
	private boolean isFreed = false;
	
	/**
	 * Creates a new Friends instance, responsible for interfacing with Steam friends.
	 */
	Friends(SteamFriendsCallback functions) {
	
		friends = new SteamFriends(functions);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getPersonaName()
	 */
	public String getPersonaName() {
		
		return friends.getPersonaName();
		
	}

	/**
	 * @param personaName
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#setPersonaName(java.lang.String)
	 */
	public SteamAPICall setPersonaName(String personaName) {
		
		return friends.setPersonaName(personaName);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getPersonaState()
	 */
	public PersonaState getPersonaState() {
		
		return friends.getPersonaState();
		
	}

	/**
	 * @param friendFlag
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendCount(com.codedisaster.steamworks.SteamFriends.FriendFlags)
	 */
	public int getFriendCount(FriendFlags friendFlag) {
		
		return friends.getFriendCount(friendFlag);
		
	}

	/**
	 * @param friendFlags
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendCount(java.util.Collection)
	 */
	public int getFriendCount(Collection<FriendFlags> friendFlags) {
		return friends.getFriendCount(friendFlags);
	}

	/**
	 * @param friend
	 * @param friendFlag
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendByIndex(int, com.codedisaster.steamworks.SteamFriends.FriendFlags)
	 */
	public SteamID getFriendByIndex(int friend, FriendFlags friendFlag) {
		
		return friends.getFriendByIndex(friend, friendFlag);
		
	}

	/**
	 * @param friend
	 * @param friendFlags
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendByIndex(int, java.util.Collection)
	 */
	public SteamID getFriendByIndex(int friend, Collection<FriendFlags> friendFlags) {
		
		return friends.getFriendByIndex(friend, friendFlags);
		
	}

	/**
	 * @param steamIDFriend
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendRelationship(com.codedisaster.steamworks.SteamID)
	 */
	public FriendRelationship getFriendRelationship(SteamID steamIDFriend) {
		
		return friends.getFriendRelationship(steamIDFriend);
		
	}

	/**
	 * @param steamIDFriend
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendPersonaState(com.codedisaster.steamworks.SteamID)
	 */
	public PersonaState getFriendPersonaState(SteamID steamIDFriend) {
		return friends.getFriendPersonaState(steamIDFriend);
	}

	/**
	 * @param steamIDFriend
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendPersonaName(com.codedisaster.steamworks.SteamID)
	 */
	public String getFriendPersonaName(SteamID steamIDFriend) {
		
		return friends.getFriendPersonaName(steamIDFriend);
		
	}

	/**
	 * @param steamIDFriend
	 * @param friendGameInfo
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendGamePlayed(
	 * 	com.codedisaster.steamworks.SteamID, 
	 * 	com.codedisaster.steamworks.SteamFriends.FriendGameInfo
	 * )
	 */
	public boolean getFriendGamePlayed(SteamID steamIDFriend, FriendGameInfo friendGameInfo) {
		
		return friends.getFriendGamePlayed(steamIDFriend, friendGameInfo);
		
	}

	/**
	 * @param steamID
	 * @param speaking
	 * @see com.codedisaster.steamworks.SteamFriends#setInGameVoiceSpeaking(
	 * 	com.codedisaster.steamworks.SteamID, 
	 * 	boolean
	 * )
	 */
	public void setInGameVoiceSpeaking(SteamID steamID, boolean speaking) {
		
		friends.setInGameVoiceSpeaking(steamID, speaking);
		
	}

	/**
	 * @param steamID
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getSmallFriendAvatar(com.codedisaster.steamworks.SteamID)
	 */
	public int getSmallFriendAvatar(SteamID steamID) {
		
		return friends.getSmallFriendAvatar(steamID);
				
	}

	/**
	 * @param steamID
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getMediumFriendAvatar(com.codedisaster.steamworks.SteamID)
	 */
	public int getMediumFriendAvatar(SteamID steamID) {
		
		return friends.getMediumFriendAvatar(steamID);
		
	}

	/**
	 * @param steamID
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getLargeFriendAvatar(com.codedisaster.steamworks.SteamID)
	 */
	public int getLargeFriendAvatar(SteamID steamID) {
		
		return friends.getLargeFriendAvatar(steamID);
		
	}

	/**
	 * @param steamID
	 * @param requireNameOnly
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#requestUserInformation(com.codedisaster.steamworks.SteamID, boolean)
	 */
	public boolean requestUserInformation(SteamID steamID, boolean requireNameOnly) {
		
		return friends.requestUserInformation(steamID, requireNameOnly);
		
	}

	/**
	 * @param dialog
	 * @see com.codedisaster.steamworks.SteamFriends#activateGameOverlay(com.codedisaster.steamworks.SteamFriends.OverlayDialog)
	 */
	public void activateGameOverlay(OverlayDialog dialog) {
		
		friends.activateGameOverlay(dialog);
		
	}

	/**
	 * @param dialog
	 * @param steamID
	 * @see com.codedisaster.steamworks.SteamFriends#activateGameOverlayToUser(
	 * 	com.codedisaster.steamworks.SteamFriends.OverlayToUserDialog, 
	 * 	com.codedisaster.steamworks.SteamID
	 * )
	 */
	public void activateGameOverlayToUser(OverlayToUserDialog dialog, SteamID steamID) {
		
		friends.activateGameOverlayToUser(dialog, steamID);
		
	}

	/**
	 * @param url
	 * @param mode
	 * @see com.codedisaster.steamworks.SteamFriends#activateGameOverlayToWebPage(
	 * 	java.lang.String, 
	 * 	com.codedisaster.steamworks.SteamFriends.OverlayToWebPageMode
	 * )
	 */
	public void activateGameOverlayToWebPage(String url, OverlayToWebPageMode mode) {
		
		friends.activateGameOverlayToWebPage(url, mode);
		
	}

	/**
	 * @param appID
	 * @param flag
	 * @see com.codedisaster.steamworks.SteamFriends#activateGameOverlayToStore(int, com.codedisaster.steamworks.SteamFriends.OverlayToStoreFlag)
	 */
	public void activateGameOverlayToStore(int appID, OverlayToStoreFlag flag) {
		
		friends.activateGameOverlayToStore(appID, flag);
		
	}

	/**
	 * @param steamIDUserPlayedWith
	 * @see com.codedisaster.steamworks.SteamFriends#setPlayedWith(com.codedisaster.steamworks.SteamID)
	 */
	public void setPlayedWith(SteamID steamIDUserPlayedWith) {
		
		friends.setPlayedWith(steamIDUserPlayedWith);
		
	}

	/**
	 * @param steamIDLobby
	 * @see com.codedisaster.steamworks.SteamFriends#activateGameOverlayInviteDialog(com.codedisaster.steamworks.SteamID)
	 */
	public void activateGameOverlayInviteDialog(SteamID steamIDLobby) {
		
		friends.activateGameOverlayInviteDialog(steamIDLobby);
		
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#setRichPresence(java.lang.String, java.lang.String)
	 */
	public boolean setRichPresence(String key, String value) {
		
		return friends.setRichPresence(key, value);
		
	}

	/**
	 * 
	 * @see com.codedisaster.steamworks.SteamFriends#clearRichPresence()
	 */
	public void clearRichPresence() {
		
		friends.clearRichPresence();
		
	}

	/**
	 * @param steamIDFriend
	 * @param key
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendRichPresence(com.codedisaster.steamworks.SteamID, java.lang.String)
	 */
	public String getFriendRichPresence(SteamID steamIDFriend, String key) {
		
		return friends.getFriendRichPresence(steamIDFriend, key);
		
	}

	/**
	 * @param steamIDFriend
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendRichPresenceKeyCount(com.codedisaster.steamworks.SteamID)
	 */
	public int getFriendRichPresenceKeyCount(SteamID steamIDFriend) {
		
		return friends.getFriendRichPresenceKeyCount(steamIDFriend);
		
	}

	/**
	 * @param steamIDFriend
	 * @param index
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendRichPresenceKeyByIndex(com.codedisaster.steamworks.SteamID, int)
	 */
	public String getFriendRichPresenceKeyByIndex(SteamID steamIDFriend, int index) {
		
		return friends.getFriendRichPresenceKeyByIndex(steamIDFriend, index);
		
	}

	/**
	 * @param steamIDFriend
	 * @see com.codedisaster.steamworks.SteamFriends#requestFriendRichPresence(com.codedisaster.steamworks.SteamID)
	 */
	public void requestFriendRichPresence(SteamID steamIDFriend) {
		
		friends.requestFriendRichPresence(steamIDFriend);
		
	}

	/**
	 * @param steamIDFriend
	 * @param connectString
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#inviteUserToGame(com.codedisaster.steamworks.SteamID, java.lang.String)
	 */
	public boolean inviteUserToGame(SteamID steamIDFriend, String connectString) {
		
		return friends.inviteUserToGame(steamIDFriend, connectString);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getCoplayFriendCount()
	 */
	public int getCoplayFriendCount() {
		
		return friends.getCoplayFriendCount();
		
	}

	/**
	 * @param index
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getCoplayFriend(int)
	 */
	public SteamID getCoplayFriend(int index) {
		
		return friends.getCoplayFriend(index);
		
	}

	/**
	 * @param steamIDFriend
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendCoplayTime(com.codedisaster.steamworks.SteamID)
	 */
	public int getFriendCoplayTime(SteamID steamIDFriend) {
		
		return friends.getFriendCoplayTime(steamIDFriend);
		
	}

	/**
	 * @param steamIDFriend
	 * @return
	 * @see com.codedisaster.steamworks.SteamFriends#getFriendCoplayGame(com.codedisaster.steamworks.SteamID)
	 */
	public int getFriendCoplayGame(SteamID steamIDFriend) {
		
		return friends.getFriendCoplayGame(steamIDFriend);
		
	}

	private void dispose() {
		
		friends.dispose();
		
	}

	@Override public void shutDown() {

		if(isFreed) return;
		isFreed = true;
		dispose();
		
	}

	@Override public boolean isFreed() {

		return isFreed;
	}

	
	
}
