
package com.kistalk.android.base;

import java.util.LinkedList;

import android.content.ContentValues;

public class FeedItem {
	public ContentValues post;
	public LinkedList<ContentValues> comments;
	
	public FeedItem(){
		post = new ContentValues();
		comments = new LinkedList<ContentValues>();
	}
}
