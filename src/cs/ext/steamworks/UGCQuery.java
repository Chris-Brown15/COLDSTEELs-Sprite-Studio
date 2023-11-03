/**
 * 
 */
package cs.ext.steamworks;

import java.util.Objects;

import com.codedisaster.steamworks.SteamUGCQuery;

import cs.core.utils.ShutDown;

/**
 * Container for UGC queries.
 */
public class UGCQuery implements ShutDown {

	SteamUGCQuery query;
	private final UGC ugc;
	
	/**
	 * Creates a UGC query
	 * 
	 * @param ugc — source UGC API
	 * @param query — Steamworks API query object produced from a query
	 */
	UGCQuery(UGC ugc , SteamUGCQuery query) {
		
		this.ugc = ugc;
		this.query  = query;
		
	}
	
	/**
	 * Adds a required tag to a pending UGC Query. This will only return UGC with the specified tag.
	 * <p>
	 * 	<b>Note:</b> This must be set before you send a UGC Query handle using 
	 * 	{@link cs.ext.steamworks.UGC#sendQueryUGCRequest(SteamUGCQuery) sendQueryUGCRequest(SteamUGCQuery)}. 	
	 * </p>
	 * 
	 * @param tag — a tag that must be attached to UGC to receive it
	 * @return {@code true} upon success, {@code false} if a failure occurs
	 */
	public boolean addRequiredTag(String tag) {
		
		Objects.requireNonNull(tag);
		
		return ugc.addRequiredTag(query, tag);
		
	}

	/**
	 * 
	 * @param tag
	 */
	public boolean addExcludedTag(String tag) {
		
		return ugc.addExcludedTag(query, tag);
		
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public boolean addRequiredKeyValueTag(String key , String value) {
		
		return ugc.addRequiredKeyValueTag(query, key, value);
		
	}
	
	/**
	 * 
	 * @param returnOnlyIDs
	 */
	public boolean setReturnOnlyIDs(boolean returnOnlyIDs) {
		
		return ugc.setReturnOnlyIDs(query, returnOnlyIDs);
		
	}
	
	/**
	 * 
	 * @param returnLongDescription
	 */
	public boolean setReturnLongDescription(boolean returnLongDescription) {
		
		return ugc.setReturnLongDescription(query, returnLongDescription);
		
	}
	
	/**
	 * 
	 * @param returnMetadata
	 */
	public boolean setReturnMetadata(boolean returnMetadata) {	
		
		return ugc.setReturnMetadata(query, returnMetadata);
		
	}
		
	/**
	 * 
	 * @param returnChildren
	 */
	public boolean setReturnChildren(boolean returnChildren) {	
		
		return ugc.setReturnChildren(query, returnChildren);
		
	}
	
	/**
	 * 
	 * @param returnAdditionalPreviews
	 */
	public boolean setReturnAdditionalPreviews(boolean returnAdditionalPreviews) {	
		
		return ugc.setReturnChildren(query, returnAdditionalPreviews);
		
	}

	/**
	 * 
	 * @param returnTotalsOnly
	 */
	public boolean setReturnTotalOnly(boolean returnTotalsOnly) {	
		
		return ugc.setReturnTotalOnly(query, returnTotalsOnly);
		
	}
	
	/**
	 * 
	 * @param returnKeyValueTags
	 */
	public boolean setReturnKeyValueTags(boolean returnKeyValueTags) {
		
		return ugc.setReturnKeyValueTags(query, returnKeyValueTags);
		
	}
	
	/**
	 * 
	 * @param language
	 * @return
	 */
	public boolean setLanguage(SteamLanguages language) {
		
		return ugc.setLanguage(query, language.APILanguageCode);
		
	}
	
	/**
	 * 
	 * 
	 * @param timeSeconds
	 * @return
	 * @see {@link UGC#setAllowCachedResponse(SteamUGCQuery, int) setAllowCachedResponse(SteamUGCQuery, int)}
	 */
	public boolean setAllowedCachedResponse(int timeSeconds) {
		
		return ugc.setAllowCachedResponse(query, timeSeconds);
		
	}
	
	/**
	 * 
	 * @param fileNameToMatch
	 * @return
	 */
	public boolean setCloudFileNameFilter(String fileNameToMatch) {
		
		return ugc.setCloudFileNameFilter(query, fileNameToMatch);
		
	}
	
	/**
	 * 
	 */
	public void send() {
		
		ugc.sendQueryUGCRequest(query);
		
	}
	
	@Override public void shutDown() {

		if(isFreed()) return;
		
		ugc.releaseQueryUserUGCRequest(query);
		query = null;

	}

 	@Override public boolean isFreed() {

		return query == null;
		
	}

	@Override public boolean equals(Object obj) {

		return obj instanceof UGCQuery query && query.query == this.query;
		
	}

	@Override public int hashCode() {

		return query.hashCode();
		
	}

	@Override public String toString() {

		return "CSXSteamworks UGC Query";
		
	}

}
