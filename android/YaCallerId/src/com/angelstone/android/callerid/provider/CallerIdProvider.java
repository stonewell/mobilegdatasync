package com.angelstone.android.callerid.provider;

import java.util.ArrayList;
import java.util.List;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.store.CallerId;
import com.angelstone.android.database.Entity;
import com.angelstone.android.database.GenericContentProvider;
import com.angelstone.android.database.GenericDatabaseHelper;

public class CallerIdProvider extends GenericContentProvider implements
		CallerIdConstants {

	@Override
	public boolean onCreate() {

		List<Entity> args = new ArrayList<Entity>();

		args.add(new CallerId());

		mOpenHelper = new GenericDatabaseHelper(getContext(), DATABASE_NAME,
				DATABASE_VERSION, args);
		
		initialize(AUTHORITY, args);

		return true;
	}

}
