package com.angelstone.android.platform;

import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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

	public SysCompat(Context ctx) {
		mCtx = ctx;
	}

	public static SysCompat register(Context ctx) {
		String release = android.os.Build.VERSION.RELEASE;
		if (release.equals("1.5"))
			return new CompatCupcake(ctx);
		else if (release.equals("1.6"))
			return new CompatDonut(ctx);
		else if (release.equals("2.2"))
			return new CompatFroyo(ctx);
		else
			// 2.0/2.0.1/2.1
			return new CompatEclair(ctx);
	}

	public abstract boolean parseSmsMessages(Intent intent,
			Map<String, String> messages);

	public abstract void setServiceForeground(Service service);

	public abstract boolean SetRingerSilence(AudioManager audioManager,
			boolean beforeQueryAction);
}
