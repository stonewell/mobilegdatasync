package com.angelstone.android.profileswitcher.ui;

import java.text.MessageFormat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;

public class ProfileAdapter extends ResourceCursorAdapter {

	private int mIndexName;
	private int mIndexFlags;
	private int mIndexDevices;
	private int mIndexActive;
	private int mIndexExpireTime;
	private int mIndexActiveTime;
	private MessageFormat mMessageFormat;

	private boolean mShowInDialog = false;

	public ProfileAdapter(Context context, Cursor c) {
		super(context, R.layout.profile_list_item, c, true);

		mIndexName = c.getColumnIndex(Profile.COLUMN_NAME);

		mIndexFlags = c.getColumnIndex(Profile.COLUMN_FLAGS);
		mIndexDevices = c.getColumnIndex(Profile.COLUMN_DEVICES);
		mIndexActive = c.getColumnIndex(Profile.COLUMN_ACTIVE);
		mIndexActiveTime = c.getColumnIndex(Profile.COLUMN_ACTIVATE_TIME);
		mIndexExpireTime = c.getColumnIndex(Profile.COLUMN_EXPIRE_TIME);
		mMessageFormat = new MessageFormat(
				context.getString(R.string.profile_expire_template));
		mShowInDialog = false;
	}

	public ProfileAdapter(Context context, Cursor c, boolean showInDialog) {
		this(context, c);
		mShowInDialog= showInDialog;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tv = (TextView) view.findViewById(R.id.profile_name);

		tv.setText(cursor.getString(mIndexName));

		Profile profile = new Profile();
		profile.setFlags(cursor.getInt(mIndexFlags));
		profile.setDevices(cursor.getInt(mIndexDevices));

		ImageView[] iv = new ImageView[5];
		iv[0] = (ImageView) view.findViewById(R.id.status_1_img);
		iv[1] = (ImageView) view.findViewById(R.id.status_2_img);
		iv[2] = (ImageView) view.findViewById(R.id.status_3_img);
		iv[3] = (ImageView) view.findViewById(R.id.status_4_img);
		iv[4] = (ImageView) view.findViewById(R.id.status_5_img);

		for (int i = 0; i < iv.length; i++)
			iv[i].setImageResource(R.drawable.transparent);

		int index = 0;

		if (profile.isWifiEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_wifi_signal_4);
		else if (profile.isWifiConfigured())
			iv[index++].setImageResource(R.drawable.stat_sys_wifi_signal_4_disable);

		if (profile.isGpsEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_gps_on);
		else if (profile.isGpsConfigured())
			iv[index++].setImageResource(R.drawable.stat_sys_gps_on_disable);

		if (profile.isBlueToothEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_data_bluetooth);
		else if (profile.isBlueToothConfigured())
			iv[index++].setImageResource(R.drawable.stat_sys_data_bluetooth_disable);

		if (profile.isMuteEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_ringer_silent);

		if (profile.isVibrateEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_ringer_vibrate);

		ImageView statusIv = (ImageView) view.findViewById(R.id.status_img);

		int active = cursor.getInt(mIndexActive);

		switch (active) {
		case Profile.ACTIVE_AUTO:
			statusIv.setImageResource(R.drawable.active_auto);
			break;
		case Profile.ACTIVE_MANUAL:
			statusIv.setImageResource(R.drawable.active_manual);
			break;
		case Profile.ACTIVE_MANUAL_TIME: {
			long expireTime = cursor.getLong(mIndexExpireTime);
			long activeTime = cursor.getLong(mIndexActiveTime);
			long duration = ((activeTime + expireTime * 1000) - System
					.currentTimeMillis()) / 1000;

			if (duration <= 0)
				statusIv.setImageResource(R.drawable.active_none);
			else {
				statusIv.setImageResource(R.drawable.active_manual_time);
				tv = (TextView) view.findViewById(R.id.profile_active_time);

				long hour = duration / 3600;
				long minute = (duration % 3600) / 60;
				long second = duration % 60;

				StringBuilder sb = new StringBuilder();

				if (hour > 0)
					sb.append(hour)
							.append(" ")
							.append(
									hour > 1 ? context.getString(R.string.multi_hour) : context
											.getString(R.string.single_hour)).append(" ");
				if (minute > 0)
					sb.append(minute)
							.append(" ")
							.append(
									minute > 1 ? context.getString(R.string.multi_minute)
											: context.getString(R.string.single_minute)).append(" ");
				if (second > 0)
					sb.append(second)
							.append(" ")
							.append(
									second > 1 ? context.getString(R.string.multi_second)
											: context.getString(R.string.single_second)).append(" ");

				tv.setText(mMessageFormat.format(new Object[] { sb.toString() }));
			}
		}
			break;
		case Profile.ACTIVE_NONE:
		default:
			statusIv.setImageResource(R.drawable.active_none);
			break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);

		if (mShowInDialog)
			view.setBackgroundColor(Color.argb(190, 0, 0, 0));
		
		return view;
	}

}
