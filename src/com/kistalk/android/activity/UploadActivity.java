package com.kistalk.android.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.kistalk.android.R;
import com.kistalk.android.base.KT_UploadMessage;
import com.kistalk.android.util.Constant;
import com.kistalk.android.util.KT_TransferManager;
import com.kistalk.android.util.UploadTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class UploadActivity extends Activity implements Constant,
		OnTouchListener {

	private ImageView uploadImage;
	private Button sendButton;
	private Uri tempFile;
	private float downXValue;
	private String username;
	private String token;
	private String currentImagePath;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_view_layout);

		LinearLayout layMain = (LinearLayout) findViewById(R.id.upload_view);
		layMain.setOnTouchListener((OnTouchListener) this);

		checkLoginState();

		Intent intent = getIntent();
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			Bundle extras = intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				currentImagePath = getPathFromContentURI(uri);

				startProcedure();
			}
		} else if (currentImagePath == null) {
			if (savedInstanceState == null)
				currentImagePath = this.getIntent().getStringExtra(
						KEY_UPLOAD_IMAGE_PATH);
			else {
				if (savedInstanceState.containsKey(KEY_CURRENT_IMAGE))
					currentImagePath = savedInstanceState
							.getString(KEY_CURRENT_IMAGE);
			}

			startProcedure();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CURRENT_IMAGE, currentImagePath);
	}
	
