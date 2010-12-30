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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;

public class ProfilesListActivity extends ContentListBaseActivity {
	private Cursor mCursor;
	private int mContextMenuStartId;

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

		setContentView(R.layout.profiles);

		ListView v = (ListView) findViewById(R.id.profile_list);

		mCursor = managedQuery(Profile.CONTENT_URI, null, null, null, null);

		v.setAdapter(new ProfileAdapter(this, mCursor));

		registerForContextMenu(v);

		getContentResolver().registerContentObserver(Profile.CONTENT_URI, true,
				mObserver);

		updateView();
	}

	@Override
	protected void editContent(long id) {
		Intent intent = new Intent();
		intent.setClass(this, ProfileEditActivity.class);
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
										Profile.CONTENT_URI, null, null);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
		ad.show();
	}

	@Override
	protected void deleteContent(long id) {
		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, id);

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
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!super.onContextItemSelected(item)) {
			AdapterContextMenuInfo aci = (AdapterContextMenuInfo) item
					.getMenuInfo();
			final long id = aci.id;

			switch (item.getItemId() - mContextMenuStartId) {
			case 1: {
				Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, id);

				Cursor c = getContentResolver().query(uri, null, null, null,
						null);

				try {
					c.moveToNext();

					if (Profile.ACTIVE_NONE != c.getInt(c
							.getColumnIndex(Profile.COLUMN_ACTIVE))) {
						AlertDialog ad = new AlertDialog.Builder(this)
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setTitle(R.string.disable_confirm)
								.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												activateProfile(id,
														Profile.ACTIVE_NONE, 0);
											}
										})
								.setNegativeButton(android.R.string.cancel,
										null).create();
						ad.show();
					} else {
						activateProfile(id, Profile.ACTIVE_MANUAL, 0);
					}
				} finally {
					c.close();
				}
				break;
			}
			case 2: {
				chooseExpireTime(id);
				break;
			}

			default:
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		mContextMenuStartId = menu.size();

		AdapterContextMenuInfo aci = (AdapterContextMenuInfo) menuInfo;
		long id = aci.id;

		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, id);

		Cursor c = getContentResolver().query(uri, null, null, null, null);

		try {
			c.moveToNext();

			if (Profile.ACTIVE_NONE != c.getInt(c
					.getColumnIndex(Profile.COLUMN_ACTIVE))) {
				menu.add(0, mContextMenuStartId + 1, mContextMenuStartId + 1,
						R.string.disable);
			} else {
				menu.add(0, mContextMenuStartId + 1, mContextMenuStartId + 1,
						R.string.enable);
				menu.add(0, mContextMenuStartId + 2, mContextMenuStartId + 2,
						R.string.enable_profile_time);
			}
		} finally {
			c.close();
		}
	}

	protected void activateProfile(long id, int active, long seconds) {
		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, id);

		ContentValues values = new ContentValues();
		values.put(Profile.COLUMN_ACTIVE, active);
		values.put(Profile.COLUMN_ACTIVATE_TIME, System.currentTimeMillis());
		values.put(Profile.COLUMN_EXPIRE_TIME, seconds);

		getContentResolver().update(uri, values, null, null);
	}

	private void chooseExpireTime(final long id) {
		final String[] expireTimeLabels = getResources().getStringArray(
				R.array.expire_time_label_array);

		final int[] expireTimes = getResources().getIntArray(
				R.array.expire_time_array);

		// Adapter that shows a list of string resources
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, expireTimeLabels) {
		};

		final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				if (which < expireTimes.length) {
					activateProfile(id, Profile.ACTIVE_MANUAL_TIME,
							expireTimes[which]);
				} else {
					// Customize
					customizeExpireTime(id);
				}
			}
		};

		new AlertDialog.Builder(this).setTitle(R.string.enable_profile_time)
				.setNegativeButton(android.R.string.cancel, null)
				.setSingleChoiceItems(adapter, -1, clickListener).show();

	}

	protected void customizeExpireTime(final long id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View v = factory.inflate(R.layout.customize_time, null);
		final EditText etHour = (EditText) v.findViewById(R.id.hour);
		final EditText etMin = (EditText) v.findViewById(R.id.minute);

		etHour.setText("4");
		etMin.setText("0");

		final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				long seconds = 0;

				try {
					seconds = Long.parseLong(etHour.getText().toString())
							* 3600 + Long.parseLong(etMin.getText().toString())
							* 60;
				} catch (Throwable t) {
				}

				if (seconds == 0) {
					showToast(getString(R.string.expire_time_is_not_valid));
				} else {
					activateProfile(id, Profile.ACTIVE_MANUAL_TIME, seconds);
				}
			}
		};

		new AlertDialog.Builder(this).setTitle(R.string.enable_profile_time)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, clickListener)
				.setView(v).show();
	}

}
