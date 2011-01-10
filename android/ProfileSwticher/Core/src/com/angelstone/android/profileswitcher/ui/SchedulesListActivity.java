package com.angelstone.android.profileswitcher.ui;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.profileswitcher.utils.ProfileCache;
import com.angelstone.android.utils.GeoCodeLoader;

public class SchedulesListActivity extends ContentListBaseActivity implements
		OnItemClickListener {
	private Cursor mCursor;
	private Cursor mProfileCursor;
	private ProfileCache mProfileCache;
	private GeoCodeLoader mGeoCodeLoader;
	private int mIndexEnableSchedule;

	private ContentObserver mObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			updateView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.schedules);

		ListView v = (ListView) findViewById(R.id.schedule_list);

		mCursor = managedQuery(Schedule.CONTENT_URI, null, null, null, null);
		mIndexEnableSchedule = mCursor.getColumnIndex(Schedule.COLUMN_ENABLE);
		
		mProfileCursor = managedQuery(Profile.CONTENT_URI, null, null, null,
				null);
		mProfileCache = new ProfileCache(this, mProfileCursor);
		mGeoCodeLoader = new GeoCodeLoader(this);

		v.setAdapter(new ScheduleAdapter(this, mCursor, mProfileCache,
				mGeoCodeLoader));
		v.setOnItemClickListener(this);

		registerForContextMenu(v);

		getContentResolver().registerContentObserver(Schedule.CONTENT_URI,
				true, mObserver);

		updateView();
	}

	@Override
	protected void editContent(long id) {
		Intent intent = new Intent();
		intent.setClass(this, ScheduleEditActivity.class);
		intent.putExtra(ProfileSwitcherConstants.EXTRA_ID, id);
		startActivity(intent);
	}

	@Override
	protected void clearAllContent() {
		AlertDialog ad = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.clear_all_confirm)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								getContentResolver().delete(
										Schedule.CONTENT_URI, null, null);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
		ad.show();
	}

	@Override
	protected void deleteContent(long id) {
		Uri uri = ContentUris.withAppendedId(Schedule.CONTENT_URI, id);

		getContentResolver().delete(uri, null, null);
	}

	private void updateView() {
		mCursor.requery();

		if (mCursor == null || mCursor.getCount() == 0) {
			findViewById(R.id.empty_message).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.empty_message).setVisibility(View.GONE);
		}
	}

	@Override
	protected void updateOptionMenuItemState(Menu menu) {
		super.updateOptionMenuItemState(menu);

		int length = menu.size();

		for (int i = 0; i < length; i++) {
			MenuItem item = menu.getItem(i);

			if (item.getTitle().equals(getString(R.string.clear_all))) {
				item.setEnabled(mCursor != null && mCursor.getCount() > 0);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		getContentResolver().unregisterContentObserver(mObserver);
		mProfileCache.clear();
		mGeoCodeLoader.stop();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mGeoCodeLoader.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGeoCodeLoader.resume();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Uri uri = ContentUris.withAppendedId(Schedule.CONTENT_URI, id);
		
		mCursor.moveToPosition(position);
		int enable = (mCursor.getInt(mIndexEnableSchedule) + 1) % 2;
		
		ContentValues values = new ContentValues();
		values.put(Schedule.COLUMN_ENABLE, enable);
		
		getContentResolver().update(uri, values, null, null);
	}
}
