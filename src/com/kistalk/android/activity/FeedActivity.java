package com.kistalk.android.activity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kistalk.android.R;
import com.kistalk.android.activity.kt_extensions.KT_SimpleCursorAdapter;
import com.kistalk.android.base.FeedItem;
import com.kistalk.android.image_management.ImageController;
import com.kistalk.android.util.Constant;
import com.kistalk.android.util.DbAdapter;
import com.kistalk.android.util.KT_TransferManager;
import com.kistalk.android.util.KT_XMLParser;

public class FeedActivity extends ListActivity implements Constant {

	// public directories for cache and files
	public static File cacheDir;
	public static File publicFilesDir;

	private static String username;
	private static String token;

	private File tempFile;

	private Animation rotate;

	// private instances of classes
	public static DbAdapter dbAdapter;
	public static ImageController imageController;

	private KT_SimpleCursorAdapter cursorAdapter;

	private SharedPreferences sharedPrefs;

	private Button moreImagesButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this);
		imageController = new ImageController();
		tempFile = new File(Environment.getExternalStorageDirectory(),
				"Kistalk-TEMP-Upload_file.jpg");
		cacheDir = getCacheDir();
		if (!cacheDir.exists() && !cacheDir.mkdirs())
			Log.e(LOG_TAG, "Can't access cacheDir");
		rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinately);
		sharedPrefs = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
		username = sharedPrefs.getString(ARG_USERNAME, null);
		token = sharedPrefs.getString(ARG_TOKEN, null);

		restoreImageCache(savedInstanceState);

		// UI setup start
		setContentView(R.layout.feed_view_layout);
		// getListView().setItemsCanFocus(true);
		moreImagesButton = (Button) getLayoutInflater().inflate(
				R.layout.more_images_button, null);
		getListView().addFooterView(moreImagesButton);
		setOnClickListeners();

		cursorAdapter = (KT_SimpleCursorAdapter) getLastNonConfigurationInstance();
		if (cursorAdapter == null)
			cursorAdapter = initializeListAdapter();
		setListAdapter(cursorAdapter);

		// If activity starts (and not restarts due to orientation changes)
		if (savedInstanceState == null) {
			validateCredentials();
			sharedPrefs.edit().putBoolean(KEY_REFRESHING_POSTS, false)
					.putInt(KEY_LAST_PAGE, 1).commit();
			refreshLatestPosts();
		}
	}

	private void validateCredentials() {
		if (token == null || username == null)
			startLoginActivityForResult();
		else {
			KT_TransferManager transferManager = new KT_TransferManager();
			if (!transferManager.validate(username, token))
				startLoginActivityForResult();
		}
	}

	@SuppressWarnings("unchecked")
	private void restoreImageCache(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			HashMap<String, String> imageCacheHashMap = (HashMap<String, String>) savedInstanceState
					.getSerializable(KEY_IMAGE_CACHE_HASHMAP);
			if (imageCacheHashMap != null)
				imageController.setCacheHashMap(imageCacheHashMap);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		imageController.killExecutor();
		if (isFinishing()) {
			for (File cacheFile : cacheDir.listFiles())
				cacheFile.delete();
			tempFile.delete();
		}

	}

	private synchronized KT_SimpleCursorAdapter initializeListAdapter() {
		dbAdapter.open();
		Cursor cur = dbAdapter.fetchAllPosts();

		Resources res = getResources();
		Drawable avatarPlaceholder = res
				.getDrawable(R.drawable.placeholder_avatar);
		Drawable imageSmallPlaceholder = res
				.getDrawable(R.drawable.placeholder_image_small);

		cursorAdapter = new KT_SimpleCursorAdapter(this,
				R.layout.feed_item_layout, cur, FEEDACTIVITY_DISPLAY_FIELDS,
				FEEDACTIVITY_DISPLAY_VIEWS, avatarPlaceholder,
				imageSmallPlaceholder, null);

		return cursorAdapter;

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Called when configuration changes because
		// android:configChanges="orientation" in XML file
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(KEY_IMAGE_CACHE_HASHMAP,
				imageController.getCacheHashMap());
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return cursorAdapter;
	}

	/* Creates a user menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.app_option_menu, menu);
		return true;
	}

	/* The system calls this method when a user selects a menu item */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshLatestPosts();
			return true;
		case R.id.menu_logout:
			showDialog(DIALOG_LOGOUT);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void logout() {
		Toast.makeText(this, "Your are now logged out", Toast.LENGTH_LONG)
				.show();
		sharedPrefs.edit().remove(ARG_USERNAME).remove(ARG_TOKEN).commit();
		imageController.clearCache();
		startLoginActivityForResult();

	}

	/*
	 * Method that checks environment and variables that's necessary for the
	 * application to run
	 */

	/*
	 * help method thats shows a dialog window for debugging and testing
	 */
	public void dialog(String s) {
		Dialog d = new Dialog(this);
		TextView tv = new TextView(this);
		tv.setText(s);
		d.setContentView(tv);
		d.setTitle("Dialog");
		d.show();
	}

	private void setOnClickListeners() {

		/*
		 * Button that allows file uploading of picture
		 */
		findViewById(R.id.upload_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						showDialog(DIALOG_CHOOSE_OPTION);
					}
				});
	}

	public void onListItemClick(View v) {
		int itemId = Integer.valueOf(((TextView) v.findViewById(R.id.item_id))
				.getText().toString());
		showComments(itemId);
	}

	// test with protected void ()
	public void downloadMoreImages(View v) {
		int lastPage = sharedPrefs.getInt(KEY_LAST_PAGE, 1);
		getPosts(lastPage + 1, FETCH_NO_COMMENTS);
		sharedPrefs.edit().putInt(KEY_LAST_PAGE, lastPage + 1).commit();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CHOOSE_OPTION:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Pick an option").setCancelable(true)
					.setItems(OPTIONS, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							if (0 == id) {
								showFileChooser();
							} else if (1 == id) {
								takePhotoAction();
							}
						}
					});
			return builder.create();

		case DIALOG_LOGOUT:
			AlertDialog.Builder secondBuilder = new AlertDialog.Builder(this);
			secondBuilder
					.setMessage("Are you sure you want to logout?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									logout();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			return secondBuilder.create();

		default:
			dialog = null;
			break;
		}
		return dialog;
	}

	private void updateAdapter(int numOfPosts) {
		dbAdapter.open();
		Cursor cur = dbAdapter.fetchPosts(numOfPosts);
		cursorAdapter.changeCursor(cur);
		dbAdapter.close();
	}

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
	}

	private void takePhotoAction() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
		startActivityForResult(intent, REQUEST_GET_CAMERA_PIC);

	}

	private void showComments(int itemId) {
		Intent commentIntent = new Intent(FeedActivity.this,
				CommentThreadActivity.class);
		commentIntent.setAction(Intent.ACTION_VIEW);
		commentIntent.putExtra(KEY_ITEM_ID, itemId);
		try {
			FeedActivity.this.startActivityForResult(commentIntent,
					REQUEST_THREAD_VIEW);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
	}

	private void showUploadView(String pathToImage) {
		Intent uploadIntent = new Intent(this, UploadActivity.class);
		uploadIntent.setAction(Intent.ACTION_VIEW);
		uploadIntent.putExtra(KEY_UPLOAD_IMAGE_PATH, pathToImage);

		try {
			this.startActivityForResult(uploadIntent, UPLOAD_REQUEST);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
	}

	private synchronized void refreshLatestPosts() {
		getPosts(1, FETCH_COMMENTS);
		sharedPrefs.edit().putInt(KEY_LAST_PAGE, 1).commit();

	};

	private synchronized void getPosts(final int page,
			final String fetchComments) {

		if (!sharedPrefs.getBoolean(KEY_REFRESHING_POSTS, false)) {
			sharedPrefs.edit().putBoolean(KEY_REFRESHING_POSTS, true).commit();
			findViewById(R.id.refresh_button).setVisibility(View.VISIBLE);
			findViewById(R.id.refresh_button).startAnimation(rotate);

			new AsyncTask<DbAdapter, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(DbAdapter... dbAdapters) {
					try {
						LinkedList<FeedItem> feedItems = KT_XMLParser
								.fetchAndParse(page, fetchComments);

						if (feedItems == null) {
							Log.e(LOG_TAG, "Problem when downloading XML file");
							return false;
						}

						dbAdapters[0].open();

						for (FeedItem feedItem : feedItems) {
							dbAdapters[0].insertPost(feedItem.post);
							dbAdapters[0].insertComments(feedItem.comments);
						}
						dbAdapters[0].close();
						return true;
					} catch (XmlPullParserException e) {
						Log.e(LOG_TAG, "" + e, e);
					} catch (IOException e) {
						Log.e(LOG_TAG, "" + e, e);
					} catch (URISyntaxException e) {
						Log.e(LOG_TAG, "" + e, e);
					}
					return false;
				}

				@Override
				protected void onPostExecute(Boolean successful) {
					sharedPrefs.edit().putBoolean(KEY_REFRESHING_POSTS, false)
							.commit();
					findViewById(R.id.refresh_button).clearAnimation();
					findViewById(R.id.refresh_button).setVisibility(
							View.INVISIBLE);
					if (successful) {
						updateAdapter(page * POSTS_PER_PAGE);
					} else
						Toast.makeText(FeedActivity.this, "Refresh failed",
								Toast.LENGTH_SHORT).show();
					cancel(true);
				}
			}.execute(dbAdapter);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case LOGIN_REQUEST:
			if (resultCode == RESULT_OK) {
				username = intent.getStringExtra(ARG_USERNAME);
				token = intent.getStringExtra(ARG_TOKEN);

				sharedPrefs.edit().putString(ARG_USERNAME, username)
						.putString(ARG_TOKEN, token).commit();

				imageController.clearCache();
				refreshLatestPosts();

			} else {
				finish();
			}
			break;

		case REQUEST_GET_CAMERA_PIC:
			if (resultCode == RESULT_OK) {
				showUploadView(tempFile.toString());
			} else
				Toast.makeText(this, ERROR_MSG_EXT_APPLICATION,
						Toast.LENGTH_LONG).show();
			break;
		case REQUEST_CHOOSE_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri recievedUri = intent.getData();
				if (recievedUri != null) {
					String realPath = getPathFromContentUri(recievedUri);
					showUploadView(realPath);
				}
			}
			break;

		case UPLOAD_REQUEST:
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Upload successful", Toast.LENGTH_LONG)
						.show();
				refreshLatestPosts();
			}
			break;

		case REQUEST_THREAD_VIEW:
			updateAdapter(sharedPrefs.getInt(KEY_LAST_PAGE, 1) * POSTS_PER_PAGE);
			break;

		default:
			break;
		}

	}

	// Convert the image URI to the direct file system path of the image file
	private String getPathFromContentUri(Uri contentUri) {

		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	private void startLoginActivityForResult() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivityForResult(loginIntent, LOGIN_REQUEST);
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		FeedActivity.username = username;
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		FeedActivity.token = token;
	}

}
