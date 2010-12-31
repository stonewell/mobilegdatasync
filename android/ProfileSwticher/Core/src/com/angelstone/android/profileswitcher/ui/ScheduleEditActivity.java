package com.angelstone.android.profileswitcher.ui;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.utils.DaysOfWeek;

public class ScheduleEditActivity extends EditBaseActivity implements
		OnClickListener {
	private static final int TIME_DIALOG_ID = 1001;
	private static final int PROFILE_CHOOSER_DIALOG_ID = 1002;
	private static final int REPEAT_CHOOSER_DIALOG_ID = 1003;

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
				R.id.img_btn_profile, R.id.img_btn_repeat, R.id.img_btn_schedule_label };

		for (int id : img_btn_ids) {
			ImageButton tmp = (ImageButton) findViewById(id);
			tmp.setOnClickListener(this);
		}

		mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);

		mProfileCursor = managedQuery(Profile.CONTENT_URI, null, null, null, null);
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
	}

	private void updateData() {
		String[] weekdays = new DateFormatSymbols().getWeekdays();
		mWeekDays = new String[] { weekdays[Calendar.MONDAY],
				weekdays[Calendar.TUESDAY], weekdays[Calendar.WEDNESDAY],
				weekdays[Calendar.THURSDAY], weekdays[Calendar.FRIDAY],
				weekdays[Calendar.SATURDAY], weekdays[Calendar.SUNDAY], };

		mDaysOfWeek = new DaysOfWeek(0);
		
		//TODO load mRepeat;
	}

	@Override
	protected void saveContent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void deleteContent() {
		// TODO Auto-generated method stub

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
			break;
		case R.id.tr_schedule_label:
		case R.id.img_btn_schedule_label:
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
					time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), true);
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
								public void onClick(DialogInterface dialog, int which) {
									updateView();
								}
							}).setMultiChoiceItems(mWeekDays, mDaysOfWeek.getBooleanArray(), mRepeatListener)
					.create();
		}
		default:
			break;
		}
		return null;
	}
}
