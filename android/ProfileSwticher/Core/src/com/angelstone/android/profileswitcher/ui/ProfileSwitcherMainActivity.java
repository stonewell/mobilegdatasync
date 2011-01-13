package com.angelstone.android.profileswitcher.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.service.ProfileSwitcherService;

public class ProfileSwitcherMainActivity extends TabActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost
				.newTabSpec("tabProfileConfiguration")
				.setIndicator(getString(R.string.title_profiles),
						getResources().getDrawable(R.drawable.profiles))
				.setContent(new Intent(this, ProfilesListActivity.class)));

		tabHost.addTab(tabHost
				.newTabSpec("tabScheduleConfiguration")
				.setIndicator(getString(R.string.title_schedules),
						getResources().getDrawable(R.drawable.schedule))
				.setContent(new Intent(this, SchedulesListActivity.class)));
		Intent intent = getIntent();

		if (intent != null
				&& intent.getBooleanExtra(ProfileSwitcherConstants.DATA_NOTIFY,
						false)) {
			intent.setClass(getApplicationContext(),
					ProfileSwitcherService.class);

			startService(intent);
		}
	}

}
