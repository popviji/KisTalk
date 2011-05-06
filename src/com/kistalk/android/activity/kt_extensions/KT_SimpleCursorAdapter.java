package com.kistalk.android.activity.kt_extensions;

import com.kistalk.android.R;
import com.kistalk.android.activity.FeedActivity;
import com.kistalk.android.util.Constant;

import android.content.Context;
import android.database.Cursor;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class KT_SimpleCursorAdapter extends SimpleCursorAdapter implements
		Constant {

	public KT_SimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void setViewImage(ImageView v, String value) {
		if (v.getId() == R.id.avatar)
			v.setImageResource(R.drawable.placeholder_avatar);
		else if (v.getId() == R.id.image)
			v.setImageResource(R.drawable.placeholder_image);
		FeedActivity.imageController.start(value, v);
	}
}
