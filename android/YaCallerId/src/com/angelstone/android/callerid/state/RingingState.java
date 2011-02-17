package com.angelstone.android.callerid.state;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

import com.angelstone.android.callerid.service.CallerIdScreenMonitorService;
import com.angelstone.android.callerid.state.CallerIdAction.ActionType;

public class RingingState extends CallerIdState {
	RingingState() {
		super(1);
	}

	@Override
	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action.mActionType) {
		case Action_Offhook:
			context.moveToState(action, new NormalState());
			break;
		case Action_Wake:
			context.moveToState(action, new ShownState());
		default:
			break;
		}
	}

	@Override
	public void enter(CallerIdContext context, CallerIdAction action) {
		super.enter(context, action);

		if (action.mActionType == ActionType.Action_Ring) {
			PowerManager pm = (PowerManager) context.mContext
					.getSystemService(Context.POWER_SERVICE);

			if (pm.isScreenOn()) {
				context.moveToState(action, new ShownState());
			} else {
				Intent intent = action.mIntent;

				context.putExtra("RINGING_NUMBER", intent
						.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
				
				Intent serviceIntent = new Intent(context.mContext.getApplicationContext(),
						CallerIdScreenMonitorService.class);
				
				context.mContext.startService(serviceIntent);
			}
		}
	}

	@Override
	public void leave(CallerIdContext context, CallerIdAction action) {
		super.leave(context, action);
		
		Intent serviceIntent = new Intent(context.mContext.getApplicationContext(),
				CallerIdScreenMonitorService.class);
		serviceIntent.putExtra("QUIT", true);
		
		context.mContext.startService(serviceIntent);
	}

}
