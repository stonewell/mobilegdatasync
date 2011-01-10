package com.angelstone.android.profileswitcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.service.ProfileSwitcherService;
import com.angelstone.android.utils.ActivityLog;

public class ProfileSwitcherReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			intent.setClass(context, ProfileSwitcherService.class);

			context.startService(intent);
		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "receiver fail", t);
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					t.getLocalizedMessage());
		}
	}
}
