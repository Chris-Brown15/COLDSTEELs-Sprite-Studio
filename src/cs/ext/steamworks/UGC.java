/**
 * 
 */
package cs.ext.steamworks;

import java.util.Collection;

import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamRemoteStorage.PublishedFileVisibility;
import com.codedisaster.steamworks.SteamRemoteStorage.WorkshopFileType;
import com.codedisaster.steamworks.SteamUGC;
import com.codedisaster.steamworks.SteamUGC.ItemAdditionalPreview;
import com.codedisaster.steamworks.SteamUGC.ItemDownloadInfo;
import com.codedisaster.steamworks.SteamUGC.ItemInstallInfo;
import com.codedisaster.steamworks.SteamUGC.ItemState;
import com.codedisaster.steamworks.SteamUGC.ItemStatistic;
import com.codedisaster.steamworks.SteamUGC.ItemUpdateInfo;
import com.codedisaster.steamworks.SteamUGC.ItemUpdateStatus;
import com.codedisaster.steamworks.SteamUGC.MatchingUGCType;
import com.codedisaster.steamworks.SteamUGC.UGCQueryType;
import com.codedisaster.steamworks.SteamUGC.UserUGCList;
import com.codedisaster.steamworks.SteamUGC.UserUGCListSortOrder;

import cs.core.utils.ShutDown;

import com.codedisaster.steamworks.SteamUGCCallback;
import com.codedisaster.steamworks.SteamUGCDetails;
import com.codedisaster.steamworks.SteamUGCQuery;
import com.codedisaster.steamworks.SteamUGCUpdateHandle;

/**
 * Class for managing user-generated content.
 */
public class UGC implements ShutDown {
	
	/**
	 * <q>
	 * 	The maximum size in bytes that a Workshop item title can be.
	 * </q>
	 */
	public static final int PUBLISHED_DOCUMENT_TITLE_MAX = 129;
	
	/**
	 * <Q>
	 * 	The maximum size in bytes that a Workshop item description can be.
	 * </q>
	 */
	public static final int PUBLISHED_DOCUMENT_DESCRIPTION_MAX = 8000;
	
	/**
	 * <q>
	 * 	The maximum amount of bytes you can set with SetItemMetadata.
	 * </q>
	 */
	public static final int DEVELOPER_METADATA_MAX = 5000;
	
	/**
	 * Maximum length of tags for Workshop items.
	 */
	public static final int ITEM_TAG_MAX_LENGTH = 255;
	
	/**
	 * <q>	
	 * 	Used to specify an invalid query handle. This is frequently returned if a call fails.
	 * </q>
	 */
	public static final long QUERY_HANDLE_INVALID = -1;
	
	/**
	 * Internal SteamUGC object from Steamworks that handles all Steam API calls
	 */
	private final SteamUGC steamUGC;

	private boolean isFreed = false;
	
	/**
	 * Creates the UGC.
	 * 
	 * @param callback — the callback implementation for this UGC
	 */
	UGC(SteamUGCCallback callback) {

		steamUGC = new SteamUGC(callback);
				
	}

	/**
	 * Frees the underlying resources for this UGC.
	 */
	private void dispose() {
		
		steamUGC.dispose();
		
	}

	/**
	 * @param accountID
	 * @param listType
	 * @param matchingType
	 * @param sortOrder
	 * @param creatorAppID
	 * @param consumerAppID
	 * @param page
	 * @return
	 * @throws BadQueryException if the query results in an error.
	 * @see com.codedisaster.steamworks.SteamUGC#createQueryUserUGCRequest(
	 * 	int, 
	 * 	com.codedisaster.steamworks.SteamUGC.UserUGCList, 
	 * 	com.codedisaster.steamworks.SteamUGC.MatchingUGCType, 
	 * 	com.codedisaster.steamworks.SteamUGC.UserUGCListSortOrder, 
	 * 	int, 
	 * 	int, 
	 * 	int
	 * )
	 */
	public UGCQuery createQueryUserUGCRequest(
		int accountID, 
		UserUGCList listType, 
		MatchingUGCType matchingType, 
		UserUGCListSortOrder sortOrder, 
		int creatorAppID, 
		int consumerAppID, 
		int page
	) throws BadQueryException {
		
		SteamUGCQuery query = steamUGC.createQueryUserUGCRequest(accountID, listType, matchingType, sortOrder, creatorAppID, consumerAppID, page );
		if(!query.isValid()) throw new BadQueryException("A query failure occurred");
		return new UGCQuery(this , query);
		
	}

