package com.kistalk.android.image_management;

import java.io.IOException;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.kistalk.android.util.Constant;
import com.kistalk.android.util.KT_TransferManager;

/**
 * An background image loader. If the image to be loaded is present in the
 * cache, it is set immediately on the given view. Otherwise, a thread from a
 * thread pool will be used to download the image in the background and set the
 * image on the view as soon as it completes.
 * 
 */
public class ImageLoader implements Runnable, Constant {

	private String imageUrl;
	private ImageLoaderHandler handler;
	private ImageCache imageCache;

	public ImageLoader(String imageUrl, ImageLoaderHandler handler,
			ImageCache imageCache) {
		this.imageUrl = imageUrl;
		this.handler = handler;
		this.imageCache = imageCache;
	}

	/**
	 * The run method for a worker thread. It will first query the image cache
	 * and if it's a miss, then the worker thread will download the image from
	 * the Web.
	 */
	public void run() {
		Bundle bundle = new Bundle();
		if (!imageCache.contains(imageUrl))
			if (!downloadImage())
				return;
		bundle.putParcelable(KEY_BITMAP, imageCache.getBitmap(imageUrl));
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	private boolean downloadImage() {

		KT_TransferManager atm = new KT_TransferManager();
		String path = atm.downloadImage(imageUrl);
		try {
			if (path != null) {
				imageCache.put(imageUrl, path);
				return true;
			} else
				return false;
		} catch (IOException e) {
			Log.e(LOG_TAG, "download for " + imageUrl + " failed");
			e.printStackTrace();
		}
		return false;
	}
}
