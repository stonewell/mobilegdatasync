package com.angelstone.android.profileswitcher.ui;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.profileswitcher.utils.ProfileCache;
import com.angelstone.android.utils.DaysOfWeek;
import com.angelstone.android.utils.GeoCodeLoader;

public class ScheduleAdapter extends ResourceCursorAdapter {
	private int mIndexStartTime;
//	private int mIndexLocation;
	private int mIndexDaysOfWeek;
	private int mIndexProfileId;
	private int mIndexLabel;
	private int mIndexEnable;

	private DateFormat mTimeFormat;
	private ProfileCache mProfileCache;
//	private GeoCodeLoader mGeoCodeLoader;

	public ScheduleAdapter(Context context, Cursor c, ProfileCache profileCache, GeoCodeLoader geoCodeLoader) {
		super(context, R.layout.schedule_list_item, c, true);

		mIndexStartTime = c.getColumnIndex(Schedule.COLUMN_START_TIME);
//		mIndexLocation = c.getColumnIndex(Schedule.COLUMN_LOCATION);
		mIndexDaysOfWeek = c.getColumnIndex(Schedule.COLUMN_REPEAT_WEEKDAYS);
		mIndexProfileId = c.getColumnIndex(Schedule.COLUMN_PROFILE_ID);
		mIndexLabel = c.getColumnIndex(Schedule.COLUMN_LABEL);
		mIndexEnable = c.getColumnIndex(Schedule.COLUMN_ENABLE);

		mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
		mProfileCache = profileCache;
//		mGeoCodeLoader = geoCodeLoader;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		TextView tv = (TextView) v.findViewById(R.id.item_schedule_time);

		long mills = c.getLong(mIndexStartTime);

		if (mills > 0) {
			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(mills);

			tv.setText(mTimeFormat.format(time.getTime()));
			tv.setVisibility(View.VISIBLE);
		} else {
			tv.setVisibility(View.GONE);
		}

//		tv = (TextView) v.findViewById(R.id.item_schedule_location);

//		String loc = c.getString(mIndexLocation);
//
//		if (TextUtils.isEmpty(loc)) {
//			tv.setVisibility(View.GONE);
//		} else {
//			tv.setVisibility(View.VISIBLE);
//			mGeoCodeLoader.loadGeoCode(tv, LocationUtils.locationFromString(loc));
//		}

		tv = (TextView) v.findViewById(R.id.item_schedule_daysofweek);

		DaysOfWeek daysOfWeek = new DaysOfWeek(c.getInt(mIndexDaysOfWeek));
		tv.setText(daysOfWeek.toString(context, true));

		tv = (TextView) v.findViewById(R.id.item_schedule_profile);

		long profileId = c.getLong(mIndexProfileId);

		tv.setText(mProfileCache.getLabel(profileId));

		tv = (TextView) v.findViewById(R.id.item_schedule_label);

		String label = c.getString(mIndexLabel);

		if (TextUtils.isEmpty(label)) {
			tv.setVisibility(View.GONE);
		} else {
			tv.setVisibility(View.VISIBLE);
			tv.setText(label);
		}
		
		ImageView iv = (ImageView)v.findViewById(R.id.item_schedule_enable);
		
		iv.setSelected(c.getInt(mIndexEnable) == 1);
	}

}
