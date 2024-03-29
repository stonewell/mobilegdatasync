package com.angelstone.android.smsblocker.provider;

import java.util.ArrayList;
import java.util.List;

import com.angelstone.android.database.Entity;
import com.angelstone.android.database.GenericContentProvider;
import com.angelstone.android.database.GenericDatabaseHelper;
import com.angelstone.android.phonetools.store.BlackList;
import com.angelstone.android.phonetools.store.EventLog;
import com.angelstone.android.phonetools.store.Setting;
import com.angelstone.android.smsblocker.store.DatabaseValues;

public class SmsBlockerProvider extends GenericContentProvider implements
		DatabaseValues {

	@Override
	public boolean onCreate() {

		List<Entity> args = new ArrayList<Entity>();

		args.add(new BlackList());
		args.add(new EventLog());
		args.add(new Setting());

		mOpenHelper = new GenericDatabaseHelper(getContext(), DATABASE_NAME,
				DATABASE_VERSION, args);
		
		initialize(AUTHORITY, args);

		return true;
	}

}
