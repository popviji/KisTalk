package com.kistalk.android.util;

import com.kistalk.android.R;

public interface Constant {

	public static final String LOG_TAG = "KisTalk";

	public static final int POSTS_PER_PAGE = 15;

	public static final String SHARED_PREF_FILE = "LOGIN_SHARED_PREF_FILE";
	/*
	 * Intent constants
	 */
	public static final int REQUEST_CHOOSE_IMAGE = 1337;
	public static final int REQUEST_GET_CAMERA_PIC = 1338;
	public static final int LOGIN_REQUEST = 1339;
	public static final int REQUEST_QR_READER = 1340;
	public static final int REQUEST_THREAD_VIEW = 1341;

	public static final String KEY_REFRESH_REQUEST = "REFRESH_REQUEST";
	public static final int UPLOAD_REQUEST = 77;

	/*
	 * Key constants for ContentValues
	 */

	// Key constants for feed items
	public static final String KEY_ITEM_ID = "KEY_ITEM_ID";
	public static final String KEY_ITEM_URL_BIG = "KEY_ITEM_URL_BIG";
	public static final String KEY_ITEM_URL_SMALL = "KEY_ITEM_URL_SMALL";
	public static final String KEY_ITEM_USER_ID = "KEY_ITEM_USER_ID";
	public static final String KEY_ITEM_USER_NAME = "KEY_ITEM_USER_NAME";
	public static final String KEY_ITEM_USER_AVATAR = "KEY_ITEM_USER_AVATAR";
	public static final String KEY_ITEM_DESCRIPTION = "KEY_ITEM_DESCRIPTION";
	public static final String KEY_ITEM_DATE = "KEY_ITEM_DATE";
	public static final String KEY_ITEM_NUM_OF_COMS = "hej";

	// Key constants for feed item comments
	public static final String KEY_COM_ID = "KEY_COM_ID";
	public static final String KEY_COM_USER_ID = "KEY_COM_USER_ID";
	public static final String KEY_COM_USER_NAME = "KEY_COM_USER_NAME";
	public final static String KEY_COM_USER_AVATAR = "KEY_COM_USER_AVATAR";
	public static final String KEY_COM_CONTENT = "KEY_COM_CONTENT";
	public static final String KEY_COM_DATE = "KEY_COM_DATE";

	// Key constants for upload activity
	public static final String KEY_UPLOAD_IMAGE_URI = "KEY_UPLOAD_IMAGE_URI";
	public static final String KEY_UPLOAD_IMAGE_DESCRIPTION = "KEY_UPLOAD_IMAGE_DESCRIPTION";
	public static final String KEY_UPLOAD_IMAGE_PATH = "KEY_UPLOAD_IMAGE_PATH";
	public static final String KEY_UPLOAD_IMAGE_BITMAP = "KEY_UPLOAD_IMAGE_BITMAP";

	public static final String KEY_CURRENT_IMAGE = "KEY_CURRENT_IMAGE";

	// Key constants for ImageLoader, ImageLoaderHandler and ImageCache and
	// Shared Preferences
	public static final String KEY_URI = "KEY_URI";
	public static final String KEY_BITMAP = "KEY_BITMAP";
	public static final String KEY_RESOURCE = "KEY_RESOURCE";
	public static final String KEY_URL = "KEY_URL";
	public static final String KEY_IMAGE_CACHE_HASHMAP = "KEY_IMAGE_CACHE_HASHMAP";
	public static final String KEY_LAST_PAGE = "KEY_LAST_PAGE";

	public static final String KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION";
	public static final String KEY_COMMENT_INPUT_TEXT = "KEY_COMMENT_INPUT_TEXT";
	public static final String KEY_COMMENT_INPUT_SELECTED = "KEY_COMMENT_INPUT_FOCUS";
	public static final String KEY_UPLOAD_INPUT_TEXT = "KEY_UPLOAD_INPUT_TEXT";
	

