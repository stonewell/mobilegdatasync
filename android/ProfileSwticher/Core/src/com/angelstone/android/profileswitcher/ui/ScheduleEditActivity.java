package com.angelstone.android.profileswitcher.ui;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.profileswitcher.utils.Alarms;
import com.angelstone.android.profileswitcher.utils.ProfileCache;
import com.angelstone.android.utils.DaysOfWeek;

public class ScheduleEditActivity extends EditBaseActivity implements
		OnClickListener {
	private static final int TIME_DIALOG_ID = 1001;
	private static final int PROFILE_CHOOSER_DIALOG_ID = 1002;
	private static final int REPEAT_CHOOSER_DIALOG_ID = 1003;
	// private static final int LOCATION_CHOOSER_DIALOG_ID = 1004;
	private static final int LABEL_CHOOSER_DIALOG_ID = 1005;
	private static final String DATA_ID = "_data_id";
	private static final String DATA_TIME = "_data_time";
//	private static final String DATA_SAVED_LOC = "_data_saved_loc";
	private static final String DATA_PROFILE_ID = "_data_profile_id";
	private static final String DATA_DAYS_OF_WEEK = "_data_days_of_week";
	private static final String DATA_LABEL = "_data_label";
//	private static final String DATA_SAVED_LOC_DESC = "_data_saved_loc_desc";

	private TextView mTextTimeDesc;
	private TextView mTextProfileDesc;
	private TextView mTextRepeatDesc;
//	private TextView mTextLocationDesc;
	private TextView mTextLabel;

	private long mId;

	private Calendar mTime = null;
	private DateFormat mTimeFormat;

	private long mProfileId = -1;
	private int mIndexProfileId;
	private ProfileAdapter mProfileAdapter;
	private Cursor mProfileCursor;
	private ProfileCache mProfileCache;

	private DaysOfWeek mDaysOfWeek;
	private String[] mWeekDays;

	// private LocationManager mLocationManager;
	// private Location mCurrentLoc;
	// private Location mSavedLoc;
	// private GeoCodeLoader mGeoCodeLoader;
	// private String mSavedLocDesc;

	// private boolean mLocDlgShown;
	// private boolean mUseCurrentLoc;
	// private EditText mEditLatitude;
	// private EditText mEditLongitude;
	// private EditText mEditAltitude;
	// private CheckBox mChkUseAltitude;

	private String mLabel = null;
	private EditText mEditLabel;

	private final TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (mTime == null)
				mTime = Calendar.getInstance();

			mTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mTime.set(Calendar.MINUTE, minute);

			updateView();
		}
	};

	private final DialogInterface.OnClickListener mProfileListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();

			Cursor c = (Cursor) mProfileAdapter.getItem(which);

			mProfileId = c.getLong(mIndexProfileId);

			updateView();
		}
	};

	private final DialogInterface.OnMultiChoiceClickListener mRepeatListener = new DialogInterface.OnMultiChoiceClickListener() {
		public void onClick(DialogInterface dialog, int which, boolean check) {
			mDaysOfWeek.set(which, check);
		}
	};

	// private final LocationListener mLocationListener = new LocationListener()
	// {
	//
	// @Override
	// public void onStatusChanged(String provider, int status, Bundle extras) {
	//
	// }
	//
	// @Override
	// public void onProviderEnabled(String provider) {
	//
	// }
	//
	// @Override
	// public void onProviderDisabled(String provider) {
	//
	// }
	//
	// @Override
	// public void onLocationChanged(Location location) {
	// if (PhoneToolsUtil.isBetterLocation(location, mCurrentLoc)) {
	// Location oldLoc = mCurrentLoc;
	// mCurrentLoc = location;
	//
	// if (mLocDlgShown && mUseCurrentLoc
	// && !isSameLocationAsUI(oldLoc)) {
	// updateLocationUI(mCurrentLoc);
	// }
	// }
	// }
	// };
	//
	// private final DialogInterface.OnClickListener mLocationSetListener = new
	// DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	//
	// updateSavedLoc(getLocationFromUI());
	//
	// updateView();
	// }
	// };

	private final DialogInterface.OnClickListener mLabelSetListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {

			mLabel = mEditLabel.getText().toString();

			updateView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.schedule_edit);

		Intent intent = getIntent();
		mId = intent.getLongExtra(ProfileSwitcherConstants.EXTRA_ID, -1);

		mTextTimeDesc = (TextView) findViewById(R.id.time_desc);
		mTextProfileDesc = (TextView) findViewById(R.id.profile_desc);
		mTextRepeatDesc = (TextView) findViewById(R.id.repeat_desc);
		// mTextLocationDesc = (TextView) findViewById(R.id.location_desc);
		mTextLabel = (TextView) findViewById(R.id.schedule_label);

		int[] tr_ids = new int[] { // R.id.tr_location,
		R.id.tr_profile, R.id.tr_repeat, R.id.tr_schedule_label, R.id.tr_time };

		for (int id : tr_ids) {
			TableRow tmp = (TableRow) findViewById(id);
			tmp.setOnClickListener(this);
		}

		int[] img_btn_ids = new int[] { // R.id.img_btn_location,
		R.id.img_btn_profile, R.id.img_btn_repeat, R.id.img_btn_schedule_label };

		for (int id : img_btn_ids) {
			ImageButton tmp = (ImageButton) findViewById(id);
			tmp.setOnClickListener(this);
		}

		mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);

		mProfileCursor = managedQuery(Profile.CONTENT_URI, null, null, null,
				null);
		mProfileAdapter = new ProfileAdapter(this, mProfileCursor, true);
		mIndexProfileId = mProfileCursor.getColumnIndex(Profile.COLUMN_ID);
		mProfileCache = new ProfileCache(this, mProfileCursor);

		// mGeoCodeLoader = new GeoCodeLoader(this);

		updateData();
		updateView();
	}

	private void updateView() {
		if (mTime != null)
			mTextTimeDesc.setText(mTimeFormat.format(mTime.getTime()));
		else
			mTextTimeDesc.setText("");

		if (mProfileId != -1) {
			mTextProfileDesc.setText(mProfileCache.getLabel(mProfileId));
		}

		mTextRepeatDesc.setText(mDaysOfWeek.toString(this, true));

		// if (mSavedLocDesc == null) {
		// mGeoCodeLoader.loadGeoCode(mTextLocationDesc, mSavedLoc);
		// } else {
		// mTextLocationDesc.setText(mSavedLocDesc);
		// }

		mTextLabel.setText(mLabel == null ? "" : mLabel);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tr_time:
			showDialog(TIME_DIALOG_ID);
			break;
		case R.id.tr_profile:
		case R.id.img_btn_profile:
			if (mProfileCursor == null || mProfileCursor.getCount() == 0) {
				new AlertDialog.Builder(this).setTitle(R.string.label_profile)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setNegativeButton(android.R.string.cancel, null)
						.setMessage(R.string.warn_no_profile).show();
			} else {
				showDialog(PROFILE_CHOOSER_DIALOG_ID);
			}
			break;
		case R.id.tr_repeat:
		case R.id.img_btn_repeat:
			showDialog(REPEAT_CHOOSER_DIALOG_ID);
			break;
		// case R.id.tr_location:
		// case R.id.img_btn_location: {
		// if (!PhoneToolsUtil.hasLocProviders(mLocationManager)) {
		// new AlertDialog.Builder(this).setTitle(R.string.label_location)
		// .setIcon(android.R.drawable.ic_dialog_alert)
		// .setNegativeButton(android.R.string.cancel, null)
		// .setMessage(R.string.warn_no_loc_providers).show();
		// } else {
		// showDialog(LOCATION_CHOOSER_DIALOG_ID);
		// }
		// }
		// break;
		case R.id.tr_schedule_label:
		case R.id.img_btn_schedule_label:
			showDialog(LABEL_CHOOSER_DIALOG_ID);
			break;
		case R.id.chk_use_altitude: {
			View vRoot = v.getRootView();
			final View rowAltitude = (View) vRoot
					.findViewById(R.id.row_altitude);
			rowAltitude.setVisibility(((CheckBox) v).isChecked() ? View.VISIBLE
					: View.GONE);

		}
			break;
		// case R.id.btn_use_current: {
		// mUseCurrentLoc = true;
		// updateLocationUI(mCurrentLoc);
		// }
		// break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID: {
			Calendar time = Calendar.getInstance();
			if (mTime != null)
				time = mTime;
			return new TimePickerDialog(this, mTimeSetListener,
					time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),
					true);
		}
		case PROFILE_CHOOSER_DIALOG_ID: {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.title_profiles)
					.setNegativeButton(android.R.string.cancel, null)
					.setSingleChoiceItems(mProfileAdapter,
							mProfileCache.getPosition(mProfileId),
							mProfileListener).create();
		}
		case REPEAT_CHOOSER_DIALOG_ID: {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.label_repeat)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									updateView();
								}
							})
					.setMultiChoiceItems(mWeekDays,
							mDaysOfWeek.getBooleanArray(), mRepeatListener)
					.create();
		}
			// case LOCATION_CHOOSER_DIALOG_ID: {
			// LayoutInflater factory = LayoutInflater.from(this);
			// final View v = factory.inflate(R.layout.location_edit, null);
			//
			// final CheckBox chkUseAltitude = (CheckBox) v
			// .findViewById(R.id.chk_use_altitude);
			// final Button btnUseCurrentLoc = (Button) v
			// .findViewById(R.id.btn_use_current);
			//
			// chkUseAltitude.setOnClickListener(this);
			// btnUseCurrentLoc.setOnClickListener(this);
			//
			// return new AlertDialog.Builder(this)
			// .setTitle(R.string.label_location)
			// .setNegativeButton(android.R.string.cancel, null)
			// .setPositiveButton(android.R.string.ok,
			// mLocationSetListener).setView(v).create();
			// }
		case LABEL_CHOOSER_DIALOG_ID: {
			LayoutInflater factory = LayoutInflater.from(this);
			final View v = factory.inflate(R.layout.label_edit, null);

			return new AlertDialog.Builder(this)
					.setTitle(R.string.label_schedule_label)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok, mLabelSetListener)
					.setView(v).create();
		}
		default:
			break;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
		// case LOCATION_CHOOSER_DIALOG_ID: {
		// mLocDlgShown = true;
		//
		// mChkUseAltitude = (CheckBox) dialog
		// .findViewById(R.id.chk_use_altitude);
		// mEditLatitude = (EditText) dialog.findViewById(R.id.latitude);
		// mEditLongitude = (EditText) dialog.findViewById(R.id.longitude);
		// mEditAltitude = (EditText) dialog.findViewById(R.id.altitude);
		// final View rowAltitude = dialog.findViewById(R.id.row_altitude);
		//
		// mChkUseAltitude.setChecked(false);
		// rowAltitude.setVisibility(View.GONE);
		//
		// mLocDlgShown = true;
		//
		// if (mSavedLoc != null) {
		// mUseCurrentLoc = false;
		// updateLocationUI(mSavedLoc);
		// } else {
		// mUseCurrentLoc = true;
		// updateLocationUI(mCurrentLoc);
		// }
		// }
		// break;
		case LABEL_CHOOSER_DIALOG_ID: {
			mEditLabel = (EditText) dialog.findViewById(R.id.label);

			mEditLabel.setText(mLabel == null ? "" : mLabel);
		}
			break;
		}
	}

	private void updateData() {
		String[] weekdays = new DateFormatSymbols().getWeekdays();
		mWeekDays = new String[] { weekdays[Calendar.MONDAY],
				weekdays[Calendar.TUESDAY], weekdays[Calendar.WEDNESDAY],
				weekdays[Calendar.THURSDAY], weekdays[Calendar.FRIDAY],
				weekdays[Calendar.SATURDAY], weekdays[Calendar.SUNDAY], };

		mDaysOfWeek = new DaysOfWeek(0);

		// mLocationManager = (LocationManager)
		// getSystemService(Context.LOCATION_SERVICE);
		//
		// mCurrentLoc =
		// mLocationManager.getLastKnownLocation(PhoneToolsUtil.getLocProvider(mLocationManager));
		// mLocationManager.requestLocationUpdates(PhoneToolsUtil.getLocProvider(mLocationManager),
		// 0, 0,
		// mLocationListener);
		//
		// updateSavedLoc(null);

		if (mId < 0)
			return;

		Uri uri = ContentUris.withAppendedId(Schedule.CONTENT_URI, mId);

		Cursor c = getContentResolver().query(uri, null, null, null, null);

		try {
			if (!c.moveToNext())
				return;

			mLabel = c.getString(c.getColumnIndex(Schedule.COLUMN_LABEL));
			mDaysOfWeek.setCoded(c.getInt(c
					.getColumnIndex(Schedule.COLUMN_REPEAT_WEEKDAYS)));
			long time = c.getLong(c.getColumnIndex(Schedule.COLUMN_START_TIME));
			if (time > 0) {
				mTime = Calendar.getInstance();
				mTime.setTimeInMillis(time);
			}

			mProfileId = c
					.getLong(c.getColumnIndex(Schedule.COLUMN_PROFILE_ID));

//			String locStr = c.getString(c
//					.getColumnIndex(Schedule.COLUMN_LOCATION));
//
//			Location location = LocationUtils.locationFromString(locStr);
//
//			updateSavedLoc(location);
		} finally {
			c.close();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// mLocationManager.removeUpdates(mLocationListener);
		// mGeoCodeLoader.stop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// mLocationManager.removeUpdates(mLocationListener);
		// mGeoCodeLoader.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// mLocationManager.requestLocationUpdates(PhoneToolsUtil.getLocProvider(mLocationManager),
		// 0, 0,
		// mLocationListener);
		//
		// mGeoCodeLoader.resume();
		//
		// updateSavedLoc(mSavedLoc, mSavedLocDesc);
	}

	// protected boolean isSameLocationAsUI(Location oldLoc) {
	// if (oldLoc == null)
	// return false;
	//
	// return oldLoc.equals(getLocationFromUI());
	// }
	//
	// protected void updateLocationUI(final Location loc) {
	// if (loc == null)
	// return;
	//
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// mEditAltitude.setText(String.valueOf(loc.getAltitude()));
	// mEditLatitude.setText(String.valueOf(loc.getLatitude()));
	// mEditLongitude.setText(String.valueOf(loc.getLongitude()));
	// }
	// });
	// }
	//
	// protected Location getLocationFromUI() {
	// double altitude = 0;
	// double longitude = 0;
	// double latitude = 0;
	//
	// String strLongitude = mEditLongitude.getText().toString();
	// String strLatitude = mEditLatitude.getText().toString();
	//
	// if (TextUtils.isEmpty(strLatitude) || TextUtils.isEmpty(strLongitude)) {
	// showToast(MessageFormat.format(
	// getString(R.string.invalid_location_template),
	// new Object[] { getString(R.string.label_latitude),
	// getString(R.string.label_longitude) }));
	// return null;
	// }
	//
	// String strAltitude = null;
	//
	// if (mChkUseAltitude.isChecked()) {
	// strAltitude = mEditAltitude.getText().toString();
	//
	// if (TextUtils.isEmpty(strAltitude)) {
	// showToast(MessageFormat.format(
	// getString(R.string.invalid_location_template_2),
	// new Object[] { getString(R.string.label_latitude),
	// getString(R.string.label_longitude),
	// getString(R.string.label_altitude) }));
	// return null;
	// }
	// }
	//
	// try {
	// longitude = Double.parseDouble(strLongitude);
	// latitude = Double.parseDouble(strLatitude);
	//
	// if (!TextUtils.isEmpty(strAltitude))
	// altitude = Double.parseDouble(strAltitude);
	// } catch (Throwable t) {
	// if (mChkUseAltitude.isChecked()) {
	// showToast(MessageFormat.format(
	// getString(R.string.invalid_location_template_2),
	// new Object[] { getString(R.string.label_latitude),
	// getString(R.string.label_longitude),
	// getString(R.string.label_altitude) }));
	// } else {
	// showToast(MessageFormat.format(
	// getString(R.string.invalid_location_template),
	// new Object[] { getString(R.string.label_latitude),
	// getString(R.string.label_longitude) }));
	// }
	// return null;
	// }
	//
	// Location loc = new Location(mCurrentLoc);
	// loc.setLatitude(latitude);
	// loc.setLongitude(longitude);
	//
	// if (mChkUseAltitude.isChecked()) {
	// loc.setAltitude(altitude);
	// } else {
	// loc.removeAltitude();
	// }
	//
	// return loc;
	// }
	//
	// protected void updateSavedLoc(Location location) {
	// updateSavedLoc(location, null);
	// }
	//
	// protected void updateSavedLoc(Location location, String defaultDesc) {
	// mSavedLoc = location;
	//
	// mLocDlgShown = false;
	// mUseCurrentLoc = false;
	//
	// if (defaultDesc == null) {
	// mGeoCodeLoader.loadGeoCode(mTextLocationDesc, location);
	// } else {
	// mTextLocationDesc.setText(defaultDesc);
	// }
	// }

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState != null) {
			mId = savedInstanceState.getLong(DATA_ID, mId);

			long time = savedInstanceState.getLong(DATA_TIME, -1);

			if (time > 0) {
				mTime = Calendar.getInstance();
				mTime.setTimeInMillis(time);
			}

			mProfileId = savedInstanceState
					.getLong(DATA_PROFILE_ID, mProfileId);

			int code = savedInstanceState.getInt(DATA_DAYS_OF_WEEK, -1);

			if (code >= 0) {
				mDaysOfWeek.setCoded(code);
			}

			String label = savedInstanceState.getString(DATA_LABEL);

			if (!TextUtils.isEmpty(label))
				mLabel = label;

			// Location loc = savedInstanceState.getParcelable(DATA_SAVED_LOC);
			// if (loc != null)
			// mSavedLoc = loc;
			//
			// label = savedInstanceState.getString(DATA_SAVED_LOC_DESC);
			// if (!TextUtils.isEmpty(label))
			// mSavedLocDesc = label;

			updateView();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(DATA_ID, mId);

		if (mTime != null)
			outState.putLong(DATA_TIME, mTime.getTimeInMillis());

		outState.putLong(DATA_PROFILE_ID, mProfileId);

		outState.putInt(DATA_DAYS_OF_WEEK, mDaysOfWeek.getCoded());

		// if (mSavedLoc != null) {
		// outState.putParcelable(DATA_SAVED_LOC, mSavedLoc);
		//
		// String saved = mTextLocationDesc.getText().toString();
		//
		// if (!saved.equals(LocationUtils.locationDefaultDisplay(this,
		// mSavedLoc))) {
		// outState.putString(DATA_SAVED_LOC_DESC, saved);
		// }
		// }

		if (!TextUtils.isEmpty(mLabel))
			outState.putString(DATA_LABEL, mLabel);
	}

	@Override
	protected void saveContent() {
		ContentValues values = new ContentValues();

		if (!validateAndCreateValues(values)) {
			return;
		}

		if (mId < 0) {
			Uri uri = getContentResolver().insert(Schedule.CONTENT_URI, values);

			mId = ContentUris.parseId(uri);
		} else {
			Uri uri = ContentUris.withAppendedId(Schedule.CONTENT_URI, mId);
			getContentResolver().update(uri, values, null, null);
		}

		Alarms.setNextAlert(this);
	}

	private boolean validateAndCreateValues(ContentValues values) {
		if (mTime == null
		// && mSavedLoc == null
		) {
			showToast(getString(R.string.no_time_and_loc_set),
					Toast.LENGTH_LONG);

			return false;
		}

		if (mTime != null
				&& mTime.getTimeInMillis() < Calendar.getInstance()
						.getTimeInMillis() && mDaysOfWeek.getCoded() == 0) {
			showToast(getString(R.string.no_repeat_and_time_in_past),
					Toast.LENGTH_LONG);

			return false;
		}

		if (mProfileId < 0) {
			showToast(getString(R.string.no_profile_select), Toast.LENGTH_LONG);

			return false;
		}

		values.put(Schedule.COLUMN_REPEAT_WEEKDAYS, mDaysOfWeek.getCoded());
		if (mTime != null) {
			values.put(Schedule.COLUMN_START_TIME, mTime.getTimeInMillis());
		} else {
			values.put(Schedule.COLUMN_START_TIME, 0);
		}
		values.put(Schedule.COLUMN_PROFILE_ID, mProfileId);

//		if (mSavedLoc != null) {
//			String sb = LocationUtils.locationToString(mSavedLoc);
//			values.put(Schedule.COLUMN_LOCATION, sb);
//		}

		if (!TextUtils.isEmpty(mLabel)) {
			values.put(Schedule.COLUMN_LABEL, mLabel);
		}

		values.put(Schedule.COLUMN_ENABLE, 1);

		return true;
	}

	@Override
	protected void deleteContent() {
		if (mId < 0)
			return;

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.note)
				.setMessage(R.string.delete_confirm)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Uri uri = ContentUris.withAppendedId(
										Schedule.CONTENT_URI, mId);
								getContentResolver().delete(uri, null, null);

								finish();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean ret = super.onPrepareOptionsMenu(menu);

		if (ret && mId < 0) {
			menu.findItem(1).setEnabled(false);
		}

		return ret;
	}
}
