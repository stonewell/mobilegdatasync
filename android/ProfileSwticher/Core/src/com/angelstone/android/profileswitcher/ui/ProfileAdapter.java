package com.angelstone.android.profileswitcher.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;

public class ProfileAdapter extends ResourceCursorAdapter {

	private int mIndexName;
//	private int mIndexFlags;
//	private int mIndexDevices;
//	private int mIndexEmailVolume;
//	private int mIndexPhoneVolume;
//	private int mIndexNotifyVolume;
//	private int mIndexAlarmVolume;
//	private int mIndexPhoneRingtone;
//	private int mIndexNotifyRingtone;
//	private int mIndexAlarmRingtone;
//	private int mIndexEmailRingtone;

	public ProfileAdapter(Context context, Cursor c) {
		super(context, R.layout.profile_list_item, c, true);
		
		mIndexName = c.getColumnIndex(Profile.COLUMN_NAME);
//		mIndexFlags = c.getColumnIndex(Profile.COLUMN_FLAGS);
//
//		mIndexDevices = c.getColumnIndex(Profile.COLUMN_DEVICES);
//
//		mIndexEmailVolume = c.getColumnIndex(Profile.COLUMN_EMAIL_VOLUME);
//		mIndexPhoneVolume = c.getColumnIndex(Profile.COLUMN_PHONE_VOLUME);
//		mIndexNotifyVolume = c.getColumnIndex(Profile.COLUMN_NOTIFY_VOLUME);
//		mIndexAlarmVolume = c.getColumnIndex(Profile.COLUMN_ALARM_VOLUME);
//
//		mIndexPhoneRingtone = c.getColumnIndex(Profile.COLUMN_PHONE_RING_TONE);
//		mIndexNotifyRingtone = c
//				.getColumnIndex(Profile.COLUMN_NOTIFY_RING_TONE);
//		mIndexAlarmRingtone = c.getColumnIndex(Profile.COLUMN_ALARM_RING_TONE);
//		mIndexEmailRingtone = c.getColumnIndex(Profile.COLUMN_EMAIL_RING_TONE);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tv = (TextView)view.findViewById(R.id.profile_name);
		
		tv.setText(cursor.getString(mIndexName));
	}

}
