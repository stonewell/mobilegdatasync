package com.angelstone.android.smsblocker.ui;

import android.app.Activity;
import android.os.Bundle;

public class SmsBlockerAboutView extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.setContentView(R.layout.main);

		// try {
		// PackageManager pManager = this.getPackageManager();
		// PackageInfo pInfo;
		// pInfo = pManager.getPackageInfo(getPackageName(),
		// PackageManager.GET_META_DATA);
		// int vcode = pInfo.versionCode;
		// String versionCodeStrFormat =
		// getResources().getString(R.string.about_version_title);
		// String versionCodeStr = String.format(versionCodeStrFormat, vcode);
		// TextView tv1 = (TextView)findViewById(R.id.main_version);
		// tv1.setText(versionCodeStr);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// //printException(e.getLocalizedMessage());
		// }
	}

}
