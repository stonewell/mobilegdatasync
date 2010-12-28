package com.angelstone.android.profileswitcher.provider;

import java.util.ArrayList;
import java.util.List;

import com.angelstone.android.database.GenericContentProvider;
import com.angelstone.android.profileswitcher.store.DatabaseValues;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;

public class ProfileSwitcherProvider extends GenericContentProvider implements
		DatabaseValues {

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());

		List<ContentProviderArg> args = new ArrayList<ContentProviderArg>();

		args.add(new ContentProviderArg(Profile.TABLE_NAME, Profile.CONTENT_TYPE,
				Profile.CONTENT_ITEM_TYPE, Profile.DEFAULT_SORT_ORDER,
				Profile.AUTHORITY, Profile.URI_STRING, Profile.COLUMN_NAME));
		args.add(new ContentProviderArg(Schedule.TABLE_NAME, Schedule.CONTENT_TYPE,
				Schedule.CONTENT_ITEM_TYPE, Schedule.DEFAULT_SORT_ORDER,
				Schedule.AUTHORITY, Schedule.URI_STRING,
				Schedule.COLUMN_REPEAT_WEEKDAYS));
		
		initialize(args);

		return true;
	}

}
