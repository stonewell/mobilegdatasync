package com.angelstone.android.profileswitcher.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;

public class ProfileEditActivity extends EditBaseActivity implements
		OnClickListener {
	private static final int DIALOG_RING_TONE_VIBRATE_VOLUME = 0;
	
	private int mId = -1;
	private Profile mProfile = null;
	private int mCurrentEditingId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.profile_edit);

		Intent intent = getIntent();
		mId = intent.getIntExtra("id", -1);

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

	private Profile loadProfile(int id) {
		Profile p = new Profile();

		if (id == -1)
			return p;

		// TODO: Load Profile from db
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
			View viewParent = (View)v.getParent();
			View vv = viewParent.findViewById(R.id.seekbar_volume);
			vv.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_vibrate: {
			View viewParent = (View)v.getParent();
			View vv = viewParent.findViewById(R.id.toggle_vibrate);
			vv.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_ringtone: {
			View viewParent = (View)v.getParent();
			View vv = viewParent.findViewById(R.id.spinner_ringtone);
			vv.setEnabled(((CheckBox) v).isChecked());
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
		// TODO Auto-generated method stub
	}

	@Override
	protected void deleteContent() {
		// TODO Auto-generated method stub

	}

	private void editRingToneAndVolume(int id) {
		mCurrentEditingId = id;
		showDialog(DIALOG_RING_TONE_VIBRATE_VOLUME);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_RING_TONE_VIBRATE_VOLUME:
			// This example shows how to add a custom layout to an AlertDialog
			LayoutInflater factory = LayoutInflater.from(this);
			final View ringtoneAndVolumeView = factory.inflate(
					R.layout.ringtone_and_volume, null);

			//Set onClick properties
			CheckBox c = (CheckBox)ringtoneAndVolumeView.findViewById(R.id.check_volume);
			c.setOnClickListener(this);
			c = (CheckBox)ringtoneAndVolumeView.findViewById(R.id.check_vibrate);
			c.setOnClickListener(this);
			c = (CheckBox)ringtoneAndVolumeView.findViewById(R.id.check_ringtone);
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
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}
		return null;
	}

	private void updateProfileRingtonVolume(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		switch(id) {
		case DIALOG_RING_TONE_VIBRATE_VOLUME:
			if (mCurrentEditingId == -1)
				break;
			
			break;
		}
	}

}
