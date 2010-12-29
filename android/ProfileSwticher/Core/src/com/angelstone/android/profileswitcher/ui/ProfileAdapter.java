package com.angelstone.android.profileswitcher.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
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

	public ProfileAdapter(Context context, Cursor c) {
		super(context, R.layout.profile_list_item, c, true);

		mIndexName = c.getColumnIndex(Profile.COLUMN_NAME);

		mIndexFlags = c.getColumnIndex(Profile.COLUMN_FLAGS);
		mIndexDevices = c.getColumnIndex(Profile.COLUMN_DEVICES);
		mIndexActive = c.getColumnIndex(Profile.COLUMN_ACTIVE);
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
			iv[i].setImageResource(-1);

		int index = 0;

		if (profile.isWifiEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_wifi_signal_4);
		else if (profile.isWifiConfigured())
			iv[index++]
					.setImageResource(R.drawable.stat_sys_wifi_signal_4_disable);

		if (profile.isGpsEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_gps_on);
		else if (profile.isGpsConfigured())
			iv[index++].setImageResource(R.drawable.stat_sys_gps_on_disable);

		if (profile.isBlueToothEnable())
			iv[index++].setImageResource(R.drawable.stat_sys_data_bluetooth);
		else if (profile.isBlueToothConfigured())
			iv[index++]
					.setImageResource(R.drawable.stat_sys_data_bluetooth_disable);

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
		case Profile.ACTIVE_MANUAL_TIME:
			statusIv.setImageResource(R.drawable.active_manual_time);
			break;
		case Profile.ACTIVE_NONE:
		default:
			statusIv.setImageResource(R.drawable.active_none);
			break;
		}
	}

}
