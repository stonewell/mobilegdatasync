package com.angelstone.android.smsblocker.store;

import android.net.Uri;

/**
 * a class representing a phone-using scene
 * 
 * @see com.angelstone.android.smsblocker.store.PhoneNumberDisposition
 */
public class BlackList implements SmsBlockerContent {
	public static final Uri CONTENT_BLACK_LIST_URI = Uri.parse("content://"
			+ AUTHORITY + "/blacklist");
	public static final String _ID = "_id";
	public static final String NUMBER = "number";
	public static final String BLOCK_SMS = "block_sms";
	public static final String DEFAULT_SORT_ORDER = BLOCK_SMS + " asc," + NUMBER
			+ " asc";
	
  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.angelstone.android.smsblocker.blacklist";
  public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.angelstone.android.smsblocker.blacklist";

	public BlackList() {
	}
}
