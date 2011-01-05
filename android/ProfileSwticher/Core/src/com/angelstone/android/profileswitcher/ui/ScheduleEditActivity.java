package com.angelstone.android.profileswitcher.ui;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.DaysOfWeek;
import com.angelstone.android.utils.HandlerThreadQuiter;
import com.angelstone.android.utils.PhoneToolsUtil;

public class ScheduleEditActivity extends EditBaseActivity implements
		OnClickListener {
	private static final int TIME_DIALOG_ID = 1001;
	private static final int PROFILE_CHOOSER_DIALOG_ID = 1002;
	private static final int REPEAT_CHOOSER_DIALOG_ID = 1003;
	private static final int LOCATION_CHOOSER_DIALOG_ID = 1004;
	private static final int LABEL_CHOOSER_DIALOG_ID = 1005;
	private static final String DATA_ID = "_data_id";
	private static final String DATA_TIME = "_data_time";
	private static final String DATA_SAVED_LOC = "_data_saved_loc";
	private static final String DATA_PROFILE_ID = "_data_profile_id";
	private static final String DATA_PROFILE_POS = "_data_profile_pos";
	private static final String DATA_DAYS_OF_WEEK = "_data_days_of_week";
	private static final String DATA_LABEL = "_data_label";
	private static final String DATA_SAVED_LOC_DESC = "_data_saved_loc_desc";

	private TextView mTextTimeDesc;
	private TextView mTextProfileDesc;
	private TextView mTextRepeatDesc;
	private TextView mTextLocationDesc;
	private TextView mTextLabel;

	private long mId;

	private Calendar mTime = null;
	private long mProfileId = -1;
	private int mProfilePosition = -1;

	private ProfileAdapter mProfileAdapter;
	private Cursor mProfileCursor;

	private DateFormat mTimeFormat;

	private int mIndexProfileId;
	private int mIndexProfileName;

	private DaysOfWeek mDaysOfWeek;
	private String[] mWeekDays;

	private LocationManager mLocationManager;
	private Location mCurrentLoc;
	private Location mSavedLoc;
	private String mSavedLocDesc;
	private long mSavedLocUpdateTime;

	private boolean mLocDlgShown;
	private boolean mUseCurrentLoc;
	private EditText mEditLatitude;
	private EditText mEditLongitude;
	private EditText mEditAltitude;
	private CheckBox mChkUseAltitude;

	private String mLabel = null;
	private EditText mEditLabel;

	private Handler mBackgroundHandler = null;
	private HandlerThread mBackgroundHandlerThread = null;

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
			mProfilePosition = which;

			updateView();
		}
	};

	private final DialogInterface.OnMultiChoiceClickListener mRepeatListener = new DialogInterface.OnMultiChoiceClickListener() {
		public void onClick(DialogInterface dialog, int which, boolean check) {
			mDaysOfWeek.set(which, check);
		}
	};

	private final LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			if (PhoneToolsUtil.isBetterLocation(location, mCurrentLoc)) {
				Location oldLoc = mCurrentLoc;
				mCurrentLoc = location;

				if (mLocDlgShown && mUseCurrentLoc
						&& !isSameLocationAsUI(oldLoc)) {
					updateLocationUI(mCurrentLoc);
				}
			}
		}
	};

	private final DialogInterface.OnClickListener mLocationSetListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {

			updateSavedLoc(getLocationFromUI());

			updateView();
		}
	};

	private final class UpdateLocDesRunnable implements Runnable {
		private long mCreateTime;
		private int mLoopCount;
		private static final int RETRY_TIMES = 5;

		public UpdateLocDesRunnable(long createTime) {
			mCreateTime = createTime;
			mLoopCount = 0;
		}

		@Override
		public void run() {
			if (mSavedLocUpdateTime > mCreateTime || mSavedLoc == null
					|| mLoopCount > RETRY_TIMES)
				return;

			try {
				Geocoder gc = new Geocoder(ScheduleEditActivity.this);

				List<Address> results = null;

				results = gc.getFromLocation(mSavedLoc.getLatitude(),
						mSavedLoc.getLongitude(), 1);

				if (results != null && results.size() > 0) {
					Address address = results.get(0);
					StringBuilder sb1 = new StringBuilder();

					for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
						sb1.append(address.getAddressLine(i)).append("");
					}

					mSavedLocDesc = sb1.toString();

					ScheduleEditActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mTextLocationDesc.setText(mSavedLocDesc);
						}
					});
				} else if (mLoopCount <= RETRY_TIMES) {
					mLoopCount++;
					mBackgroundHandler.postDelayed(this, 1000);
				}
			} catch (Throwable t) {
				ActivityLog.logWarning(ScheduleEditActivity.this,
						ProfileSwitcherConstants.TAG,
						"Update Geo Location Fail:" + t.getLocalizedMessage());
			}
		}
	};

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
		mTextLocationDesc = (TextView) findViewById(R.id.location_desc);
		mTextLabel = (TextView) findViewById(R.id.schedule_label);

		int[] tr_ids = new int[] { R.id.tr_location, R.id.tr_profile,
				R.id.tr_repeat, R.id.tr_schedule_label, R.id.tr_time };

		for (int id : tr_ids) {
			TableRow tmp = (TableRow) findViewById(id);
			tmp.setOnClickListener(this);
		}

		int[] img_btn_ids = new int[] { R.id.img_btn_location,
				R.id.img_btn_profile, R.id.img_btn_repeat,
				R.id.img_btn_schedule_label };

		for (int id : img_btn_ids) {
			ImageButton tmp = (ImageButton) findViewById(id);
			tmp.setOnClickListener(this);
		}

		mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);

		mProfileCursor = managedQuery(Profile.CONTENT_URI, null, null, null,
				null);
		mProfileAdapter = new ProfileAdapter(this, mProfileCursor, true);
		mIndexProfileId = mProfileCursor.getColumnIndex(Profile.COLUMN_ID);
		mIndexProfileName = mProfileCursor.getColumnIndex(Profile.COLUMN_NAME);

		startBackgroundHandler();

		updateData();
		updateView();
	}

	private void updateView() {
		if (mTime != null)
			mTextTimeDesc.setText(mTimeFormat.format(mTime.getTime()));
		else
			mTextTimeDesc.setText("");

		if (mProfileId != -1 && mProfilePosition != -1) {
			mProfileCursor.moveToPosition(mProfilePosition);
			mTextProfileDesc.setText(mProfileCursor
					.getString(mIndexProfileName));
		}

		mTextRepeatDesc.setText(mDaysOfWeek.toString(this, true));

		mTextLocationDesc.setText(getSavedLocDesc());

		mTextLabel.setText(mLabel == null ? "" : mLabel);
	}

	private String getSavedLocDesc() {
		if (!TextUtils.isEmpty(mSavedLocDesc)) {
			return mSavedLocDesc;
		}

		if (mSavedLoc == null)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append(mSavedLoc.getLongitude() > 0 ? getString(R.string.east_longitude)
				: getString(R.string.west_longitude));
		sb.append(Location.convert(mSavedLoc.getLongitude(),
				Location.FORMAT_DEGREES));
		sb.append(",");
		sb.append(mSavedLoc.getLatitude() > 0 ? getString(R.string.north_latitude)
				: getString(R.string.south_latitude));
		sb.append(Location.convert(mSavedLoc.getLatitude(),
				Location.FORMAT_DEGREES));

		if (mSavedLoc.hasAltitude()) {
			sb.append(",");
			sb.append(Location.convert(mSavedLoc.getAltitude(),
					Location.FORMAT_DEGREES));
		}

		return sb.toString();
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
		case R.id.tr_location:
		case R.id.img_btn_location: {
			if (!hasLocProviders()) {
				new AlertDialog.Builder(this).setTitle(R.string.label_location)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setNegativeButton(android.R.string.cancel, null)
						.setMessage(R.string.warn_no_loc_providers).show();
			} else {
				showDialog(LOCATION_CHOOSER_DIALOG_ID);
			}
		}
			break;
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
		case R.id.btn_use_current: {
			mUseCurrentLoc = true;
			updateLocationUI(mCurrentLoc);
		}
			break;
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
					.setSingleChoiceItems(mProfileAdapter, mProfilePosition,
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
		case LOCATION_CHOOSER_DIALOG_ID: {
			LayoutInflater factory = LayoutInflater.from(this);
			final View v = factory.inflate(R.layout.location_edit, null);

			final CheckBox chkUseAltitude = (CheckBox) v
					.findViewById(R.id.chk_use_altitude);
			final Button btnUseCurrentLoc = (Button) v
					.findViewById(R.id.btn_use_current);

			chkUseAltitude.setOnClickListener(this);
			btnUseCurrentLoc.setOnClickListener(this);

			return new AlertDialog.Builder(this)
					.setTitle(R.string.label_location)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							mLocationSetListener).setView(v).create();
		}
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
		case LOCATION_CHOOSER_DIALOG_ID: {
			mLocDlgShown = true;

			mChkUseAltitude = (CheckBox) dialog
					.findViewById(R.id.chk_use_altitude);
			mEditLatitude = (EditText) dialog.findViewById(R.id.latitude);
			mEditLongitude = (EditText) dialog.findViewById(R.id.longitude);
			mEditAltitude = (EditText) dialog.findViewById(R.id.altitude);
			final View rowAltitude = dialog.findViewById(R.id.row_altitude);

			mChkUseAltitude.setChecked(false);
			rowAltitude.setVisibility(View.GONE);

			mLocDlgShown = true;

			if (mSavedLoc != null) {
				mUseCurrentLoc = false;
				updateLocationUI(mSavedLoc);
			} else {
				mUseCurrentLoc = true;
				updateLocationUI(mCurrentLoc);
			}
		}
			break;
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

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mCurrentLoc = mLocationManager.getLastKnownLocation(getLocProvider());
		mLocationManager.requestLocationUpdates(getLocProvider(), 0, 0,
				mLocationListener);

		updateSavedLoc(null);

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
			updateProfilePosition();

			String loc = c
					.getString(c.getColumnIndex(Schedule.COLUMN_LOCATION));

			if (!TextUtils.isEmpty(loc)) {
				String[] parts = loc.split(",");

				if (parts.length >= 2) {
					double longitude = Location.convert(parts[0]);
					double latitude = Location.convert(parts[1]);

					Location location = new Location(
							LocationManager.NETWORK_PROVIDER);
					location.setLatitude(latitude);
					location.setLongitude(longitude);

					if (parts.length > 2) {
						location.setAltitude(Location.convert(parts[2]));
					} else {
						location.removeAltitude();
					}

					updateSavedLoc(location);
				}
			}

		} finally {
			c.close();
		}
	}

	private void updateProfilePosition() {
		if (mProfileId < 0) {
			mProfilePosition = -1;
			return;
		}

		if (mProfileCursor.moveToFirst()) {
			do {

				if (mProfileCursor.getLong(mIndexProfileId) == mProfileId) {
					mProfilePosition = mProfileCursor.getPosition();
					return;
				}
			} while (mProfileCursor.moveToNext());
		}

		mProfileId = -1;
	}

	private String getLocProvider() {
		List<String> providers = mLocationManager.getProviders(true);

		if (providers != null && providers.size() > 0) {
			for (String p : providers) {
				if (LocationManager.NETWORK_PROVIDER.equals(p))
					return LocationManager.NETWORK_PROVIDER;
			}

			return providers.get(0);
		}

		return LocationManager.NETWORK_PROVIDER;
	}

	private boolean hasLocProviders() {
		List<String> providers = mLocationManager.getProviders(true);

		return (providers != null && providers.size() > 0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mLocationManager.removeUpdates(mLocationListener);

		stopBackgroundHandler();
	}

	private void stopBackgroundHandler() {
		if (mBackgroundHandlerThread == null)
			return;

		try {
			HandlerThreadQuiter.quit(mBackgroundHandlerThread);
		} catch (VerifyError ex) {
			mBackgroundHandlerThread.getLooper().quit();
		}

		mBackgroundHandlerThread = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(mLocationListener);

		stopBackgroundHandler();
	}

	@Override
	protected void onResume() {
		super.onResume();

		startBackgroundHandler();

		mLocationManager.requestLocationUpdates(getLocProvider(), 0, 0,
				mLocationListener);

		updateSavedLoc(mSavedLoc, mSavedLocDesc);
	}

	private void startBackgroundHandler() {
		if (mBackgroundHandlerThread == null
				|| !mBackgroundHandlerThread.isAlive()) {
			mBackgroundHandlerThread = new HandlerThread(
					"ScheduleEditActivity_Update_Thread",
					android.os.Process.THREAD_PRIORITY_BACKGROUND);
		}

		if (!mBackgroundHandlerThread.isAlive()) {
			mBackgroundHandlerThread.start();
		}

		mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
	}

	protected boolean isSameLocationAsUI(Location oldLoc) {
		if (oldLoc == null)
			return false;

		return oldLoc.equals(getLocationFromUI());
	}

	protected void updateLocationUI(final Location loc) {
		if (loc == null)
			return;

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mEditAltitude.setText(String.valueOf(loc.getAltitude()));
				mEditLatitude.setText(String.valueOf(loc.getLatitude()));
				mEditLongitude.setText(String.valueOf(loc.getLongitude()));
			}
		});
	}

	protected Location getLocationFromUI() {
		double altitude = 0;
		double longitude = 0;
		double latitude = 0;

		String strLongitude = mEditLongitude.getText().toString();
		String strLatitude = mEditLatitude.getText().toString();

		if (TextUtils.isEmpty(strLatitude) || TextUtils.isEmpty(strLongitude)) {
			showToast(MessageFormat.format(
					getString(R.string.invalid_location_template),
					new Object[] { getString(R.string.label_latitude),
							getString(R.string.label_longitude) }));
			return null;
		}

		String strAltitude = null;

		if (mChkUseAltitude.isChecked()) {
			strAltitude = mEditAltitude.getText().toString();

			if (TextUtils.isEmpty(strAltitude)) {
				showToast(MessageFormat.format(
						getString(R.string.invalid_location_template_2),
						new Object[] { getString(R.string.label_latitude),
								getString(R.string.label_longitude),
								getString(R.string.label_altitude) }));
				return null;
			}
		}

		try {
			longitude = Double.parseDouble(strLongitude);
			latitude = Double.parseDouble(strLatitude);

			if (!TextUtils.isEmpty(strAltitude))
				altitude = Double.parseDouble(strAltitude);
		} catch (Throwable t) {
			if (mChkUseAltitude.isChecked()) {
				showToast(MessageFormat.format(
						getString(R.string.invalid_location_template_2),
						new Object[] { getString(R.string.label_latitude),
								getString(R.string.label_longitude),
								getString(R.string.label_altitude) }));
			} else {
				showToast(MessageFormat.format(
						getString(R.string.invalid_location_template),
						new Object[] { getString(R.string.label_latitude),
								getString(R.string.label_longitude) }));
			}
			return null;
		}

		Location loc = new Location(mCurrentLoc);
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);

		if (mChkUseAltitude.isChecked()) {
			loc.setAltitude(altitude);
		} else {
			loc.removeAltitude();
		}

		return loc;
	}

	protected void updateSavedLoc(Location location) {
		updateSavedLoc(location, null);
	}

	protected void updateSavedLoc(Location location, String defaultDesc) {
		mSavedLoc = location;

		mLocDlgShown = false;
		mUseCurrentLoc = false;
		mSavedLocDesc = defaultDesc;
		mSavedLocUpdateTime = System.currentTimeMillis();

		mBackgroundHandler.postAtFrontOfQueue(new UpdateLocDesRunnable(System
				.currentTimeMillis() + 1));
	}

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
			mProfilePosition = savedInstanceState.getInt(DATA_PROFILE_POS,
					mProfilePosition);

			int code = savedInstanceState.getInt(DATA_DAYS_OF_WEEK, -1);

			if (code >= 0) {
				mDaysOfWeek.setCoded(code);
			}

			String label = savedInstanceState.getString(DATA_LABEL);

			if (!TextUtils.isEmpty(label))
				mLabel = label;

			Location loc = savedInstanceState.getParcelable(DATA_SAVED_LOC);
			if (loc != null)
				mSavedLoc = loc;

			label = savedInstanceState.getString(DATA_SAVED_LOC_DESC);
			if (!TextUtils.isEmpty(label))
				mSavedLocDesc = label;

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
		outState.putInt(DATA_PROFILE_POS, mProfilePosition);

		outState.putInt(DATA_DAYS_OF_WEEK, mDaysOfWeek.getCoded());

		if (mSavedLoc != null)
			outState.putParcelable(DATA_SAVED_LOC, mSavedLoc);

		if (!TextUtils.isEmpty(mLabel))
			outState.putString(DATA_LABEL, mLabel);

		if (!TextUtils.isEmpty(mSavedLocDesc)) {
			outState.putString(DATA_SAVED_LOC_DESC, mSavedLocDesc);
		}
	}

	@Override
	protected void saveContent() {
		ContentValues values = new ContentValues();

		if (!validateAndCreateValues(values)) {
			return;
		}

		if (mId < 0) {
			getContentResolver().insert(Schedule.CONTENT_URI, values);
		} else {
			Uri uri = ContentUris.withAppendedId(Schedule.CONTENT_URI, mId);
			getContentResolver().update(uri, values, null, null);
		}
	}

	private boolean validateAndCreateValues(ContentValues values) {
		if (mTime == null && mSavedLoc == null) {
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

		if (mSavedLoc != null) {
			StringBuilder sb = new StringBuilder(1024);
			sb.append(String.valueOf(mSavedLoc.getLongitude())).append(",");
			sb.append(String.valueOf(mSavedLoc.getLatitude()));

			if (mSavedLoc.hasAltitude())
				sb.append(",").append(String.valueOf(mSavedLoc.getAltitude()));
			values.put(Schedule.COLUMN_LOCATION, sb.toString());
		}

		if (!TextUtils.isEmpty(mLabel)) {
			values.put(Schedule.COLUMN_LABEL, mLabel);
		}

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

}