	/**
	 * @param queryType
	 * @param matchingType
	 * @param creatorAppID
	 * @param consumerAppID
	 * @param page
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#createQueryAllUGCRequest(
	 * 	com.codedisaster.steamworks.SteamUGC.UGCQueryType, 
	 * 	com.codedisaster.steamworks.SteamUGC.MatchingUGCType, 
	 * 	int, 
	 * 	int, 
	 * 	int
	 * )
	 */
	public SteamUGCQuery createQueryAllUGCRequest(
		UGCQueryType queryType, 
		MatchingUGCType matchingType, 
		int creatorAppID, 
		int consumerAppID, 
		int page
	) {
		
		return steamUGC.createQueryAllUGCRequest(queryType, matchingType, creatorAppID, consumerAppID, page);
		
	}

	/**
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#createQueryUGCDetailsRequest(com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamUGCQuery createQueryUGCDetailsRequest(SteamPublishedFileID publishedFileID) {
		
		return steamUGC.createQueryUGCDetailsRequest(publishedFileID);
		
	}

	/**
	 * @param publishedFileIDs
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#createQueryUGCDetailsRequest(java.util.Collection)
	 */
	public SteamUGCQuery createQueryUGCDetailsRequest(Collection<SteamPublishedFileID> publishedFileIDs) {
		
		return steamUGC.createQueryUGCDetailsRequest(publishedFileIDs);
		
	}

	/**
	 * @param query
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#sendQueryUGCRequest(com.codedisaster.steamworks.SteamUGCQuery)
	 */
	public SteamAPICall sendQueryUGCRequest(SteamUGCQuery query) {
		
		return steamUGC.sendQueryUGCRequest(query);
		
	}

	/**
	 * 
	 * @param query
	 * @return
	 * @see {@link UGC#sendQueryUGCRequest(SteamUGCQuery) sendQueryUGCRequest(SteamUGCQuery)}
	 */
	public SteamAPICall sentQueryUGCRequest(UGCQuery query) {
		
		return steamUGC.sendQueryUGCRequest(query.query);
		
	}
	
	/**
	 * Retrieve the details of an individual workshop item after receiving a querying UGC call result.
	 * <p>
	 * 	You should call this in a loop to get the details of all the workshop items returned.
	 * </p>
	 * 
	 * <p>
	 * 	<b>NOTE:</b>This must only be called with the handle obtained from a successful {@link 
	 * 	com.codedisaster.steamworks.SteamUGCCallback#onUGCQueryCompleted(SteamUGCQuery, int , int, boolean, com.codedisaster.steamworks.SteamResult
	 * 	) onUGCQueryCompleted} call result.
	 * </p>
	 * 
	 * @param query — the UGC query handle to get the results from
	 * @param index — the index of the item to get the details of
	 * @param details — returns the the UGC details
	 * @return {@code true} upon success, indicates that {@code details} has been filled out, otherwise, {@code false} if the UGC query handle is
	 * 		   is invalid of {@code index} is out of bounds. 
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCResult(
	 * 	com.codedisaster.steamworks.SteamUGCQuery, 
	 * 	int, 
	 * 	com.codedisaster.steamworks.SteamUGCDetails
	 * )
	 */
	public boolean getQueryUGCResult(SteamUGCQuery query, int index, SteamUGCDetails details) {
		
		return steamUGC.getQueryUGCResult(query, index, details);
		
	}

	/**
	 * @param query
	 * @param index
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCPreviewURL(com.codedisaster.steamworks.SteamUGCQuery, int)
	 */
	public String getQueryUGCPreviewURL(SteamUGCQuery query, int index) {
		
		return steamUGC.getQueryUGCPreviewURL(query, index);
		
	}

	/**
	 * @param query
	 * @param index
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCMetadata(com.codedisaster.steamworks.SteamUGCQuery, int)
	 */
	public String getQueryUGCMetadata(SteamUGCQuery query, int index) {
		
		return steamUGC.getQueryUGCMetadata(query, index);
		
	}

