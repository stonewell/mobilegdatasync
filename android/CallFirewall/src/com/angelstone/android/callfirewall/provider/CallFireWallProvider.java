package com.angelstone.android.callfirewall.provider;

import java.util.ArrayList;
import java.util.List;

import com.angelstone.android.callfirewall.CallFireWallConstants;
import com.angelstone.android.callfirewall.store.CallFireWallSetting;
import com.angelstone.android.database.Entity;
import com.angelstone.android.database.GenericContentProvider;
import com.angelstone.android.database.GenericDatabaseHelper;
import com.angelstone.android.phonetools.store.BlackList;
import com.angelstone.android.phonetools.store.EventLog;

public class CallFireWallProvider extends GenericContentProvider implements
		CallFireWallConstants {

	@Override
	public boolean onCreate() {

		List<Entity> args = new ArrayList<Entity>();

		args.add(new BlackList());
		args.add(new EventLog());
		args.add(new CallFireWallSetting());

		mOpenHelper = new GenericDatabaseHelper(getContext(), DATABASE_NAME,
				DATABASE_VERSION, args);
		
		initialize(AUTHORITY, args);

		return true;
	}

}