	// public static final String KEY_DB_ADAPTER = "KEY_DB_ADAPTER";

	// Constants for webserver and more
	public static final String SCHEME = "http";
	public static final String HOST = "www.kistalk.com";
	public static final String XML_FEED_PATH = "/api/feed/android";
	public static final String XML_THREAD_PATH = "/api/thread";

	public static final String VALIDATE_CREDENTIAL_PATH = "api/validate_token";

	public static final String UPLOAD_IMAGE_PATH = "/api/images/create";
	public static final String POST_COMMENT_PATH = "/api/comment/create";

	public static final String WEBSERVER_URL = SCHEME + "://" + HOST;
	public static final String XML_FEED_FULL_URL = WEBSERVER_URL
			+ XML_FEED_PATH;

	// Argument names for webserver
	public static final String ARG_USERNAME = "username";
	public static final String ARG_TOKEN = "token";

	public static final String ARG_UPLOAD_IMAGE = "image";
	public static final String ARG_UPLOAD_DESCRIPTION = "comment";

	public static final String ARG_COMMENT_ITEMID = "image_id";
	public static final String ARG_COMMENT_CONTENT = "content";

	public static final String ARG_POSTS_PER_PAGE = "per_page";
	public static final String ARG_PAGE = "page";
	public static final String ARG_ITEM_ID = "id";

	public static final String ARG_FETCH_COMMENTS = "comments";

	public static final String FETCH_NO_COMMENTS = "0";
	public static final String FETCH_COMMENTS = "1";

	// Message constants
	public static final short UPLOAD_PHOTO_MESSAGE_TAG = 0;
	public static final short UPLOAD_COMMENT_MESSAGE_TAG = 1;

	// Dialog constants
	public static final CharSequence[] OPTIONS = { "Choose existing photo",
			"Capture a photo" };
	public static final int DIALOG_CLEAR_COMMENT_FIELD = 7;
	public static final int DIALOG_CHOOSE_OPTION = 11;
	public static final int DIALOG_LOGOUT = 13;
	public static final int DIALOG_GALLERY_OPTION = 17;
	public static final int DIALOG_PARSED_TEXT_OPTION = 23;

	// Shared preference constant for synchronize threads
	// The activity gets killed if an user turns his/hers android phone causes
	// it to be killed
	// Shared preference is persistent storage
	public static final String KEY_REFRESHING_POSTS = "KEY_REFRESHING_POSTS";

	// Databse constants
	public static final String[] FEEDACTIVITY_DISPLAY_FIELDS = {
			KEY_ITEM_USER_NAME, KEY_ITEM_USER_AVATAR, KEY_ITEM_URL_SMALL,
			KEY_ITEM_DESCRIPTION, KEY_ITEM_DATE, KEY_ITEM_NUM_OF_COMS,
			KEY_ITEM_ID };

	public static final int[] FEEDACTIVITY_DISPLAY_VIEWS = { R.id.user_name,
			R.id.avatar, R.id.image_small, R.id.description, R.id.date,
			R.id.num_of_comments, R.id.item_id };

	public static final String[] COMTHREAD_ACTIVITY_DISPLAY_FIELDS = {
			KEY_COM_USER_NAME, KEY_COM_USER_AVATAR, KEY_COM_CONTENT,
			KEY_COM_DATE, KEY_COM_ID };

	public static final int[] COMTHREAD_ACTIVITY_DISPLAY_VIEWS = {
			R.id.user_name, R.id.avatar, R.id.comment, R.id.date, R.id.com_id };

	// Error messages
	public static final String ERROR_MSG_EXT_APPLICATION = "Error: External application returned with failed result code";

	// Pattern matching
	public static final String REGEXP_URL_LINKS = "(^|[ \t\r\n])((ftp|http|https):(([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*(),;/?:@&~=%-]*))?([A-Za-z0-9$_+!*();/?:~-]))";
}