	/**
	 * @param query
	 * @param index
	 * @param statType
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCStatistic(
	 * 	com.codedisaster.steamworks.SteamUGCQuery, 
	 * 	int, 
	 * 	com.codedisaster.steamworks.SteamUGC.ItemStatistic
	 * )
	 */
	public long getQueryUGCStatistic(SteamUGCQuery query, int index, ItemStatistic statType) {
		
		return steamUGC.getQueryUGCStatistic(query, index, statType);
		
	}

	/**
	 * @param query
	 * @param index
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCNumAdditionalPreviews(com.codedisaster.steamworks.SteamUGCQuery, int)
	 */
	public int getQueryUGCNumAdditionalPreviews(SteamUGCQuery query, int index) {
		
		return steamUGC.getQueryUGCNumAdditionalPreviews(query, index);
	
	}

	/**
	 * @param query
	 * @param index
	 * @param previewIndex
	 * @param previewInfo
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCAdditionalPreview(
	 * 	com.codedisaster.steamworks.SteamUGCQuery, 
	 * 	int, 
	 * 	int, 
	 * 	com.codedisaster.steamworks.SteamUGC.ItemAdditionalPreview
	 * )
	 */
	public boolean getQueryUGCAdditionalPreview(SteamUGCQuery query, int index, int previewIndex, ItemAdditionalPreview previewInfo) {
		
		return steamUGC.getQueryUGCAdditionalPreview(query, index, previewIndex, previewInfo);
		
	}

	/**
	 * @param query
	 * @param index
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCNumKeyValueTags(com.codedisaster.steamworks.SteamUGCQuery, int)
	 */
	public int getQueryUGCNumKeyValueTags(SteamUGCQuery query, int index) {
		
		return steamUGC.getQueryUGCNumKeyValueTags(query, index);
		
	}

	/**
	 * @param query
	 * @param index
	 * @param keyValueTagIndex
	 * @param keyAndValue
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getQueryUGCKeyValueTag(com.codedisaster.steamworks.SteamUGCQuery, int, int, java.lang.String[])
	 */
	public boolean getQueryUGCKeyValueTag(SteamUGCQuery query, int index, int keyValueTagIndex, String[] keyAndValue) {
		
		return steamUGC.getQueryUGCKeyValueTag(query, index, keyValueTagIndex, keyAndValue);
		
	}

	/**
	 * @param query
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#releaseQueryUserUGCRequest(com.codedisaster.steamworks.SteamUGCQuery)
	 */
	public boolean releaseQueryUserUGCRequest(SteamUGCQuery query) {
		
		return steamUGC.releaseQueryUserUGCRequest(query);
		
	}

	/**
	 * @param query
	 * @param tagName
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#addRequiredTag(com.codedisaster.steamworks.SteamUGCQuery, java.lang.String)
	 */
	public boolean addRequiredTag(SteamUGCQuery query, String tagName) {
		
		return steamUGC.addRequiredTag(query, tagName);
		
	}

	/**
	 * @param query
	 * @param tagName
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#addExcludedTag(com.codedisaster.steamworks.SteamUGCQuery, java.lang.String)
	 */
	public boolean addExcludedTag(SteamUGCQuery query, String tagName) {
		
		return steamUGC.addExcludedTag(query, tagName);
		
	}

