package com.angelstone.android.callerid.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.utils.PhotoLoader;
import com.angelstone.android.utils.ActivityLog;

public class FullScreenCallerIdView extends Activity implements OnClickListener {

	private PhotoLoader mPhotoLoader = null;
	private boolean mHide = false;
	private ITelephony mTelephony = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

		flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

		getWindow().addFlags(flags);

		setContentView(R.layout.fullscreen_caller_id);

		mTelephony = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));

		mHide = false;
		mPhotoLoader = new PhotoLoader(this, R.drawable.ic_contact_list_picture);

		Button btn = (Button) findViewById(R.id.answerButton);
		btn.setOnClickListener(this);

		btn = (Button) findViewById(R.id.endButton);
		btn.setOnClickListener(this);

		Intent intent = getIntent();

		long id = intent.getLongExtra(CallerIdConstants.DATA_ID, -1);

		if (id < 0)
			id = 0;

		updateView(intent, id);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mPhotoLoader.stop();

		try {
			mTelephony.showCallScreen();
		} catch (RemoteException e) {
			ActivityLog.logError(this, getString(R.string.app_name),
					e.getLocalizedMessage());
			Log.e(CallerIdConstants.TAG, "Fail when show call screen", e);
		}
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
		} else {
			if (intent.getBooleanExtra(CallerIdConstants.DATA_PAUSE_Call, false)) {
				return;
			}

			long id = intent.getLongExtra(CallerIdConstants.DATA_ID, -1);

			if (id < 0)
				finish();
			else {
				updateView(intent, id);
			}
		}
	}

	private void updateView(Intent intent, long id) {
		ImageView iv = (ImageView) findViewById(R.id.caller_img);

		mPhotoLoader.loadPhoto(iv, id);

		TextView tv = (TextView) findViewById(R.id.incoming_number);

		String number = intent
				.getStringExtra(CallerIdConstants.DATA_INCOMING_NUMBER);

		if (TextUtils.isEmpty(number))
			number = getString(R.string.unknown);
		tv.setText(number);
	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.answerButton:
				mTelephony.answerRingingCall();
				break;
			case R.id.endButton:
				mTelephony.endCall();
				break;
			}
		} catch (Throwable e) {
			ActivityLog.logError(this, getString(R.string.app_name),
					e.getLocalizedMessage());
			Log.e(CallerIdConstants.TAG, "Fail when do handling ringing call", e);
		}
	}

}
