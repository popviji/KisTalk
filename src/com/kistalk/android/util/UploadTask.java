package com.kistalk.android.util;

import com.kistalk.android.activity.CommentThreadActivity;
import com.kistalk.android.activity.UploadActivity;
import com.kistalk.android.base.KT_UploadMessage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

/*
 * The three generic types are:
 * Params, the type of the parameters sent to the task upon execution.
 * Progress, the type of the progress units published during the background computation.
 * Result, the type of the result of the background computation.
 * 
 * NEVER call one of the methods manually.
 * Create a new thread and invoke the method "execute(params)" where params
 * is a list of parameters.
 * Each thread can only run once.
 * 
 * All the methods in this class are thread safe.
 * 
 * A task can be cancelled at any time by invoking cancel(boolean).
 * Invoking this method will cause subsequent calls to isCancelled() to return true.
 * To ensure that a task is cancelled as quickly as possible, 
 * you should always check the return value of isCancelled() periodically 
 * from doInBackground(Object[]), if possible (inside a loop for instance.)
 * */

/* The parameters are of the type ContentValues and the result is of type String */
public class UploadTask extends AsyncTask<KT_UploadMessage, Void, Boolean>
		implements Constant {

	private Context context;
	private ProgressDialog progDialog;
	private CommentThreadActivity cThreadActivity;
	private UploadActivity uploadActivity;
	private short messageTag;

	public UploadTask(Context context, CommentThreadActivity cThreadActivity,
			UploadActivity uploadActivty) {
		super();
		this.context = context;
		this.progDialog = new ProgressDialog(context);
		this.cThreadActivity = cThreadActivity;
		this.uploadActivity = uploadActivty;
	}

	@Override
	protected void onPreExecute() {
		progDialog.setCancelable(true);
		progDialog.setMessage("Uploading. Please wait...");
		progDialog.setButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Are you sure you want to cancel?")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										cancel(true); // Kill the running thread
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
				/* Create and show dialog */
				(builder.create()).show();
			}
		});
		progDialog.show();
	}

	@Override
	protected Boolean doInBackground(KT_UploadMessage... messages) {
		KT_TransferManager transferManager = new KT_TransferManager();

		messageTag = messages[0].getMessageTag();

		/* If not cancelled or not gone through all items - do work */
		Log.i(LOG_TAG, "Uploading message");
		if (messages[0].getMessageTag() == UPLOAD_PHOTO_MESSAGE_TAG) {
			if (transferManager.uploadPhotoMessage(messages[0]))
				return true;
		} else if (messages[0].getMessageTag() == UPLOAD_COMMENT_MESSAGE_TAG) {
			if (transferManager.uploadComment(messages[0])) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		progDialog.dismiss(); // Removes the progress dialog
		if (messageTag == UPLOAD_COMMENT_MESSAGE_TAG) {
			cThreadActivity.commentPosted(result.booleanValue());
		} else {
			final boolean sucessful = result.booleanValue();
			if (sucessful) {
				uploadActivity.finishActivityProcedure();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Upload failed!")
						.setCancelable(true)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				(builder.create()).show();
			}
		}
	}
}