	/**
	 * @param query
	 * @param returnOnlyIDs
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnOnlyIDs(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setReturnOnlyIDs(SteamUGCQuery query, boolean returnOnlyIDs) {
		
		return steamUGC.setReturnOnlyIDs(query, returnOnlyIDs);
		
	}

	/**
	 * @param query
	 * @param returnKeyValueTags
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnKeyValueTags(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setReturnKeyValueTags(SteamUGCQuery query, boolean returnKeyValueTags) {
		
		return steamUGC.setReturnKeyValueTags(query, returnKeyValueTags);
		
	}

	/**
	 * @param query
	 * @param returnLongDescription
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnLongDescription(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setReturnLongDescription(SteamUGCQuery query, boolean returnLongDescription) {
		
		return steamUGC.setReturnLongDescription(query, returnLongDescription);
		
	}

	/**
	 * @param query
	 * @param returnMetadata
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnMetadata(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setReturnMetadata(SteamUGCQuery query, boolean returnMetadata) {
		
		return steamUGC.setReturnMetadata(query, returnMetadata);
		
	}

	/**
	 * @param query
	 * @param returnChildren
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnChildren(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setReturnChildren(SteamUGCQuery query, boolean returnChildren) {
		
		return steamUGC.setReturnChildren(query, returnChildren);
		
	}

	/**
	 * @param query
	 * @param returnAdditionalPreviews
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnAdditionalPreviews(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setReturnAdditionalPreviews(SteamUGCQuery query, boolean returnAdditionalPreviews) {
		
		return steamUGC.setReturnAdditionalPreviews(query, returnAdditionalPreviews);
		
	}

	/**
	 * @param query
	 * @param returnTotalOnly
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnTotalOnly(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setReturnTotalOnly(SteamUGCQuery query, boolean returnTotalOnly) {
		
		return steamUGC.setReturnTotalOnly(query, returnTotalOnly);
		
	}

	/**
	 * @param query
	 * @param days
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setReturnPlaytimeStats(com.codedisaster.steamworks.SteamUGCQuery, int)
	 */
	public boolean setReturnPlaytimeStats(SteamUGCQuery query, int days) {
		
		return steamUGC.setReturnPlaytimeStats(query, days);
		
	}

	/**
	 * @param query
	 * @param language
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setLanguage(com.codedisaster.steamworks.SteamUGCQuery, java.lang.String)
	 */
	public boolean setLanguage(SteamUGCQuery query, String language) {
		
		return steamUGC.setLanguage(query, language);
		
	}

	/**
	 * Sets whether results will be returned from the cache for the specific period of time on a pending UGC Query.
	 * <p>
	 * 	<b>NOTE:</b> This must be set before you send a UGC Query handle using 
	 * 	{@link UGC#sendQueryUGCRequest(SteamUGCQuery) sendQueryUGCRequest(SteamUGCQuery)}.
	 * </p>
	 * 
	 * @param query — the UGC query handle to customize 
	 * @param maxAgeSeconds — the maximum amount of time that an item can be returned without a cache invalidation
	 * @return {@code true} upon success, or {@code false} if {@code query.isValid()} is {@code false}.
	 * @see com.codedisaster.steamworks.SteamUGC#setAllowCachedResponse(com.codedisaster.steamworks.SteamUGCQuery, int)
	 */
	public boolean setAllowCachedResponse(SteamUGCQuery query, int maxAgeSeconds) {
		
		return steamUGC.setAllowCachedResponse(query, maxAgeSeconds);
		
	}

	/**
	 * @param query
	 * @param matchCloudFileName
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setCloudFileNameFilter(com.codedisaster.steamworks.SteamUGCQuery, java.lang.String)
	 */
	public boolean setCloudFileNameFilter(SteamUGCQuery query, String matchCloudFileName) {
		
		return steamUGC.setCloudFileNameFilter(query, matchCloudFileName);
		
	}

	/**
	 * @param query
	 * @param matchAnyTag
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setMatchAnyTag(com.codedisaster.steamworks.SteamUGCQuery, boolean)
	 */
	public boolean setMatchAnyTag(SteamUGCQuery query, boolean matchAnyTag) {
		
		return steamUGC.setMatchAnyTag(query, matchAnyTag);
		
	}

	/**
	 * @param query
	 * @param searchText
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setSearchText(com.codedisaster.steamworks.SteamUGCQuery, java.lang.String)
	 */
	public boolean setSearchText(SteamUGCQuery query, String searchText) {
		
		return steamUGC.setSearchText(query, searchText);
		
	}

	/**
	 * @param query
	 * @param days
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setRankedByTrendDays(com.codedisaster.steamworks.SteamUGCQuery, int)
	 */
	public boolean setRankedByTrendDays(SteamUGCQuery query, int days) {
		
		return steamUGC.setRankedByTrendDays(query, days);
		
	}

