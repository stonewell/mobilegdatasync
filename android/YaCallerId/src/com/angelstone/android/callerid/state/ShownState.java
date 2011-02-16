package com.angelstone.android.callerid.state;

import android.content.Intent;
import android.telephony.TelephonyManager;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.state.CallerIdAction.ActionType;
import com.angelstone.android.callerid.ui.FullScreenCallerIdView;

public class ShownState extends CallerIdState {
	ShownState() {
		super(2);
	}

	@Override
	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action.mActionType) {
		case Action_Sleep:
		case Action_Offhook:
		case Action_Hide:
			context.moveToState(action, new NormalState());
			break;
		default:
			break;
		}
	}

	@Override
	public void enter(CallerIdContext context, CallerIdAction action) {
		super.enter(context, action);

		final Intent newIntent = new Intent(context.mContext.getApplicationContext(),
				FullScreenCallerIdView.class);

		String number = context.getStringExtra("RINGING_NUMBER");
		
		if (action.mActionType == ActionType.Action_Ring) {
			Intent intent = action.mIntent;
			
			number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		}
		
		newIntent.putExtra(CallerIdConstants.DATA_INCOMING_NUMBER, number);
		
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_FROM_BACKGROUND
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		context.mContext.startActivity(newIntent);
	}

	@Override
	public void leave(CallerIdContext context, CallerIdAction action) {
		super.leave(context, action);
		final Intent newIntent = new Intent(context.mContext.getApplicationContext(),
				FullScreenCallerIdView.class);
		newIntent.putExtra(CallerIdConstants.DATA_HIDE, true);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_FROM_BACKGROUND
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		context.mContext.startActivity(newIntent);
	}
}
