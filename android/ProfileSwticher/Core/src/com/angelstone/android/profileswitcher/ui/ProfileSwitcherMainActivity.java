package com.angelstone.android.profileswitcher.ui;

import com.angelstone.android.profileswitcher.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class ProfileSwitcherMainActivity extends TabActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("tabProfileConfiguration")
                .setIndicator(getString(R.string.title_profiles),
                		getResources().getDrawable(R.drawable.profiles))
                .setContent(new Intent(this, ProfileConfigurationActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("tabScheduleConfiguration")
                .setIndicator(getString(R.string.title_schedules),
                		getResources().getDrawable(R.drawable.schedule))
                .setContent(new Intent(this, ScheduleConfigurationActivity.class)));
    }

}
