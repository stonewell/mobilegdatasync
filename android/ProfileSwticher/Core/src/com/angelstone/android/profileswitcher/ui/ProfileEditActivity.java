package com.angelstone.android.profileswitcher.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;

public class ProfileEditActivity extends EditBaseActivity implements
		OnClickListener {
	private static final int DIALOG_RING_TONE_VIBRATE_VOLUME = 0;

	private static final int CHOOSE_RINGTONE = 0;

	private long mId = -1;
	private Profile mProfile = null;
	private int mCurrentEditingId = -1;
	private Uri mCurrentSelectedRingtoneUri = null;

	private TextView mRingtoneTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.profile_edit);

		Intent intent = getIntent();
		mId = intent.getLongExtra(ProfileSwitcherConstants.EXTRA_ID, -1);

		mProfile = loadProfile(mId);

		EditText et = (EditText) findViewById(R.id.edit_name);
		et.setText(mProfile.getName());

		CheckBox cb = (CheckBox) findViewById(R.id.check_gps);
		cb.setOnClickListener(this);
		cb.setChecked(mProfile.isGpsConfigured());

		ToggleButton tb = (ToggleButton) findViewById(R.id.toggle_gps);
		tb.setChecked(mProfile.isGpsEnable());
		tb.setEnabled(mProfile.isGpsConfigured());

		cb = (CheckBox) findViewById(R.id.check_wifi);
		cb.setOnClickListener(this);
		cb.setChecked(mProfile.isWifiConfigured());

		tb = (ToggleButton) findViewById(R.id.toggle_wifi);
		tb.setChecked(mProfile.isWifiEnable());
		tb.setEnabled(mProfile.isWifiConfigured());

		cb = (CheckBox) findViewById(R.id.check_bluetooth);
		cb.setOnClickListener(this);
		cb.setChecked(mProfile.isBlueToothConfigured());

		tb = (ToggleButton) findViewById(R.id.toggle_bluetooth);
		tb.setChecked(mProfile.isBlueToothEnable());
		tb.setEnabled(mProfile.isBlueToothConfigured());

		cb = (CheckBox) findViewById(R.id.check_phone);
		cb.setOnClickListener(this);
		cb.setChecked(mProfile.isPhoneConfigured());

		ImageButton ib = (ImageButton) findViewById(R.id.img_btn_phone);
		ib.setOnClickListener(this);
		ib.setEnabled(mProfile.isPhoneConfigured());

		cb = (CheckBox) findViewById(R.id.check_notification);
		cb.setOnClickListener(this);
		cb.setChecked(mProfile.isNotificationConfigured());

		ib = (ImageButton) findViewById(R.id.img_btn_notification);
		ib.setOnClickListener(this);
		ib.setEnabled(mProfile.isNotificationConfigured());

		cb = (CheckBox) findViewById(R.id.check_email);
		cb.setOnClickListener(this);
		cb.setChecked(mProfile.isEmailConfigured());

		ib = (ImageButton) findViewById(R.id.img_btn_email);
		ib.setOnClickListener(this);
		ib.setEnabled(mProfile.isEmailConfigured());

		cb = (CheckBox) findViewById(R.id.check_alarm);
		cb.setOnClickListener(this);
		cb.setChecked(mProfile.isAlarmConfigured());

		ib = (ImageButton) findViewById(R.id.img_btn_alarm);
		ib.setOnClickListener(this);
		ib.setEnabled(mProfile.isAlarmConfigured());
	}

	private Profile loadProfile(long id) {
		Profile p = new Profile();

		if (id == -1)
			return p;

		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, id);

		Cursor c = getContentResolver().query(uri, null, null, null, null);

		try {
			if (c.moveToNext()) {
				int idxName = c.getColumnIndex(Profile.COLUMN_NAME);
				int idxFlags = c.getColumnIndex(Profile.COLUMN_FLAGS);

				int idxDevices = c.getColumnIndex(Profile.COLUMN_DEVICES);

				int idxEmailVolume = c
						.getColumnIndex(Profile.COLUMN_EMAIL_VOLUME);
				int idxPhoneVolume = c
						.getColumnIndex(Profile.COLUMN_PHONE_VOLUME);
				int idxNotifyVolume = c
						.getColumnIndex(Profile.COLUMN_NOTIFY_VOLUME);
				int idxAlarmVolume = c
						.getColumnIndex(Profile.COLUMN_ALARM_VOLUME);

				int idxPhoneRingtone = c
						.getColumnIndex(Profile.COLUMN_PHONE_RING_TONE);
				int idxNotifyRingtone = c
						.getColumnIndex(Profile.COLUMN_NOTIFY_RING_TONE);
				int idxAlarmRingtone = c
						.getColumnIndex(Profile.COLUMN_ALARM_RING_TONE);
				int idxEmailRingtone = c
						.getColumnIndex(Profile.COLUMN_EMAIL_RING_TONE);

				p.setName(c.getString(idxName));
				p.setEmailRingtone(c.getString(idxEmailRingtone));
				p.setEmailVolume(c.getInt(idxEmailVolume));
				p.setPhoneRingtone(c.getString(idxPhoneRingtone));
				p.setPhoneVolume(c.getInt(idxPhoneVolume));
				p.setNotificationRingtone(c.getString(idxNotifyRingtone));
				p.setNotificationVolume(c.getInt(idxNotifyVolume));
				p.setAlarmRingtone(c.getString(idxAlarmRingtone));
				p.setAlarmVolume(c.getInt(idxAlarmVolume));

				// Set flags later since set volumn ringtone will change flags
				p.setFlags(c.getInt(idxFlags));
				p.setDevices(c.getInt(idxDevices));
			}
		} finally {
			c.close();
		}

		return p;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_gps: {
			findViewById(R.id.toggle_gps)
					.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_wifi: {
			findViewById(R.id.toggle_wifi).setEnabled(
					((CheckBox) v).isChecked());
		}
			break;

		case R.id.check_bluetooth: {
			findViewById(R.id.toggle_bluetooth).setEnabled(
					((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_phone: {
			findViewById(R.id.img_btn_phone).setEnabled(
					((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_email: {
			findViewById(R.id.img_btn_email).setEnabled(
					((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_alarm: {
			findViewById(R.id.img_btn_alarm).setEnabled(
					((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_notification: {
			findViewById(R.id.img_btn_notification).setEnabled(
					((CheckBox) v).isChecked());
		}
			break;
		case R.id.img_btn_notification:
		case R.id.img_btn_phone:
		case R.id.img_btn_email:
		case R.id.img_btn_alarm: {
			editRingToneAndVolume(v.getId());
		}
			break;

		case R.id.check_volume: {
			View viewParent = (View) v.getParent();
			View vv = viewParent.findViewById(R.id.seekbar_volume);
			vv.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_vibrate: {
			View viewParent = (View) v.getParent();
			View vv = viewParent.findViewById(R.id.toggle_vibrate);
			vv.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_ringtone: {
			View viewParent = (View) v.getParent();
			View vv = viewParent.findViewById(R.id.img_view_show_ringtones);
			vv.setEnabled(((CheckBox) v).isChecked());
			vv = viewParent.findViewById(R.id.btn_ringtone_uri);
			vv.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.img_view_show_ringtones: {
			showRingtones();
		}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean ret = super.onPrepareOptionsMenu(menu);

		if (ret && mId < 0) {
			menu.findItem(1).setEnabled(false);
		}

		return ret;
	}

	@Override
	protected void saveContent() {
		EditText et = (EditText) findViewById(R.id.edit_name);
		String name = et.getText().toString();

		if (TextUtils.isEmpty(name)) {
			showToast(getString(R.string.empty_name_is_not_allowed));
			return;
		}

		if (checkNameAndSave(name))
			doSave();
	}

	private boolean checkNameAndSave(String name) {
		Cursor c = getContentResolver().query(Profile.CONTENT_URI,
				new String[] { Profile.COLUMN_ID }, Profile.COLUMN_NAME + "=?",
				new String[] { name }, null);

		try {
			if (c.moveToNext()) {
				final long id = c.getLong(0);

				if (id != mId) {
					new AlertDialog.Builder(this)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.note)
							.setMessage(R.string.same_name_existing)
							.setPositiveButton(R.string.update_exist,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											mId = id;
											doSave();
										}
									})
							.setNegativeButton(android.R.string.cancel, null)
							.setNeutralButton(R.string.save_current,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											doSave();
										}
									}).show();

					return false;
				}
			}

			return true;
		} finally {
			c.close();
		}
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
										Profile.CONTENT_URI, mId);
								getContentResolver().delete(uri, null, null);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void editRingToneAndVolume(int id) {
		mCurrentEditingId = id;
		showDialog(DIALOG_RING_TONE_VIBRATE_VOLUME);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_RING_TONE_VIBRATE_VOLUME:
			LayoutInflater factory = LayoutInflater.from(this);
			final View ringtoneAndVolumeView = factory.inflate(
					R.layout.ringtone_and_volume, null);

			// Set onClick properties
			CheckBox c = (CheckBox) ringtoneAndVolumeView
					.findViewById(R.id.check_volume);
			c.setOnClickListener(this);
			c = (CheckBox) ringtoneAndVolumeView
					.findViewById(R.id.check_vibrate);
			c.setOnClickListener(this);
			c = (CheckBox) ringtoneAndVolumeView
					.findViewById(R.id.check_ringtone);
			c.setOnClickListener(this);

			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.icon)
					.setTitle(R.string.ringtone_and_volume)
					.setView(ringtoneAndVolumeView)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									updateProfileRingtonVolume(ringtoneAndVolumeView);
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create();
		}
		return null;
	}

	private void updateProfileRingtonVolume(View v) {
		if (mCurrentEditingId == -1)
			return;

		CheckBox cVolume = (CheckBox) v.findViewById(R.id.check_volume);
		CheckBox cVibrate = (CheckBox) v.findViewById(R.id.check_vibrate);
		CheckBox cRingtone = (CheckBox) v.findViewById(R.id.check_ringtone);

		SeekBar vVolume = (SeekBar) v.findViewById(R.id.seekbar_volume);
		ToggleButton vVibrate = (ToggleButton) v
				.findViewById(R.id.toggle_vibrate);

		switch (mCurrentEditingId) {
		case R.id.img_btn_phone:
			mProfile.setPhoneVolumeConfigured(cVolume.isChecked());

			if (cVolume.isChecked())
				mProfile.setPhoneVolume(vVolume.getProgress());

			mProfile.setPhoneVibrateConfigured(cVibrate.isChecked());

			if (cVibrate.isChecked())
				mProfile.setPhoneVibrate(vVibrate.isChecked());

			mProfile.setPhoneRingtoneConfigured(cRingtone.isChecked());

			if (cRingtone.isChecked())
				mProfile.setPhoneRingtone(getSelectedRingtone());
			break;
		case R.id.img_btn_alarm:
			mProfile.setAlarmVolumeConfigured(cVolume.isChecked());
			if (cVolume.isChecked())
				mProfile.setAlarmVolume(vVolume.getProgress());

			mProfile.setAlarmVibrateConfigured(cVibrate.isChecked());
			if (cVibrate.isChecked())
				mProfile.setAlarmVibrate(vVibrate.isChecked());

			mProfile.setAlarmRingtoneConfigured(cRingtone.isChecked());
			if (cRingtone.isChecked())
				mProfile.setAlarmRingtone(getSelectedRingtone());
			break;
		case R.id.img_btn_email:
			mProfile.setEmailVolumeConfigured(cVolume.isChecked());
			if (cVolume.isChecked())
				mProfile.setEmailVolume(vVolume.getProgress());

			mProfile.setEmailVibrateConfigured(cVibrate.isChecked());
			if (cVibrate.isChecked())
				mProfile.setEmailVibrate(vVibrate.isChecked());

			mProfile.setEmailRingtoneConfigured(cRingtone.isChecked());
			if (cRingtone.isChecked())
				mProfile.setEmailRingtone(getSelectedRingtone());
			break;
		case R.id.img_btn_notification:
			mProfile.setNotificationVolumeConfigured(cVolume.isChecked());
			if (cVolume.isChecked())
				mProfile.setNotificationVolume(vVolume.getProgress());

			mProfile.setNotificationVibrateConfigured(cVibrate.isChecked());
			if (cVibrate.isChecked())
				mProfile.setNotificationVibrate(vVibrate.isChecked());

			mProfile.setNotificationRingtoneConfigured(cRingtone.isChecked());
			if (cRingtone.isChecked())
				mProfile.setNotificationRingtone(getSelectedRingtone());
			break;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
		case DIALOG_RING_TONE_VIBRATE_VOLUME:
			if (mCurrentEditingId == -1)
				break;
			CheckBox cVolume = (CheckBox) dialog
					.findViewById(R.id.check_volume);
			CheckBox cVibrate = (CheckBox) dialog
					.findViewById(R.id.check_vibrate);
			CheckBox cRingtone = (CheckBox) dialog
					.findViewById(R.id.check_ringtone);

			SeekBar vVolume = (SeekBar) dialog
					.findViewById(R.id.seekbar_volume);
			ToggleButton vVibrate = (ToggleButton) dialog
					.findViewById(R.id.toggle_vibrate);

			ImageView vRingtone = (ImageView) dialog
					.findViewById(R.id.img_view_show_ringtones);
			Button vRingtoneText = (Button) dialog
					.findViewById(R.id.btn_ringtone_uri);

			mRingtoneTextView = vRingtoneText;

			vRingtone.setOnClickListener(this);

			switch (mCurrentEditingId) {
			case R.id.img_btn_phone:
				cVolume.setChecked(mProfile.isPhoneVolumeConfigured());
				vVolume.setEnabled(mProfile.isPhoneVolumeConfigured());
				vVolume.setProgress(mProfile.getPhoneVolume());

				cVibrate.setChecked(mProfile.isPhoneVibrateConfigured());
				vVibrate.setEnabled(mProfile.isPhoneVibrateConfigured());
				vVibrate.setChecked(mProfile.isPhoneVibrate());

				cRingtone.setChecked(mProfile.isPhoneRingtoneConfigured());
				vRingtone.setEnabled(mProfile.isPhoneRingtoneConfigured());
				vRingtoneText.setEnabled(mProfile.isPhoneRingtoneConfigured());
				selectRingtone(vRingtoneText, mProfile.getPhoneRingtone());
				break;
			case R.id.img_btn_alarm:
				cVolume.setChecked(mProfile.isAlarmVolumeConfigured());
				vVolume.setEnabled(mProfile.isAlarmVolumeConfigured());
				vVolume.setProgress(mProfile.getAlarmVolume());

				cVibrate.setChecked(mProfile.isAlarmVibrateConfigured());
				vVibrate.setEnabled(mProfile.isAlarmVibrateConfigured());
				vVibrate.setChecked(mProfile.isAlarmVibrate());

				cRingtone.setChecked(mProfile.isAlarmRingtoneConfigured());
				vRingtone.setEnabled(mProfile.isAlarmRingtoneConfigured());
				vRingtoneText.setEnabled(mProfile.isAlarmRingtoneConfigured());
				selectRingtone(vRingtoneText, mProfile.getAlarmRingtone());
				break;
			case R.id.img_btn_email:
				cVolume.setChecked(mProfile.isEmailVolumeConfigured());
				vVolume.setEnabled(mProfile.isEmailVolumeConfigured());
				vVolume.setProgress(mProfile.getEmailVolume());

				cVibrate.setChecked(mProfile.isEmailVibrateConfigured());
				vVibrate.setEnabled(mProfile.isEmailVibrateConfigured());
				vVibrate.setChecked(mProfile.isEmailVibrate());

				cRingtone.setChecked(mProfile.isEmailRingtoneConfigured());
				vRingtone.setEnabled(mProfile.isEmailRingtoneConfigured());
				vRingtoneText.setEnabled(mProfile.isEmailRingtoneConfigured());
				selectRingtone(vRingtoneText, mProfile.getEmailRingtone());
				break;
			case R.id.img_btn_notification:
				cVolume.setChecked(mProfile.isNotificationVolumeConfigured());
				vVolume.setEnabled(mProfile.isNotificationVolumeConfigured());
				vVolume.setProgress(mProfile.getNotificationVolume());

				cVibrate.setChecked(mProfile.isNotificationVibrateConfigured());
				vVibrate.setEnabled(mProfile.isNotificationVibrateConfigured());
				vVibrate.setChecked(mProfile.isNotificationVibrate());

				cRingtone.setChecked(mProfile
						.isNotificationRingtoneConfigured());
				vRingtone.setEnabled(mProfile
						.isNotificationRingtoneConfigured());
				vRingtoneText.setEnabled(mProfile
						.isNotificationRingtoneConfigured());
				selectRingtone(vRingtoneText,
						mProfile.getNotificationRingtone());
				break;
			}
			break;
		}
	}

	protected void showRingtones() {
		// Launch the ringtone picker
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		onPrepareRingtonePickerIntent(intent);
		startActivityForResult(intent, CHOOSE_RINGTONE);
	}

	protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {

		ringtonePickerIntent.putExtra(
				RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
				onRestoreRingtone());

		ringtonePickerIntent.putExtra(
				RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);

		ringtonePickerIntent.putExtra(
				RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));

		ringtonePickerIntent.putExtra(
				RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
		ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
				RingtoneManager.TYPE_RINGTONE);
	}

	protected Uri onRestoreRingtone() {
		if (mCurrentSelectedRingtoneUri == null)
			return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

		return mCurrentSelectedRingtoneUri;
	}

	private String getSelectedRingtone() {
		if (mCurrentSelectedRingtoneUri == null)
			return "";

		return mCurrentSelectedRingtoneUri.toString();
	}

	private void selectRingtone(TextView v, String phoneRingtone) {
		v.setText("");

		if (TextUtils.isEmpty(phoneRingtone))
			mCurrentSelectedRingtoneUri = null;
		else {
			try {
				mCurrentSelectedRingtoneUri = Uri.parse(phoneRingtone);
				v.setText(getRingtoneName(mCurrentSelectedRingtoneUri));
			} catch (Throwable t) {
				mCurrentSelectedRingtoneUri = null;
			}
		}
	}

	private String getRingtoneName(Uri currentSelectedRingtoneUri) {
		if (currentSelectedRingtoneUri == null) {
			return getString(R.string.default_ringtone);
		} else {
			Ringtone ringtone = RingtoneManager.getRingtone(this,
					currentSelectedRingtoneUri);
			if (ringtone == null)
				return getString(R.string.default_ringtone);

			return ringtone.getTitle(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case CHOOSE_RINGTONE:
			Uri pickedUri = data
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
				mCurrentSelectedRingtoneUri = null;
			} else {
				mCurrentSelectedRingtoneUri = pickedUri;
			}

			if (mRingtoneTextView != null)
				mRingtoneTextView
						.setText(getRingtoneName(mCurrentSelectedRingtoneUri));
			break;
		default:
			break;
		}
	}

	private void doSave() {
		EditText et = (EditText) findViewById(R.id.edit_name);
		String name = et.getText().toString();
		mProfile.setName(name);

		CheckBox cb = (CheckBox) findViewById(R.id.check_gps);
		ToggleButton tb = (ToggleButton) findViewById(R.id.toggle_gps);
		mProfile.setGpsEnable(tb.isChecked());
		mProfile.setGpsConfigured(cb.isChecked());

		cb = (CheckBox) findViewById(R.id.check_wifi);
		tb = (ToggleButton) findViewById(R.id.toggle_wifi);
		mProfile.setWifiEnable(tb.isChecked());
		mProfile.setWifiConfigured(cb.isChecked());

		cb = (CheckBox) findViewById(R.id.check_bluetooth);
		tb = (ToggleButton) findViewById(R.id.toggle_bluetooth);
		mProfile.setBlueToothEnable(tb.isChecked());
		mProfile.setBlueToothConfigured(cb.isChecked());

		cb = (CheckBox) findViewById(R.id.check_phone);
		mProfile.setPhoneRingtoneConfigured(cb.isChecked());
		mProfile.setPhoneVolumeConfigured(cb.isChecked());
		mProfile.setPhoneVibrateConfigured(cb.isChecked());

		cb = (CheckBox) findViewById(R.id.check_notification);
		mProfile.setNotificationRingtoneConfigured(cb.isChecked());
		mProfile.setNotificationVolumeConfigured(cb.isChecked());
		mProfile.setNotificationVibrateConfigured(cb.isChecked());

		cb = (CheckBox) findViewById(R.id.check_email);
		mProfile.setEmailRingtoneConfigured(cb.isChecked());
		mProfile.setEmailVolumeConfigured(cb.isChecked());
		mProfile.setEmailVibrateConfigured(cb.isChecked());

		cb = (CheckBox) findViewById(R.id.check_alarm);
		mProfile.setAlarmRingtoneConfigured(cb.isChecked());
		mProfile.setAlarmVolumeConfigured(cb.isChecked());
		mProfile.setAlarmVibrateConfigured(cb.isChecked());

		ContentValues values = new ContentValues();
		values.put(Profile.COLUMN_NAME, mProfile.getName());
		values.put(Profile.COLUMN_FLAGS, mProfile.getFlags());
		values.put(Profile.COLUMN_DEVICES, mProfile.getDevices());
		values.put(Profile.COLUMN_EMAIL_VOLUME, mProfile.getEmailVolume());
		values.put(Profile.COLUMN_PHONE_VOLUME, mProfile.getPhoneVolume());
		values.put(Profile.COLUMN_NOTIFY_VOLUME,
				mProfile.getNotificationVolume());
		values.put(Profile.COLUMN_ALARM_VOLUME, mProfile.getAlarmVolume());
		values.put(Profile.COLUMN_PHONE_RING_TONE, mProfile.getPhoneRingtone());
		values.put(Profile.COLUMN_NOTIFY_RING_TONE,
				mProfile.getNotificationRingtone());
		values.put(Profile.COLUMN_ALARM_RING_TONE, mProfile.getAlarmRingtone());
		values.put(Profile.COLUMN_EMAIL_RING_TONE, mProfile.getEmailRingtone());

		if (mId < 0) {
			getContentResolver().insert(Profile.CONTENT_URI, values);
		} else {
			Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, mId);

			getContentResolver().update(uri, values, null, null);
		}
	}
}
