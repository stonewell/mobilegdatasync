package com.angelstone.android.profileswitcher.utils;

import android.content.Context;
import android.content.Intent;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.store.Profile;

public class ProfileSwitcherUtils {
	public static void activateProfile(Context context, long id, int active,
			long seconds) {
		Intent intent = new Intent(ProfileSwitcherConstants.ACTION_SET_PROFILE);
		intent.putExtra(Profile.COLUMN_ID, id);
		intent.putExtra(Profile.COLUMN_ACTIVE, active);
		intent.putExtra(Profile.COLUMN_ACTIVATE_TIME,
				System.currentTimeMillis());
		intent.putExtra(Profile.COLUMN_EXPIRE_TIME, seconds);

		context.startService(intent);
	}

	public static float getLocationDistancePreference() {
		// TODO Load location distance from preference
		return 50;
	}

	public static int getLocationUpdateTimePreference() {
		// TODO Auto-generated method stub
		return 10;
	}

	public static float getLocationUpdateDistancePreference() {
		// TODO Auto-generated method stub
		return 500;
	}

	public static void enableProfile(Context context, long profileId) {
		// TODO Auto-generated method stub
		
	}

}
