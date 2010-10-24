package com.angelstone.android.easydialer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Dialer activity that displays the typical twelve key interface.
 */
public class EasyDialer extends Activity implements View.OnClickListener,
		View.OnLongClickListener, View.OnKeyListener,
		AdapterView.OnItemClickListener, TextWatcher {
	private static final String EMPTY_NUMBER = "";
	private static final String TAG = "TwelveKeyDialer";

	/** The length of DTMF tones in milliseconds */
	private static final int TONE_LENGTH_MS = 150;

	/** The DTMF tone volume relative to other sounds in the stream */
	private static final int TONE_RELATIVE_VOLUME = 80;

	/**
	 * Stream type used to play the DTMF tones off call, and mapped to the volume
	 * control keys
	 */
	private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;

	private EditText mDigits;
	private View mDelete;
	private MenuItem mAddToContactMenuItem;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock = new Object();
	private Drawable mDigitsBackground;
	private Drawable mDigitsEmptyBackground;
	private View mDialpad;
	private View mVoicemailDialAndDeleteRow;
	private View mVoicemailButton;
	private View mDialButton;
	private ListView mDialpadChooser;
	private DialpadChooserAdapter mDialpadChooserAdapter;
	// Member variables for dialpad options
	private MenuItem m2SecPauseMenuItem;
	private MenuItem mWaitMenuItem;
	private static final int MENU_ADD_CONTACTS = 1;
	private static final int MENU_2S_PAUSE = 2;
	private static final int MENU_WAIT = 3;

	// Last number dialed, retrieved asynchronously from the call DB
	// in onCreate. This number is displayed when the user hits the
	// send key and cleared in onPause.
	private String mLastNumberDialed = EMPTY_NUMBER;

	// determines if we want to playback local DTMF tones.
	private boolean mDTMFToneEnabled;
  private HapticFeedback mHaptic = new HapticFeedback();

	/** Identifier for the "Add Call" intent extra. */
	static final String ADD_CALL_MODE_KEY = "add_call_mode";

	PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		/**
		 * Listen for phone state changes so that we can take down the
		 * "dialpad chooser" if the phone becomes idle while the chooser UI is
		 * visible.
		 */
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// Log.i(TAG, "PhoneStateListener.onCallStateChanged: "
			// + state + ", '" + incomingNumber + "'");
			if ((state == TelephonyManager.CALL_STATE_IDLE)
					&& dialpadChooserVisible()) {
				// Log.i(TAG,
				// "Call ended with dialpad chooser visible!  Taking it down...");
				// Note there's a race condition in the UI here: the
				// dialpad chooser could conceivably disappear (on its
				// own) at the exact moment the user was trying to select
				// one of the choices, which would be confusing. (But at
				// least that's better than leaving the dialpad chooser
				// onscreen, but useless...)
				showDialpadChooser(false);
			}
		}
	};

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// Do nothing
	}

	public void onTextChanged(CharSequence input, int start, int before,
			int changeCount) {
		// Do nothing
		// DTMF Tones do not need to be played here any longer -
		// the DTMF dialer handles that functionality now.
	}

	public void afterTextChanged(Editable input) {
		// if (SpecialCharSequenceMgr.handleChars(this, input.toString(), mDigits))
		// {
		// // A special sequence was entered, clear the digits
		// mDigits.getText().clear();
		// }

		if (!isDigitsEmpty()) {
			mDigits.setBackgroundDrawable(mDigitsBackground);
		} else {
			mDigits.setCursorVisible(false);
			mDigits.setBackgroundDrawable(mDigitsEmptyBackground);
		}

		updateDialAndDeleteButtonEnabledState();
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Resources r = getResources();

		// Set the content view
		setContentView(getContentViewResource());

		// Load up the resources for the text field.
		mDigitsBackground = r.getDrawable(R.drawable.btn_dial_textfield_active);
		mDigitsEmptyBackground = r.getDrawable(R.drawable.btn_dial_textfield);

		mDigits = (EditText) findViewById(R.id.digits);
		mDigits.setKeyListener(DialerKeyListener.getInstance());
		mDigits.setOnClickListener(this);
		mDigits.setOnKeyListener(this);

		maybeAddNumberFormatting();

		// Check for the presence of the keypad
		View view = findViewById(R.id.one);
		if (view != null) {
			setupKeypad();
		}

		mVoicemailDialAndDeleteRow = findViewById(R.id.voicemailAndDialAndDelete);

		initVoicemailButton();

		// Check whether we should show the onscreen "Dial" button.
		mDialButton = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton);

		mDialButton.setOnClickListener(this);

		view = mVoicemailDialAndDeleteRow.findViewById(R.id.deleteButton);
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);
		mDelete = view;

		mDialpad = findViewById(R.id.dialpad); // This is null in landscape mode.

		// In landscape we put the keyboard in phone mode.
		// In portrait we prevent the soft keyboard to show since the
		// dialpad acts as one already.
		if (null == mDialpad) {
			mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
		} else {
			mDigits.setInputType(android.text.InputType.TYPE_NULL);
		}

		// Set up the "dialpad chooser" UI; see showDialpadChooser().
		mDialpadChooser = (ListView) findViewById(R.id.dialpadChooser);
		mDialpadChooser.setOnItemClickListener(this);

		if (icicle != null) {
			super.onRestoreInstanceState(icicle);
		}

		// try {
		// mHaptic.init(this, true);
		// } catch (Resources.NotFoundException nfe) {
		// Log.e(TAG, "Vibrate control bool missing.", nfe);
		// }

	}

	@Override
	protected void onRestoreInstanceState(Bundle icicle) {
		// Do nothing, state is restored in onCreate() if needed
	}

	protected void maybeAddNumberFormatting() {
		mDigits.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
	}

	/**
	 * Overridden by subclasses to control the resource used by the content view.
	 */
	protected int getContentViewResource() {
		return R.layout.twelve_key_dialer;
	}

	protected void setFormattedDigits(String data) {
		// strip the non-dialable numbers out of the data string.
		String dialString = PhoneNumberUtils.extractNetworkPortion(data);
		dialString = PhoneNumberUtils.formatNumber(dialString);
		if (!TextUtils.isEmpty(dialString)) {
			Editable digits = mDigits.getText();
			digits.replace(0, digits.length(), dialString);
			// for some reason this isn't getting called in the digits.replace call
			// above..
			// but in any case, this will make sure the background drawable looks
			// right
			afterTextChanged(digits);
		}
	}

	@Override
	protected void onNewIntent(Intent newIntent) {
		setIntent(newIntent);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// This can't be done in onCreate(), since the auto-restoring of the digits
		// will play DTMF tones for all the old digits if it is when
		// onRestoreSavedInstanceState()
		// is called. This method will be called every time the activity is created,
		// and
		// will always happen after onRestoreSavedInstanceState().
		mDigits.addTextChangedListener(this);
	}

	private void setupKeypad() {
		// Setup the listeners for the buttons
		View view = findViewById(R.id.one);
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);

		findViewById(R.id.two).setOnClickListener(this);
		findViewById(R.id.three).setOnClickListener(this);
		findViewById(R.id.four).setOnClickListener(this);
		findViewById(R.id.five).setOnClickListener(this);
		findViewById(R.id.six).setOnClickListener(this);
		findViewById(R.id.seven).setOnClickListener(this);
		findViewById(R.id.eight).setOnClickListener(this);
		findViewById(R.id.nine).setOnClickListener(this);
		findViewById(R.id.star).setOnClickListener(this);

		view = findViewById(R.id.zero);
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);

		findViewById(R.id.pound).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Query the last dialed number. Do it first because hitting
		// the DB is 'slow'. This call is asynchronous.
		queryLastOutgoingCall();

		// retrieve the DTMF tone play back setting.
		mDTMFToneEnabled = Settings.System.getInt(getContentResolver(),
				Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;

		// Retrieve the haptic feedback setting.
    mHaptic.init(this, true);
		mHaptic.checkSystemSetting();

		// if the mToneGenerator creation fails, just continue without it. It is
		// a local audio signal, and is not as important as the dtmf tone itself.
		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				try {
					// we want the user to be able to control the volume of the dial tones
					// outside of a call, so we use the stream type that is also mapped to
					// the
					// volume control keys for this activity
					mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE,
							TONE_RELATIVE_VOLUME);
					setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
				} catch (RuntimeException e) {
					Log.w(TAG, "Exception caught while creating local tone generator: "
							+ e);
					mToneGenerator = null;
				}
			}
		}

		// While we're in the foreground, listen for phone state changes,
		// purely so that we can take down the "dialpad chooser" if the
		// phone becomes idle while the chooser UI is visible.
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		mDigits.setHint(null);

		// Also, a sanity-check: the "dialpad chooser" UI should NEVER
		// be visible if the phone is idle!
		showDialpadChooser(false);

		updateDialAndDeleteButtonEnabledState();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			// Hide soft keyboard, if visible (it's fugly over button dialer).
			// The only known case where this will be true is when launching the
			// dialer with
			// ACTION_DIAL via a soft keyboard. we dismiss it here because we don't
			// have a window token yet in onCreate / onNewIntent
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(mDigits.getWindowToken(), 0);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Stop listening for phone state changes.
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephonyManager
				.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator != null) {
				mToneGenerator.release();
				mToneGenerator = null;
			}
		}
		// TODO: I wonder if we should not check if the AsyncTask that
		// lookup the last dialed number has completed.
		mLastNumberDialed = EMPTY_NUMBER; // Since we are going to query again, free
																			// stale number.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mAddToContactMenuItem = menu.add(0, MENU_ADD_CONTACTS, 0,
				R.string.recentCalls_addToContact).setIcon(
				android.R.drawable.ic_menu_add);
		m2SecPauseMenuItem = menu.add(0, MENU_2S_PAUSE, 0, R.string.add_2sec_pause)
				.setIcon(R.drawable.ic_menu_2sec_pause);
		mWaitMenuItem = menu.add(0, MENU_WAIT, 0, R.string.add_wait).setIcon(
				R.drawable.ic_menu_wait);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// We never show a menu if the "choose dialpad" UI is up.
		if (dialpadChooserVisible()) {
			return false;
		}

		if (isDigitsEmpty()) {
			mAddToContactMenuItem.setVisible(false);
			m2SecPauseMenuItem.setVisible(false);
			mWaitMenuItem.setVisible(false);
		} else {
			CharSequence digits = mDigits.getText();

//			// Put the current digits string into an intent
//			Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
//			intent.putExtra(Insert.PHONE, digits);
//			intent.setType(People.CONTENT_ITEM_TYPE);
//			mAddToContactMenuItem.setIntent(intent);
//			mAddToContactMenuItem.setVisible(true);

			// Check out whether to show Pause & Wait option menu items
			int selectionStart;
			int selectionEnd;
			String strDigits = digits.toString();

			selectionStart = mDigits.getSelectionStart();
			selectionEnd = mDigits.getSelectionEnd();

			if (selectionStart != -1) {
				if (selectionStart > selectionEnd) {
					// swap it as we want start to be less then end
					int tmp = selectionStart;
					selectionStart = selectionEnd;
					selectionEnd = tmp;
				}

				if (selectionStart != 0) {
					// Pause can be visible if cursor is not in the begining
					m2SecPauseMenuItem.setVisible(true);

					// For Wait to be visible set of condition to meet
					mWaitMenuItem.setVisible(showWait(selectionStart, selectionEnd,
							strDigits));
				} else {
					// cursor in the beginning both pause and wait to be invisible
					m2SecPauseMenuItem.setVisible(false);
					mWaitMenuItem.setVisible(false);
				}
			} else {
				// cursor is not selected so assume new digit is added to the end
				int strLength = strDigits.length();
				mWaitMenuItem.setVisible(showWait(strLength, strLength, strDigits));
			}
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CALL: {
			long callPressDiff = SystemClock.uptimeMillis() - event.getDownTime();
			if (callPressDiff >= ViewConfiguration.getLongPressTimeout()) {
				// Launch voice dialer
				Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
				}
			}
			return true;
		}
		case KeyEvent.KEYCODE_1: {
			long timeDiff = SystemClock.uptimeMillis() - event.getDownTime();
			if (timeDiff >= ViewConfiguration.getLongPressTimeout()) {
				// Long press detected, call voice mail
				callVoicemail();
			}
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CALL: {
			dialButtonPressed();
			return true;
		}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void keyPressed(int keyCode) {
		mHaptic.vibrate();
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		mDigits.onKeyDown(keyCode, event);
	}

	public boolean onKey(View view, int keyCode, KeyEvent event) {
		switch (view.getId()) {
		case R.id.digits:
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				dialButtonPressed();
				return true;
			}
			break;
		}
		return false;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.one: {
			playTone(ToneGenerator.TONE_DTMF_1);
			keyPressed(KeyEvent.KEYCODE_1);
			return;
		}
		case R.id.two: {
			playTone(ToneGenerator.TONE_DTMF_2);
			keyPressed(KeyEvent.KEYCODE_2);
			return;
		}
		case R.id.three: {
			playTone(ToneGenerator.TONE_DTMF_3);
			keyPressed(KeyEvent.KEYCODE_3);
			return;
		}
		case R.id.four: {
			playTone(ToneGenerator.TONE_DTMF_4);
			keyPressed(KeyEvent.KEYCODE_4);
			return;
		}
		case R.id.five: {
			playTone(ToneGenerator.TONE_DTMF_5);
			keyPressed(KeyEvent.KEYCODE_5);
			return;
		}
		case R.id.six: {
			playTone(ToneGenerator.TONE_DTMF_6);
			keyPressed(KeyEvent.KEYCODE_6);
			return;
		}
		case R.id.seven: {
			playTone(ToneGenerator.TONE_DTMF_7);
			keyPressed(KeyEvent.KEYCODE_7);
			return;
		}
		case R.id.eight: {
			playTone(ToneGenerator.TONE_DTMF_8);
			keyPressed(KeyEvent.KEYCODE_8);
			return;
		}
		case R.id.nine: {
			playTone(ToneGenerator.TONE_DTMF_9);
			keyPressed(KeyEvent.KEYCODE_9);
			return;
		}
		case R.id.zero: {
			playTone(ToneGenerator.TONE_DTMF_0);
			keyPressed(KeyEvent.KEYCODE_0);
			return;
		}
		case R.id.pound: {
			playTone(ToneGenerator.TONE_DTMF_P);
			keyPressed(KeyEvent.KEYCODE_POUND);
			return;
		}
		case R.id.star: {
			playTone(ToneGenerator.TONE_DTMF_S);
			keyPressed(KeyEvent.KEYCODE_STAR);
			return;
		}
		case R.id.deleteButton: {
			keyPressed(KeyEvent.KEYCODE_DEL);
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
			if (!isDigitsEmpty()) {
				mDigits.setCursorVisible(true);
			}
			return;
		}
		}
	}

	public boolean onLongClick(View view) {
		final Editable digits = mDigits.getText();
		int id = view.getId();
		switch (id) {
		case R.id.deleteButton: {
			digits.clear();
			// TODO: The framework forgets to clear the pressed
			// status of disabled button. Until this is fixed,
			// clear manually the pressed status. b/2133127
			mDelete.setPressed(false);
			return true;
		}
		case R.id.one: {
			if (isDigitsEmpty()) {
				callVoicemail();
				return true;
			}
			return false;
		}
		case R.id.zero: {
			keyPressed(KeyEvent.KEYCODE_PLUS);
			return true;
		}
		}
		return false;
	}

	void callVoicemail() {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("voicemail",
				EMPTY_NUMBER, null));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		mDigits.getText().clear();
		finish();
	}

	void dialButtonPressed() {
		final String number = mDigits.getText().toString();
		boolean sendEmptyFlash = false;
		Intent intent = new Intent(Intent.ACTION_CALL);

		if (isDigitsEmpty()) { // There is no number entered.
			if (!TextUtils.isEmpty(mLastNumberDialed)) {
				// Otherwise, pressing the Dial button without entering
				// any digits means "recall the last number dialed".
				mDigits.setText(mLastNumberDialed);
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

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		mDigits.getText().clear();

		// Don't finish TwelveKeyDialer yet if we're sending a blank flash for CDMA.
		// CDMA
		// networks use Flash messages when special processing needs to be done,
		// mainly for
		// 3-way or call waiting scenarios. Presumably, here we're in a special
		// 3-way scenario
		// where the network needs a blank flash before being able to add the new
		// participant.
		// (This is not the case with all 3-way calls, just certain CDMA
		// infrastructures.)
		if (!sendEmptyFlash) {
			finish();
		}
	}

	/**
	 * Plays the specified tone for TONE_LENGTH_MS milliseconds.
	 * 
	 * The tone is played locally, using the audio stream for phone calls. Tones
	 * are played only if the "Audible touch tones" user preference is checked,
	 * and are NOT played if the device is in silent mode.
	 * 
	 * @param tone
	 *          a tone code from {@link ToneGenerator}
	 */
	void playTone(int tone) {
		// if local tone playback is disabled, just return.
		if (!mDTMFToneEnabled) {
			return;
		}

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
				Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
				return;
			}

			// Start the new tone (will stop any playing tone)
			mToneGenerator.startTone(tone, TONE_LENGTH_MS);
		}
	}

	/**
	 * Brings up the "dialpad chooser" UI in place of the usual Dialer elements
	 * (the textfield/button and the dialpad underneath).
	 * 
	 * We show this UI if the user brings up the Dialer while a call is already in
	 * progress, since there's a good chance we got here accidentally (and the
	 * user really wanted the in-call dialpad instead). So in this situation we
	 * display an intermediate UI that lets the user explicitly choose between the
	 * in-call dialpad ("Use touch tone keypad") and the regular Dialer ("Add
	 * call").  (Or, the option "Return to call in progress" just goes back to the
	 * in-call UI with no dialpad at all.)
	 * 
	 * @param enabled
	 *          If true, show the "dialpad chooser" instead of the regular Dialer
	 *          UI
	 */
	private void showDialpadChooser(boolean enabled) {
		if (enabled) {
			// Log.i(TAG, "Showing dialpad chooser!");
			mDigits.setVisibility(View.GONE);
			if (mDialpad != null)
				mDialpad.setVisibility(View.GONE);
			mVoicemailDialAndDeleteRow.setVisibility(View.GONE);
			mDialpadChooser.setVisibility(View.VISIBLE);

			// Instantiate the DialpadChooserAdapter and hook it up to the
			// ListView. We do this only once.
			if (mDialpadChooserAdapter == null) {
				mDialpadChooserAdapter = new DialpadChooserAdapter(this);
				mDialpadChooser.setAdapter(mDialpadChooserAdapter);
			}
		} else {
			// Log.i(TAG, "Displaying normal Dialer UI.");
			mDigits.setVisibility(View.VISIBLE);
			if (mDialpad != null)
				mDialpad.setVisibility(View.VISIBLE);
			mVoicemailDialAndDeleteRow.setVisibility(View.VISIBLE);
			mDialpadChooser.setVisibility(View.GONE);
		}
	}

	/**
	 * @return true if we're currently showing the "dialpad chooser" UI.
	 */
	private boolean dialpadChooserVisible() {
		return mDialpadChooser.getVisibility() == View.VISIBLE;
	}

	/**
	 * Simple list adapter, binding to an icon + text label for each item in the
	 * "dialpad chooser" list.
	 */
	private static class DialpadChooserAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		// Simple struct for a single "choice" item.
		static class ChoiceItem {
			String text;
			Bitmap icon;
			int id;

			public ChoiceItem(String s, Bitmap b, int i) {
				text = s;
				icon = b;
				id = i;
			}
		}

		// IDs for the possible "choices":
		static final int DIALPAD_CHOICE_USE_DTMF_DIALPAD = 101;
		static final int DIALPAD_CHOICE_RETURN_TO_CALL = 102;
		static final int DIALPAD_CHOICE_ADD_NEW_CALL = 103;

		private static final int NUM_ITEMS = 3;
		private ChoiceItem mChoiceItems[] = new ChoiceItem[NUM_ITEMS];

		public DialpadChooserAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);

			// Initialize the possible choices.
			// TODO: could this be specified entirely in XML?

			// - "Use touch tone keypad"
			mChoiceItems[0] = new ChoiceItem(
					context.getString(R.string.dialer_useDtmfDialpad),
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.ic_dialer_fork_tt_keypad),
					DIALPAD_CHOICE_USE_DTMF_DIALPAD);

			// - "Return to call in progress"
			mChoiceItems[1] = new ChoiceItem(
					context.getString(R.string.dialer_returnToInCallScreen),
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.ic_dialer_fork_current_call),
					DIALPAD_CHOICE_RETURN_TO_CALL);

			// - "Add call"
			mChoiceItems[2] = new ChoiceItem(
					context.getString(R.string.dialer_addAnotherCall),
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.ic_dialer_fork_add_call), DIALPAD_CHOICE_ADD_NEW_CALL);
		}

		public int getCount() {
			return NUM_ITEMS;
		}

		/**
		 * Return the ChoiceItem for a given position.
		 */
		public Object getItem(int position) {
			return mChoiceItems[position];
		}

		/**
		 * Return a unique ID for each possible choice.
		 */
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Make a view for each row.
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			// When convertView is non-null, we can reuse it (there's no need
			// to reinflate it.)
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dialpad_chooser_list_item,
						null);
			}

			TextView text = (TextView) convertView.findViewById(R.id.text);
			text.setText(mChoiceItems[position].text);

			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			icon.setImageBitmap(mChoiceItems[position].icon);

			return convertView;
		}
	}

	/**
	 * Handle clicks from the dialpad chooser.
	 */
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		DialpadChooserAdapter.ChoiceItem item = (DialpadChooserAdapter.ChoiceItem) parent
				.getItemAtPosition(position);
		int itemId = item.id;
		switch (itemId) {
		case DialpadChooserAdapter.DIALPAD_CHOICE_USE_DTMF_DIALPAD:
			break;

		case DialpadChooserAdapter.DIALPAD_CHOICE_RETURN_TO_CALL:
			break;

		case DialpadChooserAdapter.DIALPAD_CHOICE_ADD_NEW_CALL:
			// Log.i(TAG, "DIALPAD_CHOICE_ADD_NEW_CALL");
			// Ok, guess the user really did want to be here (in the
			// regular Dialer) after all. Bring back the normal Dialer UI.
			showDialpadChooser(false);
			break;

		default:
			Log.w(TAG, "onItemClick: unexpected itemId: " + itemId);
			break;
		}
	}

	/**
	 * Returns true whenever any one of the options from the menu is selected.
	 * Code changes to support dialpad options
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_2S_PAUSE:
			updateDialString(",");
			return true;
		case MENU_WAIT:
			updateDialString(";");
			return true;
		}
		return false;
	}

	/**
	 * Updates the dial string (mDigits) after inserting a Pause character (,) or
	 * Wait character (;).
	 */
	private void updateDialString(String newDigits) {
		int selectionStart;
		int selectionEnd;

		// SpannableStringBuilder editable_text = new
		// SpannableStringBuilder(mDigits.getText());
		int anchor = mDigits.getSelectionStart();
		int point = mDigits.getSelectionEnd();

		selectionStart = Math.min(anchor, point);
		selectionEnd = Math.max(anchor, point);

		Editable digits = mDigits.getText();
		if (selectionStart != -1) {
			if (selectionStart == selectionEnd) {
				// then there is no selection. So insert the pause at this
				// position and update the mDigits.
				digits.replace(selectionStart, selectionStart, newDigits);
			} else {
				digits.replace(selectionStart, selectionEnd, newDigits);
				// Unselect: back to a regular cursor, just pass the character inserted.
				mDigits.setSelection(selectionStart + 1);
			}
		} else {
			int len = mDigits.length();
			digits.replace(len, len, newDigits);
		}
	}

	/**
	 * Update the enabledness of the "Dial" and "Backspace" buttons if applicable.
	 */
	private void updateDialAndDeleteButtonEnabledState() {
		final boolean digitsNotEmpty = !isDigitsEmpty();

		if (mDialButton != null) {
			mDialButton.setEnabled(digitsNotEmpty
					|| !TextUtils.isEmpty(mLastNumberDialed));
		}
		mDelete.setEnabled(digitsNotEmpty);
	}

	/**
	 * Check if voicemail is enabled/accessible.
	 */
	private void initVoicemailButton() {
		boolean hasVoicemail = false;
		try {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

			hasVoicemail = tm.getVoiceMailNumber() != null;
		} catch (SecurityException se) {
			// Possibly no READ_PHONE_STATE privilege.
		}

		mVoicemailButton = mVoicemailDialAndDeleteRow
				.findViewById(R.id.voicemailButton);
		if (hasVoicemail) {
			mVoicemailButton.setOnClickListener(this);
		} else {
			mVoicemailButton.setEnabled(false);
		}
	}

	/**
	 * This function return true if Wait menu item can be shown otherwise returns
	 * false. Assumes the passed string is non-empty and the 0th index check is
	 * not required.
	 */
	private boolean showWait(int start, int end, String digits) {
		if (start == end) {
			// visible false in this case
			if (start > digits.length())
				return false;

			// preceding char is ';', so visible should be false
			if (digits.charAt(start - 1) == ';')
				return false;

			// next char is ';', so visible should be false
			if ((digits.length() > start) && (digits.charAt(start) == ';'))
				return false;
		} else {
			// visible false in this case
			if (start > digits.length() || end > digits.length())
				return false;

			// In this case we need to just check for ';' preceding to start
			// or next to end
			if (digits.charAt(start - 1) == ';')
				return false;
		}
		return true;
	}

	/**
	 * @return true if the widget with the phone number digits is empty.
	 */
	private boolean isDigitsEmpty() {
		return mDigits.length() == 0;
	}

	/**
	 * Starts the asyn query to get the last dialed/outgoing number. When the
	 * background query finishes, mLastNumberDialed is set to the last dialed
	 * number or an empty string if none exists yet.
	 */
	private void queryLastOutgoingCall() {
		mLastNumberDialed = EMPTY_NUMBER;
	}

	@Override
	public void startSearch(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {
		if (globalSearch) {
			super.startSearch(initialQuery, selectInitialQuery, appSearchData,
					globalSearch);
		} else {
		}
	}
}
