package com.kistalk.android.activity.kt_extensions;

import com.kistalk.android.R;
import com.kistalk.android.activity.FeedActivity;
import com.kistalk.android.util.Constant;

import android.content.Context;
import android.database.Cursor;
import android.graphics.AvoidXfermode;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class KT_SimpleCursorAdapter extends SimpleCursorAdapter implements
		Constant {

	Drawable avatarPlaceholder;
	Drawable imageSmallPlaceholder;
	Drawable imageBigPlaceholder;

	public KT_SimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, Drawable avatarPlaceholder,
			Drawable imageSmallPlaceholder, Drawable imageBigPlaceholder) {
		super(context, layout, c, from, to);
		this.avatarPlaceholder = avatarPlaceholder;
		this.imageSmallPlaceholder = imageSmallPlaceholder;
		this.imageBigPlaceholder = imageBigPlaceholder;
	}

	@Override
	public void setViewImage(ImageView v, String value) {
		if (v.getId() == R.id.avatar)
			v.setImageDrawable(avatarPlaceholder);
		else if (v.getId() == R.id.image_big)
			v.setImageDrawable(imageBigPlaceholder);
		else if (v.getId() == R.id.image_small)
			v.setImageDrawable(imageSmallPlaceholder);
		FeedActivity.imageController.start(value, v);
	}
}
