package com.angelstone.android.profileswitcher.ui;

import android.content.Intent;
import android.os.Bundle;
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
	private int mId = -1;
	private Profile mProfile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.profile_edit);

		Intent intent = getIntent();
		mId = intent.getIntExtra("id", -1);

		mProfile = loadProfile(mId);

		EditText et = (EditText)findViewById(R.id.edit_name);
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

		return p;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_gps: {
			findViewById(R.id.toggle_gps).setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_wifi: {
			findViewById(R.id.toggle_wifi).setEnabled(((CheckBox) v).isChecked());
		}
			break;

		case R.id.check_bluetooth: {
			findViewById(R.id.toggle_bluetooth)
					.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_phone: {
			findViewById(R.id.img_btn_phone)
					.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_email: {
			findViewById(R.id.img_btn_email)
					.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_alarm: {
			findViewById(R.id.img_btn_alarm)
					.setEnabled(((CheckBox) v).isChecked());
		}
			break;
		case R.id.check_notification: {
			findViewById(R.id.img_btn_notification)
					.setEnabled(((CheckBox) v).isChecked());
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

}