	/**
	 * @param query
	 * @param key
	 * @param value
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#addRequiredKeyValueTag(
	 * 	com.codedisaster.steamworks.SteamUGCQuery, 
	 * 	java.lang.String, 
	 * 	java.lang.String
	 * )
	 */
	public boolean addRequiredKeyValueTag(SteamUGCQuery query, String key, String value) {
		
		return steamUGC.addRequiredKeyValueTag(query, key, value);
		
	}

	/**
	 * @param publishedFileID
	 * @param maxAgeSeconds
	 * @return
	 * @deprecated
	 * @see com.codedisaster.steamworks.SteamUGC#requestUGCDetails(com.codedisaster.steamworks.SteamPublishedFileID, int)
	 */
	public SteamAPICall requestUGCDetails(SteamPublishedFileID publishedFileID, int maxAgeSeconds) {
		
		return steamUGC.requestUGCDetails(publishedFileID, maxAgeSeconds);
		
	}

	/**
	 * @param consumerAppID
	 * @param fileType
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#createItem(int, com.codedisaster.steamworks.SteamRemoteStorage.WorkshopFileType)
	 */
	public SteamAPICall createItem(int consumerAppID, WorkshopFileType fileType) {
		
		return steamUGC.createItem(consumerAppID, fileType);
		
	}

	/**
	 * @param consumerAppID
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#startItemUpdate(int, com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamUGCUpdateHandle startItemUpdate(int consumerAppID, SteamPublishedFileID publishedFileID) {
		
		return steamUGC.startItemUpdate(consumerAppID, publishedFileID);
		
	}

	/**
	 * Sets a new title for an item.
	 * 
	 * <p>
	 * 	The title must be limited to the size {@link UGC#PUBLISHED_DOCUMENT_TITLE_MAX PUBLISHED_DOCUMENT_TITLE_MAX}.
	 * </p>
	 * 
	 * <p>
	 * 	<b>
	 * 		Note:
	 * 	</b>
	 * 	This must be set before you submit the UGC updat ehandle using {@link UGC#submitItemUpdate(SteamUGCUpdateHandle, String) submitItemUpdate}.
	 * </p>
	 * 
	 * @param update — the workshop item handle to customize
	 * @param title — the new title of the item
	 * @return {@code true} upon success, {@code false} if {@code update} is invalid.
	 * @see com.codedisaster.steamworks.SteamUGC#setItemTitle(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public boolean setItemTitle(SteamUGCUpdateHandle update, String title) {
		
		return steamUGC.setItemTitle(update, title);
		
	}

	/**
	 * @param update
	 * @param description
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setItemDescription(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public boolean setItemDescription(SteamUGCUpdateHandle update, String description) {
		
		return steamUGC.setItemDescription(update, description);
		
	}

	/**
	 * @param update
	 * @param language
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setItemUpdateLanguage(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public boolean setItemUpdateLanguage(SteamUGCUpdateHandle update, String language) {
		
		return steamUGC.setItemUpdateLanguage(update, language);
		
	}

	/**
	 * @param update
	 * @param metaData
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setItemMetadata(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public boolean setItemMetadata(SteamUGCUpdateHandle update, String metaData) {
		
		return steamUGC.setItemMetadata(update, metaData);
		
	}

	/**
	 * @param update
	 * @param visibility
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setItemVisibility(
	 * 	com.codedisaster.steamworks.SteamUGCUpdateHandle, 
	 * 	com.codedisaster.steamworks.SteamRemoteStorage.PublishedFileVisibility
	 * )
	 */
	public boolean setItemVisibility(SteamUGCUpdateHandle update, PublishedFileVisibility visibility) {
		
		return steamUGC.setItemVisibility(update, visibility);
		
	}

