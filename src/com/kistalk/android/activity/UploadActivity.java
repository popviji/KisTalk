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
import com.kistalk.android.util.UploadTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_view_layout);

		LinearLayout layMain = (LinearLayout) findViewById(R.id.upload_view);
		layMain.setOnTouchListener((OnTouchListener) this);

		final String path = this.getIntent().getStringExtra(
				KEY_UPLOAD_IMAGE_PATH);

		int smartImageSize = getSmartImageSize();

		File file = new File(path);
		Bitmap storedImage = decodeFile(file, smartImageSize);

		/*
		 * Create an OnClickListener
		 */
		OnClickListener onCL = createNewOnClickListener(path);

		uploadImage = (ImageView) findViewById(R.id.upload_image);
		uploadImage.setImageBitmap(storedImage);
		uploadImage.setOnClickListener(onCL);

		sendButton = (Button) findViewById(R.id.send_button);
		sendButton.setOnClickListener(onCL);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CHOOSE_OPTION_ID:
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

		default:
			dialog = null;
		}
		return dialog;
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
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_GET_CAMERA_PIC) {
				String uriString = tempFile.toString();
				URI uRI = null;
				try {
					uRI = new URI(uriString);
				} catch (URISyntaxException e) {
					Log.e(LOG_TAG, e.toString());
					e.printStackTrace();
				}
				File f = new File(uRI);

				final String realPath = f.toString();

				int smartImageSize = getSmartImageSize();

				Bitmap newImage = decodeFile(f, smartImageSize);

				OnClickListener newOnCL = createNewOnClickListener(realPath);

				((ImageView) findViewById(R.id.upload_image))
						.setImageBitmap(newImage);

				sendButton.setOnClickListener(newOnCL);
			}
			if (requestCode == REQUEST_CHOOSE_IMAGE) {
				if (intent != null) {
					Uri recievedUri = intent.getData();
					final String realPath = getRealPathFromURI(recievedUri);

					OnClickListener newOnCL = createNewOnClickListener(realPath);

					File file = new File(realPath);

					int smartImageSize = getSmartImageSize();

					Bitmap newImage = decodeFile(file, smartImageSize);

					((ImageView) findViewById(R.id.upload_image))
							.setImageBitmap(newImage);

					sendButton.setOnClickListener(newOnCL);
				}
			}
		}
	}

	private OnClickListener createNewOnClickListener(final String newPath) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.send_button) {
					String comment = ((EditText) findViewById(R.id.inputbox))
							.getText().toString().trim();

					if (comment.length() < 3)
						Toast.makeText(UploadActivity.this,
								"Comment too short", Toast.LENGTH_SHORT).show();
					else {
						KT_UploadMessage message = new KT_UploadMessage(
								newPath, comment, -1, UPLOAD_PHOTO_MESSAGE_TAG);
						new UploadTask(UploadActivity.this).execute(message);
					}
				} else if (v.getId() == R.id.upload_image) {
					showDialog(DIALOG_CHOOSE_OPTION_ID);
				}
			}
		};
	}

	// Convert the image URI to the direct file system path of the image file
	private String getRealPathFromURI(Uri contentUri) {

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
