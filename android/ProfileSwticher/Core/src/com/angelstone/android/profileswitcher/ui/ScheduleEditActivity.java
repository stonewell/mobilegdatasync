package com.angelstone.android.profileswitcher.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.utils.DaysOfWeek;
import com.angelstone.android.utils.PhoneToolsUtil;

public class ScheduleEditActivity extends EditBaseActivity implements
		OnClickListener {
	private static final int TIME_DIALOG_ID = 1001;
	private static final int PROFILE_CHOOSER_DIALOG_ID = 1002;
	private static final int REPEAT_CHOOSER_DIALOG_ID = 1003;
	private static final int LOCATION_CHOOSER_DIALOG_ID = 1004;

	private long mId;
	private TextView mTimeDesc;
	private TextView mProfileDesc;
	private TextView mRepeatDesc;
	private TextView mLocationDesc;

	private TextView mLabel;

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

	private Location mCurrentLoc;
	private boolean mLocDlgShown;
	private boolean mUseCurrentLoc;
	private Location mSavedLoc;
	private LocationManager mLocationManager;
	private EditText mEditLatitude;
	private EditText mEditLongitude;
	private EditText mEditAltitude;
	private CheckBox mChkUseAltitude;

	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (mTime == null)
				mTime = Calendar.getInstance();

			mTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mTime.set(Calendar.MINUTE, minute);

			updateView();
		}
	};

	private DialogInterface.OnClickListener mProfileListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();

			Cursor c = (Cursor) mProfileAdapter.getItem(which);

			mProfileId = c.getLong(mIndexProfileId);
			mProfilePosition = which;

			updateView();
		}
	};

	private DialogInterface.OnMultiChoiceClickListener mRepeatListener = new DialogInterface.OnMultiChoiceClickListener() {
		public void onClick(DialogInterface dialog, int which, boolean check) {
			mDaysOfWeek.set(which, check);
		}
	};

	private LocationListener mLocationListener = new LocationListener() {

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

	private DialogInterface.OnClickListener mLocationSetListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			mSavedLoc = getLocationFromUI();

			mLocDlgShown = false;
			mUseCurrentLoc = false;

			updateView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.schedule_edit);

		Intent intent = getIntent();
		mId = intent.getLongExtra(ProfileSwitcherConstants.EXTRA_ID, -1);

		mTimeDesc = (TextView) findViewById(R.id.time_desc);
		mProfileDesc = (TextView) findViewById(R.id.profile_desc);
		mRepeatDesc = (TextView) findViewById(R.id.repeat_desc);
		mLocationDesc = (TextView) findViewById(R.id.location_desc);
		mLabel = (TextView) findViewById(R.id.schedule_label);

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

		updateData();
		updateView();
	}

	private void updateView() {
		if (mTime != null)
			mTimeDesc.setText(mTimeFormat.format(mTime.getTime()));
		else
			mTimeDesc.setText("");

		if (mProfileId != -1 && mProfilePosition != -1) {
			mProfileCursor.moveToPosition(mProfilePosition);
			mProfileDesc.setText(mProfileCursor.getString(mIndexProfileName));
		}

		mRepeatDesc.setText(mDaysOfWeek.toString(this, true));

		if (mSavedLoc != null) {
			StringBuilder sb = new StringBuilder();

			Geocoder gc = new Geocoder(this);
			List<Address> results = null;

			try {
				results = gc.getFromLocation(mSavedLoc.getLatitude(),
						mSavedLoc.getLongitude(), 1);
			} catch (IOException e) {
			}

			if (results != null && results.size() > 0) {
				Address address = results.get(0);

				for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
					sb.append(address.getAddressLine(i)).append("");
				}
			} else {
				sb.append(mSavedLoc.getLongitude() > 0 ?
						getString(R.string.east_longitude) :
							getString(R.string.west_longitude));
				sb.append(Location.convert(mSavedLoc.getLongitude(),
						Location.FORMAT_DEGREES));
				sb.append(",");
				sb.append(mSavedLoc.getLatitude() > 0 ?
						getString(R.string.north_latitude) :
							getString(R.string.south_latitude));
				sb.append(Location.convert(mSavedLoc.getLatitude(),
						Location.FORMAT_DEGREES));

				if (mSavedLoc.hasAltitude()) {
					sb.append(",");
					sb.append(Location.convert(mSavedLoc.getAltitude(),
							Location.FORMAT_DEGREES));
				}
			}

			mLocationDesc.setText(sb.toString());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tr_time:
			showDialog(TIME_DIALOG_ID);
			break;
		case R.id.tr_profile:
		case R.id.img_btn_profile:
			showDialog(PROFILE_CHOOSER_DIALOG_ID);
			break;
		case R.id.tr_repeat:
		case R.id.img_btn_repeat:
			showDialog(REPEAT_CHOOSER_DIALOG_ID);
			break;
		case R.id.tr_location:
		case R.id.img_btn_location:
			showDialog(LOCATION_CHOOSER_DIALOG_ID);
			break;
		case R.id.tr_schedule_label:
		case R.id.img_btn_schedule_label:
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
		mLocDlgShown = false;
		mUseCurrentLoc = false;
		mLocationManager.requestLocationUpdates(getLocProvider(), 0, 0,
				mLocationListener);

		// TODO load mRepeat;
	}

	private String getLocProvider() {
		return LocationManager.NETWORK_PROVIDER;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLocationManager.removeUpdates(mLocationListener);
	}

	protected boolean isSameLocationAsUI(Location oldLoc) {
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

	@Override
	protected void saveContent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void deleteContent() {
		// TODO Auto-generated method stub

	}

}
