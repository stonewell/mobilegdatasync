package com.angelstone.android.platform;

import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

public abstract class SysCompat {

	protected Context mCtx = null;

	public static class DataColumns {
		public static class Contacts {
			final static int _ID = 0x00000001;
			final static int NAME = 0x00000002;
			final static int CUSTOM_RINGTONE = 0x00000004;
		}
	}

	public Uri CONTACT_URI = null;
	public Uri PHONE_URI = null;

	public String COLUMN_CONTACT_NAME = null;
	public String COLUMN_CONTACT_ID = null;

	public String COLUMN_PHONE_NAME = null;
	public String COLUMN_PHONE_NUMBER = null;

	public String COLUMN_PHONE_CONTACT_ID = null;
	public String COLUMN_PHONE_ID = null;
	public String PHONE_TYPE = null;
	public String COLUMN_PHONE_TYPE = null;
	public String COLUMN_PHONE_ID_v16 = null;

	public Uri PHONE_LOOKUP_FILTER_URI = null;
	public String PHONE_LOOKUP_NUMBER = null;
	public String PHONE_LOOKUP_NAME = null;
	public String PHONE_LOOKUP_TYPE = null;
	public String PHONE_LOOKUP_PHOTO_ID = null;

	public SysCompat(Context ctx) {
		mCtx = ctx;
	}

	public static SysCompat register(Context ctx) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk == 3)
			return new CompatCupcake(ctx);
		else if (sdk == 4)
			return new CompatDonut(ctx);
		else if (sdk == 8)
			return new CompatFroyo(ctx);
		else if (sdk == 9)
			return new CompatGingerBread(ctx);
		else
			// 2.0/2.0.1/2.1
			return new CompatEclair(ctx);
	}

	public abstract boolean parseSmsMessages(Intent intent,
			Map<String, String> messages);

	public abstract void setServiceForeground(Service service);

	public abstract CharSequence getPhoneTypeLabel(int type);
	
	public abstract Bitmap getPhoto(int photoId, int defaultResource);
	
	public abstract boolean enableBluetooth(boolean enable);
	
	public abstract boolean enableMobileNetwork(boolean enable);
	
	public abstract Uri getReceiptUri();
	
	public abstract boolean isScreenOn();
}
