/**
 * 
 */
package cs.ext.steamworks;

import java.nio.ByteBuffer;

import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamAuth.BeginAuthSessionResult;
import com.codedisaster.steamworks.SteamAuth.UserHasLicenseForAppResult;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUser.VoiceResult;
import com.codedisaster.steamworks.SteamUserCallback;

import cs.core.utils.ShutDown;

/**
 * The Steam API User API.
 */
public class User implements ShutDown {

	private final SteamUser user;
	private boolean isFreed = false;
	
	/**
	 * Creates a user API.
	 */
	User(SteamUserCallback callbacks) {

		this.user = new SteamUser(callbacks);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamUser#getSteamID()
	 */
	public SteamID getSteamID() {
		
		return user.getSteamID();
		
	}

	/**
	 * 
	 * @see com.codedisaster.steamworks.SteamUser#startVoiceRecording()
	 */
	public void startVoiceRecording() {
		
		user.startVoiceRecording();
		
	}

	/**
	 * 
	 * @see com.codedisaster.steamworks.SteamUser#stopVoiceRecording()
	 */
	public void stopVoiceRecording() {
		
		user.stopVoiceRecording();
		
	}

	/**
	 * @param bytesAvailable
	 * @return
	 * @see com.codedisaster.steamworks.SteamUser#getAvailableVoice(int[])
	 */
	public VoiceResult getAvailableVoice(int[] bytesAvailable) {
		
		return user.getAvailableVoice(bytesAvailable);
		
	}

	/**
	 * @param voiceData
	 * @param bytesWritten
	 * @return
	 * @throws SteamException
	 * @see com.codedisaster.steamworks.SteamUser#getVoice(java.nio.ByteBuffer, int[])
	 */
	public VoiceResult getVoice(ByteBuffer voiceData, int[] bytesWritten) throws SteamException {
		
		return user.getVoice(voiceData, bytesWritten);
		
	}

	/**
	 * @param voiceData
	 * @param audioData
	 * @param bytesWritten
	 * @param desiredSampleRate
	 * @return
	 * @throws SteamException
	 * @see com.codedisaster.steamworks.SteamUser#decompressVoice(java.nio.ByteBuffer, java.nio.ByteBuffer, int[], int)
	 */
	public VoiceResult decompressVoice(
		ByteBuffer voiceData, 
		ByteBuffer audioData, 
		int[] bytesWritten, 
		int desiredSampleRate
	) throws SteamException {
	
		return user.decompressVoice(voiceData, audioData, bytesWritten, desiredSampleRate);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamUser#getVoiceOptimalSampleRate()
	 */
	public int getVoiceOptimalSampleRate() {
		
		return user.getVoiceOptimalSampleRate();
		
	}

	/**
	 * @param authTicket
	 * @param sizeInBytes
	 * @return
	 * @throws SteamException
	 * @see com.codedisaster.steamworks.SteamUser#getAuthSessionTicket(java.nio.ByteBuffer, int[])
	 */
	public SteamAuthTicket getAuthSessionTicket(ByteBuffer authTicket, int[] sizeInBytes) throws SteamException {
		
		return user.getAuthSessionTicket(authTicket, sizeInBytes);
	
	}

	/**
	 * @param authTicket
	 * @param steamID
	 * @return
	 * @throws SteamException
	 * @see com.codedisaster.steamworks.SteamUser#beginAuthSession(java.nio.ByteBuffer, com.codedisaster.steamworks.SteamID)
	 */
	public BeginAuthSessionResult beginAuthSession(ByteBuffer authTicket, SteamID steamID) throws SteamException {
		
		return user.beginAuthSession(authTicket, steamID);
		
	}

	/**
	 * @param steamID
	 * @see com.codedisaster.steamworks.SteamUser#endAuthSession(com.codedisaster.steamworks.SteamID)
	 */
	public void endAuthSession(SteamID steamID) {
		
		user.endAuthSession(steamID);
		
	}

	/**
	 * @param authTicket
	 * @see com.codedisaster.steamworks.SteamUser#cancelAuthTicket(com.codedisaster.steamworks.SteamAuthTicket)
	 */
	public void cancelAuthTicket(SteamAuthTicket authTicket) {
		
		user.cancelAuthTicket(authTicket);
		
	}

	/**
	 * @param steamID
	 * @param appID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUser#userHasLicenseForApp(com.codedisaster.steamworks.SteamID, int)
	 */
	public UserHasLicenseForAppResult userHasLicenseForApp(SteamID steamID, int appID) {
		
		return user.userHasLicenseForApp(steamID, appID);
		
	}

	/**
	 * @param dataToInclude
	 * @return
	 * @throws SteamException
	 * @see com.codedisaster.steamworks.SteamUser#requestEncryptedAppTicket(java.nio.ByteBuffer)
	 */
	public SteamAPICall requestEncryptedAppTicket(ByteBuffer dataToInclude) throws SteamException {
		
		return user.requestEncryptedAppTicket(dataToInclude);
		
	}

	/**
	 * @param ticket
	 * @param sizeInBytes
	 * @return
	 * @throws SteamException
	 * @see com.codedisaster.steamworks.SteamUser#getEncryptedAppTicket(java.nio.ByteBuffer, int[])
	 */
	public boolean getEncryptedAppTicket(ByteBuffer ticket, int[] sizeInBytes) throws SteamException {
		
		return user.getEncryptedAppTicket(ticket, sizeInBytes);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamUser#isBehindNAT()
	 */
	public boolean isBehindNAT() {
		
		return user.isBehindNAT();
		
	}

	/**
	 * @param steamIDGameServer
	 * @param serverIP
	 * @param serverPort
	 * @see com.codedisaster.steamworks.SteamUser#advertiseGame(com.codedisaster.steamworks.SteamID, int, short)
	 */
	public void advertiseGame(SteamID steamIDGameServer, int serverIP, short serverPort) {
		
		user.advertiseGame(steamIDGameServer, serverIP, serverPort);
		
	}

	private void dispose() {
		
		user.dispose();
		
	}

	@Override public void shutDown() {

		if(isFreed()) return;
		dispose();
		
	}

	@Override public boolean isFreed() {
		
		return isFreed;
		
	}

}
