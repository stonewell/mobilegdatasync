package com.angelstone.easygsync;

import com.angelstone.easygsync.contacts.ContactsExample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class EasyGSyncMainActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
		ContactsExample.main(new String[0]);
		}catch(Throwable t) {
			Log.e("EasyGSync", "Error", t);
		}
	}
}