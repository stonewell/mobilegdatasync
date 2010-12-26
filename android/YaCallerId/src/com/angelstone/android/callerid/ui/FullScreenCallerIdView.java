package com.angelstone.android.callerid.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.utils.PhotoLoader;

public class FullScreenCallerIdView extends Activity {

	private PhotoLoader mPhotoLoader = null;
	private boolean mHide = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

		flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

		getWindow().addFlags(flags);

		setContentView(R.layout.fullscreen_caller_id);

		mHide = false;
		mPhotoLoader = new PhotoLoader(this, R.drawable.ic_contact_list_picture);

		Intent intent = getIntent();

		long id = intent.getLongExtra(CallerIdConstants.DATA_ID, -1);

		if (id < 0)
			id = 0;

		ImageView iv = (ImageView) findViewById(R.id.caller_img);

		mPhotoLoader.loadPhoto(iv, id);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mPhotoLoader.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mPhotoLoader.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!mHide) {
			Intent intent = new Intent(getApplicationContext(),
					FullScreenCallerIdView.class);

			intent.putExtra(CallerIdConstants.DATA_PAUSE_Call, true);
			startActivity(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (mHide) {
			finish();
			
			return;
		}
		
		boolean hide = intent.getBooleanExtra(CallerIdConstants.DATA_HIDE, false);
		
		if (hide) {
			mHide = true;
			finish();
		}
		else {
			if (intent.getBooleanExtra(CallerIdConstants.DATA_PAUSE_Call, false)) {
				return;
			}
			
			long id = intent.getLongExtra(CallerIdConstants.DATA_ID, -1);

			if (id < 0)
				finish();
			else {
				ImageView iv = (ImageView) findViewById(R.id.caller_img);

				mPhotoLoader.loadPhoto(iv, id);
			}
		}
	}

}
