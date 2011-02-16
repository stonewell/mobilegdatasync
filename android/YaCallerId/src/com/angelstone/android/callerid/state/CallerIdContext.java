package com.angelstone.android.callerid.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

public class CallerIdContext {
	private CallerIdState mCurrentState;
	private Bundle mBundle = new Bundle();

	Context mContext;

	public CallerIdContext() {
		mCurrentState = new NormalState();
	}

	public void moveToState(CallerIdAction action, CallerIdState state) {
		if (mCurrentState != null)
			mCurrentState.leave(this, action);

		int from_state = mCurrentState.mStateType;

		mCurrentState = state;

		if (mCurrentState != null)
			mCurrentState.enter(this, action);

		Log.e("CallerID", "From:" + from_state + " To:"
				+ mCurrentState.mStateType);
	}

	public CallerIdState currentState() {
		return mCurrentState;
	}

	public void action(CallerIdAction action) {
		if (mCurrentState == null)
			mCurrentState = new NormalState();

		mCurrentState.action(this, action);
	}

	public void init(Context context) {
		mContext = context;
	}

	public void saveToPreference() {
		SharedPreferences sp = mContext.getSharedPreferences("CallerIdContext",
				Context.MODE_PRIVATE);

		Editor ed = sp.edit();
		ed.putInt("STATE", mCurrentState.mStateType);
		ed.putInt("BUNDLE_COUNT", mBundle.size());

		int index = 0;

		for (String key : mBundle.keySet()) {
			ed.putString("KEY" + index, key);
			ed.putString("VAL" + index, mBundle.getString(key));
		}

		ed.commit();
	}

	public void loadFromPreference() {
		SharedPreferences sp = mContext.getSharedPreferences("CallerIdContext",
				Context.MODE_PRIVATE);

		int state = sp.getInt("STATE", -1);

		switch (state) {
		case 0:
			mCurrentState = new NormalState();
			break;
		case 1:
			mCurrentState = new RingingState();
			break;
		case 2:
			mCurrentState = new ShownState();
			break;
		case 3:
			mCurrentState = new WakeupState();
			break;
		default:
			mCurrentState = new NormalState();
			break;
		}

		int count = sp.getInt("BUNDLE_COUNT", 0);

		for (int i = 0; i < count; i++) {
			mBundle.putString(sp.getString("KEY" + i, ""),
					sp.getString("VAL" + i, ""));
		}
	}

	public String getStringExtra(String name) {
		return mBundle.getString(name);
	}

	public void putExtra(String name, String value) {
		mBundle.putString(name, value);
	}
}
