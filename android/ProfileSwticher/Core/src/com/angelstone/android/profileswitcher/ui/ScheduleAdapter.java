package com.angelstone.android.profileswitcher.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Schedule;

public class ScheduleAdapter extends ResourceCursorAdapter {
	private int mIndexStartTime;

	public ScheduleAdapter(Context context, Cursor c) {
		super(context, R.layout.schedule_list_item, c, true);
		
		mIndexStartTime = c.getColumnIndex(Schedule.COLUMN_START_TIME);
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		TextView tv = (TextView)v.findViewById(R.id.schedule_desc);
		
		tv.setText(String.valueOf(c.getLong(mIndexStartTime)));
	}

}
