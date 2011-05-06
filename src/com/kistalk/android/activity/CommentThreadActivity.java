package com.kistalk.android.activity;

import com.kistalk.android.R;
import com.kistalk.android.activity.kt_extensions.KT_SimpleCursorAdapter;
import com.kistalk.android.base.KT_UploadMessage;
import com.kistalk.android.image_management.ImageController;
import com.kistalk.android.util.Constant;
import com.kistalk.android.util.DbAdapter;
import com.kistalk.android.util.UploadTask;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CommentThreadActivity extends ListActivity implements Constant {

	private int itemId;
	private ImageController imageController;
	private DbAdapter dbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		dbAdapter = new DbAdapter(this);
		super.onCreate(savedInstanceState);
		itemId = getIntent().getIntExtra(KEY_ITEM_ID, 0);
		setContentView(R.layout.thread_view_layout);
		imageController = FeedActivity.imageController;
		addImageAsHeader();
	}

	@Override
	protected void onStart() {
		super.onStart();
		populateList();
		addCommentForm();
		((EditText) findViewById(R.id.inputbox))
				.setText((String) getLastNonConfigurationInstance());

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
				(ImageView) imageItem.findViewById(R.id.image));
		imageController.start(avatarUrl,
				(ImageView) imageItem.findViewById(R.id.avatar));
		((TextView) imageItem.findViewById(R.id.user_name)).setText(userName);
		((TextView) imageItem.findViewById(R.id.description))
				.setText(description);
		((TextView) imageItem.findViewById(R.id.date)).setText(date);

		// add view as header to list
		getListView().addHeaderView(imageItem);
	}

	private synchronized void populateList() {

		dbAdapter.open();
		Cursor cur = dbAdapter.fetchComments(itemId);

		String[] displayFields = new String[] { KEY_COM_USER_NAME,
				KEY_COM_USER_AVATAR, KEY_COM_CONTENT, KEY_COM_DATE };

		int[] displayViews = new int[] { R.id.user_name, R.id.avatar,
				R.id.comment, R.id.date };

		KT_SimpleCursorAdapter adapter = new KT_SimpleCursorAdapter(this,
				R.layout.comment_item_layout, cur, displayFields, displayViews);

		setListAdapter(adapter);

		dbAdapter.close();
	}

	private synchronized void addCommentForm() {
		View commentForm = getLayoutInflater().inflate(
				R.layout.thread_comment_form_layout, null);

		getListView().addFooterView(commentForm);

		commentForm.findViewById(R.id.comment_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

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
							new UploadTask(CommentThreadActivity.this)
									.execute(message);
						}

					}
				});
	}
}