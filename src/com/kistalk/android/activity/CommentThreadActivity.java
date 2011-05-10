package com.kistalk.android.activity;

import java.io.IOException;
import java.net.URISyntaxException;

import org.xmlpull.v1.XmlPullParserException;

import com.kistalk.android.R;
import com.kistalk.android.activity.kt_extensions.KT_SimpleCursorAdapter;
import com.kistalk.android.base.FeedItem;
import com.kistalk.android.base.KT_UploadMessage;
import com.kistalk.android.image_management.ImageController;
import com.kistalk.android.util.Constant;
import com.kistalk.android.util.DbAdapter;
import com.kistalk.android.util.KT_XMLParser;
import com.kistalk.android.util.UploadTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CommentThreadActivity extends ListActivity implements Constant {

	private int itemId;
	private ImageController imageController;
	private DbAdapter dbAdapter;

	private SharedPreferences sharedPrefs;

	private KT_SimpleCursorAdapter cursorAdapter;

	private Animation rotate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter = new DbAdapter(this);
		itemId = getIntent().getIntExtra(KEY_ITEM_ID, 0);
		setContentView(R.layout.thread_view_layout);
		imageController = FeedActivity.imageController;
		addImageAsHeader();
		loadAnimations();

		sharedPrefs = getSharedPreferences(LOGIN_SHARED_PREF_FILE, MODE_PRIVATE);

		if (savedInstanceState == null) {
			sharedPrefs.edit().putBoolean(KEY_REFRESHING_POSTS, false).commit();
			refreshThread();
		}

		cursorAdapter = initializeListAdapter();
		addCommentForm();
		((EditText) findViewById(R.id.inputbox))
				.setText((String) getLastNonConfigurationInstance());
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CLEAR_COMMENT_FIELD:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Clear comment field?")
					.setCancelable(true)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									((EditText) findViewById(R.id.inputbox))
											.setText("");
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			return builder.create();

		default:
			dialog = null;
			break;
		}
		return dialog;
	}

	private void loadAnimations() {
		rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinately);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// return super.onRetainNonConfigurationInstance();
		return ((EditText) findViewById(R.id.inputbox)).getText().toString();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/* Creates a user menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.comment_thread_option_menu, menu);
		return true;
	}

	/* The system calls this method when a user selects a menu item */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshThread();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addImageAsHeader() {
		// instantiate thread feed item layout
		View imageItem = getLayoutInflater().inflate(
				R.layout.thread_feed_item_layout, null);

		// query database
		dbAdapter.open();
		Cursor cur = dbAdapter.fetchPostFromId(itemId);

		// Extract fields from cursor
		String imageUrl = cur.getString(cur.getColumnIndex(KEY_ITEM_URL_BIG));
		String userName = cur.getString(cur.getColumnIndex(KEY_ITEM_USER_NAME));
		String avatarUrl = cur.getString(cur
				.getColumnIndex(KEY_ITEM_USER_AVATAR));
		String description = cur.getString(cur
				.getColumnIndex(KEY_ITEM_DESCRIPTION));
		String date = cur.getString(cur.getColumnIndex(KEY_ITEM_DATE));

		dbAdapter.close();

		// Set views
		imageController.start(imageUrl,
				(ImageView) imageItem.findViewById(R.id.image_big));
		imageController.start(avatarUrl,
				(ImageView) imageItem.findViewById(R.id.avatar));
		((TextView) imageItem.findViewById(R.id.user_name)).setText(userName);
		((TextView) imageItem.findViewById(R.id.description))
				.setText(description);
		((TextView) imageItem.findViewById(R.id.date)).setText(date);

		// add view as header to list
		getListView().addHeaderView(imageItem);
	}

	private synchronized KT_SimpleCursorAdapter initializeListAdapter() {

		dbAdapter.open();
		Cursor cur = dbAdapter.fetchComments(itemId);

		Resources res = getResources();
		Drawable avatarPlaceholder = res
				.getDrawable(R.drawable.placeholder_avatar);
		Drawable imageBigPlaceholder = res
				.getDrawable(R.drawable.placeholder_image_big);

		KT_SimpleCursorAdapter adapter = new KT_SimpleCursorAdapter(this,
				R.layout.comment_item_layout, cur,
				COMTHREAD_ACTIVITY_DISPLAY_FIELDS,
				COMTHREAD_ACTIVITY_DISPLAY_VIEWS, avatarPlaceholder, null,
				imageBigPlaceholder);

		setListAdapter(adapter);

		dbAdapter.close();

		return adapter;
	}

	private void updateAdapter() {
		dbAdapter.open();
		Cursor cur = dbAdapter.fetchComments(itemId);
		cursorAdapter.changeCursor(cur);
		dbAdapter.close();
	}

	/* Adds a comment form which is a fixed view at the bottom of the list */
	private synchronized void addCommentForm() {

		View commentForm = getLayoutInflater().inflate(
				R.layout.thread_comment_form_layout, null);

		getListView().addFooterView(commentForm);

		commentForm.findViewById(R.id.comment_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (v.getId() == R.id.comment_button) {

							String comment = ((EditText) findViewById(R.id.inputbox))
									.getText().toString().trim();
							if (comment.length() < 3)
								Toast.makeText(CommentThreadActivity.this,
										"Comment too short", Toast.LENGTH_LONG)
										.show();
							else if (comment.length() > 500)
								Toast.makeText(CommentThreadActivity.this,
										"Comment too long", Toast.LENGTH_LONG)
										.show();
							else {
								KT_UploadMessage message = new KT_UploadMessage(
										null, comment, itemId,
										UPLOAD_COMMENT_MESSAGE_TAG);
								new UploadTask(CommentThreadActivity.this,
										CommentThreadActivity.this, null)
										.execute(message);
							}
						}
					}
				});

		// Normal OnClickListener for clear comment button
		commentForm.findViewById(R.id.clear_comment_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (v.getId() == R.id.clear_comment_button) {
							showDialog(DIALOG_CLEAR_COMMENT_FIELD);
						}
					}
				});

		// OnLongClickListener for clear comment button
		commentForm.findViewById(R.id.clear_comment_button)
				.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						if (v.getId() == R.id.clear_comment_button) {
							((EditText) findViewById(R.id.inputbox))
									.setText("");
							return true;
						} else
							return false;
					}
				});
	}

	/*
	 * Called by the AsyncTask when the job is done
	 * 
	 * @param sucessful
	 */
	public void commentPosted(boolean successful) {
		((EditText) findViewById(R.id.inputbox)).setText("");
		((EditText) findViewById(R.id.inputbox)).clearFocus();
		refreshThread();

		/*
		 * Must be placed here in order it to properly clear focus and then let
		 * the user again to comment the thread
		 */
		// Access the soft keyboard
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// Hide soft keyboard hidden unless the user has selected the text field
		inputMethodManager.hideSoftInputFromWindow(
				((EditText) findViewById(R.id.inputbox)).getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);

		if (successful)
			Toast.makeText(this, "Comment posted", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Failed to post comment", Toast.LENGTH_LONG)
					.show();
	}

	public void refreshThread() {

		if (!sharedPrefs.getBoolean(KEY_REFRESHING_POSTS, false)) {
			sharedPrefs.edit().putBoolean(KEY_REFRESHING_POSTS, true).commit();
			findViewById(R.id.refresh_button).setVisibility(View.VISIBLE);
			findViewById(R.id.refresh_button).startAnimation(rotate);

			new AsyncTask<DbAdapter, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(DbAdapter... dbAdapters) {
					try {
						FeedItem feedItem = KT_XMLParser
								.fetchAndParseSingleThread(itemId);

						if (feedItem == null) {
							Log.e(LOG_TAG, "Problem when downloading XML file");
							return false;
						}

						dbAdapters[0].open();

						dbAdapters[0].insertPost(feedItem.post);
						dbAdapters[0].insertComments(feedItem.comments);
						
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
						updateAdapter();
					} else
						Toast.makeText(CommentThreadActivity.this,
								"Refresh failed", Toast.LENGTH_SHORT).show();

					cancel(true);
				}
			}.execute(dbAdapter);
		}
	}
}