package com.angelstone.android.easydialer;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class EasyDialerService extends Service {
	private static final String TAG = "EasyDialer";

	/** The length of DTMF tones in milliseconds */
	private static final int TONE_LENGTH_MS = 150;

	/** The DTMF tone volume relative to other sounds in the stream */
	private static final int TONE_RELATIVE_VOLUME = 80;

	/**
	 * Stream type used to play the DTMF tones off call, and mapped to the volume
	 * control keys
	 */
	private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;

	private String mLastNumberDialed = null;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock = new Object();
	public static StringBuilder mTextViewHelper = new StringBuilder();
  private HapticFeedback mHaptic = new HapticFeedback();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onClick(int viewId) {
		switch (viewId) {
		case R.id.one: {
			playTone(ToneGenerator.TONE_DTMF_1);
			keyPressed("1");
			return;
		}
		case R.id.two: {
			playTone(ToneGenerator.TONE_DTMF_2);
			keyPressed("2");
			return;
		}
		case R.id.three: {
			playTone(ToneGenerator.TONE_DTMF_3);
			keyPressed("3");
			return;
		}
		case R.id.four: {
			playTone(ToneGenerator.TONE_DTMF_4);
			keyPressed("4");
			return;
		}
		case R.id.five: {
			playTone(ToneGenerator.TONE_DTMF_5);
			keyPressed("5");
			return;
		}
		case R.id.six: {
			playTone(ToneGenerator.TONE_DTMF_6);
			keyPressed("6");
			return;
		}
		case R.id.seven: {
			playTone(ToneGenerator.TONE_DTMF_7);
			keyPressed("7");
			return;
		}
		case R.id.eight: {
			playTone(ToneGenerator.TONE_DTMF_8);
			keyPressed("8");
			return;
		}
		case R.id.nine: {
			playTone(ToneGenerator.TONE_DTMF_9);
			keyPressed("9");
			return;
		}
		case R.id.zero: {
			playTone(ToneGenerator.TONE_DTMF_0);
			keyPressed("0");
			return;
		}
		case R.id.pound: {
			playTone(ToneGenerator.TONE_DTMF_P);
			keyPressed("#");
			return;
		}
		case R.id.star: {
			playTone(ToneGenerator.TONE_DTMF_S);
			keyPressed("*");
			return;
		}
		case R.id.deleteButton: {
			if (mTextViewHelper.length() > 0)
				mTextViewHelper.deleteCharAt(mTextViewHelper.length() - 1);
			mHaptic.vibrate();
			return;
		}
		case R.id.dialButton: {
			mHaptic.vibrate(); // Vibrate here too, just like we do for the regular
			// keys
			dialButtonPressed();
			return;
		}
		case R.id.voicemailButton: {
			callVoicemail();
			mHaptic.vibrate();
			return;
		}
		case R.id.digits: {
			return;
		}
		}
	}

	private void playTone(int tone) {
		// Also do nothing if the phone is in silent mode.
		// We need to re-check the ringer mode for *every* playTone()
		// call, rather than keeping a local flag that's updated in
		// onResume(), since it's possible to toggle silent mode without
		// leaving the current activity (via the ENDCALL-longpress menu.)
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int ringerMode = audioManager.getRingerMode();
		if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
				|| (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
			return;
		}

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				return;
			}

			// Start the new tone (will stop any playing tone)
			mToneGenerator.startTone(tone, TONE_LENGTH_MS);
		}
	}

	private void keyPressed(String key) {
		mHaptic.vibrate();
		mTextViewHelper.append(key);
	}

	private void dialButtonPressed() {
		final String number = mTextViewHelper.toString();
		Intent intent = new Intent(Intent.ACTION_CALL);

		if (mTextViewHelper.length() == 0) { // There is no number entered.
			if (!TextUtils.isEmpty(mLastNumberDialed)) {
				// Otherwise, pressing the Dial button without entering
				// any digits means "recall the last number dialed".
				mTextViewHelper = new StringBuilder(mLastNumberDialed);
				return;
			} else {
				// Rare case: there's no "last number dialed". There's
				// nothing useful for the Dial button to do in this case.
				playTone(ToneGenerator.TONE_PROP_NACK);
				return;
			}
		} else { // There is a number.
			intent.setData(Uri.fromParts("tel", number, null));
		}

		mLastNumberDialed = number;
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		mTextViewHelper.delete(0, mTextViewHelper.length());
	}

	private void callVoicemail() {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("voicemail",
				"", null));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		mTextViewHelper.delete(0, mTextViewHelper.length());
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mTextViewHelper = new StringBuilder();
    mHaptic.init(this, true);
    mHaptic.checkSystemSetting();

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				try {
					// we want the user to be able to control the volume of the dial tones
					// outside of a call, so we use the stream type that is also mapped to
					// the
					// volume control keys for this activity
					mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE,
							TONE_RELATIVE_VOLUME);
				} catch (RuntimeException e) {
					Log.w(TAG, "Exception caught while creating local tone generator: "
							+ e);
					mToneGenerator = null;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		synchronized (mToneGeneratorLock) {
			if (mToneGenerator != null) {
				mToneGenerator.release();
				mToneGenerator = null;
			}
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		String action = intent.getAction();

		if (action.startsWith(Constants.ACTION_DELTE + "#")
				|| action.startsWith(Constants.ACTION_DIAL + "#")
				|| action.startsWith(Constants.ACTION_DIAL_PAD + "#")
				|| action.startsWith(Constants.ACTION_VOICE_MAIL + "#")
				|| action.startsWith(Constants.ACTION_SPEED_DIAL + "#")) {
			int appWidgetId = intent.getIntExtra(Constants.EXTRA_WIDGET_ID, 0);
			int viewId = intent.getIntExtra(Constants.EXTRA_VIEW_ID, 0);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
			
			onClick(viewId);
			
			RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(),
					R.layout.twelve_key_dialer_widget);
			views.setTextViewText(R.id.digits, mTextViewHelper);
			views.setTextViewText(R.id.digits_empty, "");
			views.setViewVisibility(R.id.digits, mTextViewHelper.length() > 0 ? View.VISIBLE : View.GONE);
			views.setViewVisibility(R.id.digits_empty, mTextViewHelper.length() == 0 ? View.VISIBLE : View.GONE);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
