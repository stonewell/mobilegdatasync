package com.angelstone.android.callerid.state;

public class CallerIdContext {
	private CallerIdState mCurrentState;

	public CallerIdContext() {
		mCurrentState = new NormalState();
	}
	
	public void moveToState(CallerIdState state) {
		if (mCurrentState != null)
			mCurrentState.leave(this);

		mCurrentState = state;

		if (mCurrentState != null)
			mCurrentState.enter(this);
	}

	public CallerIdState currentState() {
		return mCurrentState;
	}
	
	public void action(CallerIdAction action) {
		if (mCurrentState == null)
			mCurrentState = new NormalState();
		
		mCurrentState.action(action);
	}
}