	/**
	 * Sets arbitrary developer specified tags on an item.
	 * 
	 * <p>
	 * 	Each tag must be limited to 255 characters. Tag names can only include printable characters, excluding ','. For reference on what 
	 * 	characters are allowed, refer to 
	 * 		<a href="http://en.cppreference.com/w/c/string/byte/isprint">
	 * 			http://en.cppreference.com/w/c/string/byte/isprint
	 * 		</a>
	 * </p>
	 * <p>
	 * 	<b>
	 * 		NOTE: 
	 * 	</b>
	 * 	This must be set before you submit the UGC update handle using {@link UGC#submitItemUpdate(SteamUGCUpdateHandle, String) submitItemUpdate}.
	 * </p>
	 * 
	 * @param update — the workshop item update handle to customize
	 * @param tags — the list of tags to set on this item
	 * @return {@code true} upon success, {@code false} if the UGC update {@code handle} is invalid, or if one of the tags is either due to 
	 * 		   exceeding the maximum length of because it is {@code null}.
	 * @see com.codedisaster.steamworks.SteamUGC#setItemTags(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String[])
	 * @see {@link UGC#addRequiredTag(SteamUGCQuery, String) addRequiredTag}
	 * @see {@link UGC#addExcludedTag(SteamUGCQuery, String) addExcludedTag}
	 * @see {@link UGC#setMatchAnyTag(SteamUGCQuery, boolean) setMatchAnyTag}
	 */
	public boolean setItemTags(SteamUGCUpdateHandle update, String[] tags) {
		
		return steamUGC.setItemTags(update, tags);
		
	}

	/**
	 * @param update
	 * @param contentFolder
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setItemContent(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public boolean setItemContent(SteamUGCUpdateHandle update, String contentFolder) {
		
		return steamUGC.setItemContent(update, contentFolder);
		
	}

	/**
	 * @param update
	 * @param previewFile
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setItemPreview(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public boolean setItemPreview(SteamUGCUpdateHandle update, String previewFile) {
		
		return steamUGC.setItemPreview(update, previewFile);
		
	}

	/**
	 * @param update
	 * @param key
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#removeItemKeyValueTags(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public boolean removeItemKeyValueTags(SteamUGCUpdateHandle update, String key) {
		
		return steamUGC.removeItemKeyValueTags(update, key);
		
	}

	/**
	 * @param update
	 * @param key
	 * @param value
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#addItemKeyValueTag(
	 * 	com.codedisaster.steamworks.SteamUGCUpdateHandle, 
	 * 	java.lang.String, 
	 * 	java.lang.String
	 * )
	 */
	public boolean addItemKeyValueTag(SteamUGCUpdateHandle update, String key, String value) {
		
		return steamUGC.addItemKeyValueTag(update, key, value);
		
	}

	/**
	 * @param update
	 * @param changeNote
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#submitItemUpdate(com.codedisaster.steamworks.SteamUGCUpdateHandle, java.lang.String)
	 */
	public SteamAPICall submitItemUpdate(SteamUGCUpdateHandle update, String changeNote) {
		
		return steamUGC.submitItemUpdate(update, changeNote);
		
	}

	/**
	 * @param update
	 * @param updateInfo
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getItemUpdateProgress(
	 * 	com.codedisaster.steamworks.SteamUGCUpdateHandle, 
	 * 	com.codedisaster.steamworks.SteamUGC.ItemUpdateInfo
	 * )
	 */
	public ItemUpdateStatus getItemUpdateProgress(SteamUGCUpdateHandle update, ItemUpdateInfo updateInfo) {
		
		return steamUGC.getItemUpdateProgress(update, updateInfo);
		
	}

