/**
 * 
 */
package cs.csss.steamworks;

import com.codedisaster.steamworks.SteamAuth.AuthSessionResponse;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUserCallback;

/**
 * Contains implementations of callback functions for the Steam API User API.
 */
public class SpriteStudioSteamUserCallbacks implements SteamUserCallback {

	/**
	 * Creates a User Callbacks implementation for Sprite Studio. 
	 */
	public SpriteStudioSteamUserCallbacks() {}

	@Override public void onAuthSessionTicket(SteamAuthTicket authTicket, SteamResult result) {}

	@Override public void onValidateAuthTicket(SteamID steamID, AuthSessionResponse authSessionResponse, SteamID ownerSteamID) {}

	@Override public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {}

	@Override public void onEncryptedAppTicket(SteamResult result) {}

}