//	@Override
//	public Object onRetainNonConfigurationInstance() {
//		// return super.onRetainNonConfigurationInstance();
//		return ((EditText) findViewById(R.id.inputbox)).getText().toString();
//	}

	private void startProcedure() {
		final String pathImage = currentImagePath;

		File f = new File(pathImage);
		int smartImageSize = getSmartImageSize();
		Bitmap storedImage = decodeFile(f, smartImageSize);

		/*
		 * Create an OnClickListener
		 */
		OnClickListener onCL = createNewOnClickListener(pathImage);

		setupGUI(storedImage, onCL);
	}

	private void setupGUI(Bitmap storedImage, OnClickListener onCL) {
		uploadImage = (ImageView) findViewById(R.id.upload_image);
		uploadImage.setImageBitmap(storedImage);
		uploadImage.setOnClickListener(onCL);

		sendButton = (Button) findViewById(R.id.send_button);
		sendButton.setOnClickListener(onCL);

		// Normal OnClickListener for clear comment button
		findViewById(R.id.clear_comment_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (v.getId() == R.id.clear_comment_button) {
							showDialog(DIALOG_CLEAR_COMMENT_FIELD);
						}
					}
				});

		// OnLongClickListener for clear comment button
		findViewById(R.id.clear_comment_button).setOnLongClickListener(
				new OnLongClickListener() {

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

		case DIALOG_CLEAR_COMMENT_FIELD:
			AlertDialog.Builder secondBuilder = new AlertDialog.Builder(this);
			secondBuilder
					.setMessage("Clear comment field?")
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
			return secondBuilder.create();

		default:
			dialog = null;
			break;
		}
		return dialog;
	}

	public void finishActivityProcedure() {
		Intent refreshIntent = new Intent(this, FeedActivity.class);
		setResult(RESULT_OK, refreshIntent);
		finish();
	}

	private void checkLoginState() {

		sp = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);

		username = sp.getString(ARG_USERNAME, null);
		token = sp.getString(ARG_TOKEN, null);

		validateCredentials();
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

	private void startLoginActivityForResult() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivityForResult(loginIntent, LOGIN_REQUEST);
	}

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
	}

	private void takePhotoAction() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		try {
			tempFile = Uri.fromFile(File.createTempFile("image", ".jpg"));
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
			e.printStackTrace();
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT, tempFile);
		startActivityForResult(intent, REQUEST_GET_CAMERA_PIC);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
		case REQUEST_GET_CAMERA_PIC:
			if (resultCode == RESULT_OK) {
				String uriString = tempFile.toString();
				URI uRI = null;
				try {
					uRI = new URI(uriString);
				} catch (URISyntaxException e) {
					Log.e(LOG_TAG, e.toString());
					e.printStackTrace();
				}
				File f = new File(uRI);

				final String newImagePath = f.toString();
				currentImagePath = newImagePath;

				startProcedure();
			} else
				Toast.makeText(this, ERROR_MSG_EXT_APPLICATION,
						Toast.LENGTH_LONG).show();
			break;
		case REQUEST_CHOOSE_IMAGE:
			if (resultCode == RESULT_OK) {
				if (intent != null) {
					Uri recievedUri = intent.getData();
					currentImagePath = getPathFromContentURI(recievedUri);

					startProcedure();
				}
			} else
				Toast.makeText(this, ERROR_MSG_EXT_APPLICATION,
						Toast.LENGTH_LONG).show();
			break;
		case LOGIN_REQUEST:
			if (resultCode == RESULT_OK) {
				username = intent.getStringExtra(ARG_USERNAME);
				token = intent.getStringExtra(ARG_TOKEN);

				sp.edit().putString(ARG_USERNAME, username)
						.putString(ARG_TOKEN, token).commit();
			} else {
				finish();
			}
			break;
		default:
			break;
		}
	}

	private OnClickListener createNewOnClickListener(final String newPath) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.send_button:
					String comment = ((EditText) findViewById(R.id.inputbox))
							.getText().toString().trim();
					if (comment.length() < 3)
						Toast.makeText(UploadActivity.this,
								"Comment too short", Toast.LENGTH_SHORT).show();
					else if (comment.length() > 500)
						Toast.makeText(UploadActivity.this, "Comment too long",
								Toast.LENGTH_LONG).show();
					else {
						KT_UploadMessage message = new KT_UploadMessage(
								newPath, comment, -1, UPLOAD_PHOTO_MESSAGE_TAG);
						new UploadTask(UploadActivity.this, null,
								UploadActivity.this).execute(message);
					}
					break;
				case R.id.upload_image:
					showDialog(DIALOG_CHOOSE_OPTION);
					break;

				default:
					break;
				}
			}
		};
	}

	// Convert the image URI to the direct file system path of the image file
	private String getPathFromContentURI(Uri contentUri) {

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

	/* To fix OutOfMemory errors when using BitmapFactory decoding */
	private Bitmap decodeFile(File file, int maxSize) {
		Bitmap image = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(file);
			BitmapFactory.decodeStream(fis, null, o);
			try {
				fis.close();

				double scale = 1;
				if (o.outHeight > maxSize || o.outWidth > maxSize) {
					scale = Math.pow(
							2,
							(int) Math.round(Math.log(maxSize
									/ (double) Math
											.max(o.outHeight, o.outWidth))
									/ Math.log(0.5)));
				}

				// Decode with inSampleSize
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = (int) scale;
				fis = new FileInputStream(file);
				image = BitmapFactory.decodeStream(fis, null, o2);

				fis.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.toString());
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, e.toString());
		}
		return image;
	}

	private boolean isDisplayLandscape() {
		Display disp = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();

		int orentation = disp.getOrientation();

		// It is 1 or 3 depending if it's turned 90 degrees or 270 degrees
		if (orentation == 1 || orentation == 3)
			return true;
		else
			return false;

	}

	private int getSmartImageSize() {
		Display disp = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();

		if (isDisplayLandscape())
			return disp.getHeight();
		else
			return disp.getWidth();
	}

	@Override
	public boolean onTouch(View v, MotionEvent mEvent) {
		// Get the action that was done on this touch event
		switch (mEvent.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			// store the X value when the user's finger was pressed down
			downXValue = mEvent.getX();
			break;
		}

		case MotionEvent.ACTION_UP: {
			// Get the X value when the user released his/her finger
			float currentX = mEvent.getX();

			// going backwards: pushing stuff to the right
			if (downXValue < currentX) {
				// Get a reference to the ViewFlipper
				ViewFlipper vf = (ViewFlipper) findViewById(R.id.flippery);
				// Set the animation
				vf.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_out));
				// Flip!
				vf.showPrevious();
			}

			// going forwards: pushing stuff to the left
			if (downXValue > currentX) {
				// Get a reference to the ViewFlipper
				ViewFlipper vf = (ViewFlipper) findViewById(R.id.flippery);
				// Set the animation
				vf.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_in));
				// Flip!
				vf.showNext();
			}
			break;
		}
		}

		// if you return false, these actions will not be recorded
		return true;
	}
}