	/**
	 * @param publishedFileID
	 * @param voteUp
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#setUserItemVote(com.codedisaster.steamworks.SteamPublishedFileID, boolean)
	 */
	public SteamAPICall setUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp) {
		
		return steamUGC.setUserItemVote(publishedFileID, voteUp);
		
	}

	/**
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getUserItemVote(com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamAPICall getUserItemVote(SteamPublishedFileID publishedFileID) {
		
		return steamUGC.getUserItemVote(publishedFileID);
		
	}

	/**
	 * @param appID
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#addItemToFavorites(int, com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamAPICall addItemToFavorites(int appID, SteamPublishedFileID publishedFileID) {
		
		return steamUGC.addItemToFavorites(appID, publishedFileID);
		
	}

	/**
	 * @param appID
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#removeItemFromFavorites(int, com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamAPICall removeItemFromFavorites(int appID, SteamPublishedFileID publishedFileID) {
		
		return steamUGC.removeItemFromFavorites(appID, publishedFileID);
		
	}

	/**
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#subscribeItem(com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamAPICall subscribeItem(SteamPublishedFileID publishedFileID) {
		
		return steamUGC.subscribeItem(publishedFileID);
		
	}

	/**
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#unsubscribeItem(com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamAPICall unsubscribeItem(SteamPublishedFileID publishedFileID) {
		
		return steamUGC.unsubscribeItem(publishedFileID);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getNumSubscribedItems()
	 */
	public int getNumSubscribedItems() {
		
		return steamUGC.getNumSubscribedItems();
		
	}

	/**
	 * @param publishedFileIds
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getSubscribedItems(com.codedisaster.steamworks.SteamPublishedFileID[])
	 */
	public int getSubscribedItems(SteamPublishedFileID[] publishedFileIds) {
		
		return steamUGC.getSubscribedItems(publishedFileIds);
		
	}

	/**
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getItemState(com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public Collection<ItemState> getItemState(SteamPublishedFileID publishedFileID) {
		
		return steamUGC.getItemState(publishedFileID);
		
	}

	/**
	 * @param publishedFileID
	 * @param installInfo
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getItemInstallInfo(
	 *	com.codedisaster.steamworks.SteamPublishedFileID, 
	 *	com.codedisaster.steamworks.SteamUGC.ItemInstallInfo
	 * )
	 */
	public boolean getItemInstallInfo(SteamPublishedFileID publishedFileID, ItemInstallInfo installInfo) {
		
		return steamUGC.getItemInstallInfo(publishedFileID, installInfo);
		
	}

	/**
	 * @param publishedFileID
	 * @param downloadInfo
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#getItemDownloadInfo(
	 * 	com.codedisaster.steamworks.SteamPublishedFileID, 
	 * 	com.codedisaster.steamworks.SteamUGC.ItemDownloadInfo
	 * )
	 */
	public boolean getItemDownloadInfo(SteamPublishedFileID publishedFileID, ItemDownloadInfo downloadInfo) {
		
		return steamUGC.getItemDownloadInfo(publishedFileID, downloadInfo);
		
	}

	/**
	 * @param publishedFileID
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#deleteItem(com.codedisaster.steamworks.SteamPublishedFileID)
	 */
	public SteamAPICall deleteItem(SteamPublishedFileID publishedFileID) {
		
		return steamUGC.deleteItem(publishedFileID);
		
	}

	/**
	 * @param publishedFileID
	 * @param highPriority
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#downloadItem(com.codedisaster.steamworks.SteamPublishedFileID, boolean)
	 */
	public boolean downloadItem(SteamPublishedFileID publishedFileID, boolean highPriority) {
		
		return steamUGC.downloadItem(publishedFileID, highPriority);
		
	}

	/**
	 * @param workshopDepotID
	 * @param folder
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#initWorkshopForGameServer(int, java.lang.String)
	 */
	public boolean initWorkshopForGameServer(int workshopDepotID, String folder) {
		
		return steamUGC.initWorkshopForGameServer(workshopDepotID, folder);
		
	}

	/**
	 * @param suspend
	 * @see com.codedisaster.steamworks.SteamUGC#suspendDownloads(boolean)
	 */
	public void suspendDownloads(boolean suspend) {
		
		steamUGC.suspendDownloads(suspend);
		
	}

	/**
	 * @param publishedFileIDs
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#startPlaytimeTracking(com.codedisaster.steamworks.SteamPublishedFileID[])
	 */
	public SteamAPICall startPlaytimeTracking(SteamPublishedFileID[] publishedFileIDs) {
		
		return steamUGC.startPlaytimeTracking(publishedFileIDs);
		
	}

	/**
	 * @param publishedFileIDs
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#stopPlaytimeTracking(com.codedisaster.steamworks.SteamPublishedFileID[])
	 */
	public SteamAPICall stopPlaytimeTracking(SteamPublishedFileID[] publishedFileIDs) {
		
		return steamUGC.stopPlaytimeTracking(publishedFileIDs);
		
	}

	/**
	 * @return
	 * @see com.codedisaster.steamworks.SteamUGC#stopPlaytimeTrackingForAllItems()
	 */
	public SteamAPICall stopPlaytimeTrackingForAllItems() {
		
		return steamUGC.stopPlaytimeTrackingForAllItems();
		
	}

	@Override public void shutDown() {

		if(isFreed()) return;
		dispose();
		isFreed = true;
		
	}

	@Override public boolean isFreed() {
		
		return isFreed;
		
	}

}
