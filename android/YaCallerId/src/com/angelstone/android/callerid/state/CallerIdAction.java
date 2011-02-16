package com.angelstone.android.callerid.state;

import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallerIdAction {
	
	public static enum ActionType {
		Action_Unknown,
		Action_Ring,
		Action_Offhook,
		Action_Sleep,
		Action_Wake,
		Action_Hide,
	};
	
	public static final CallerIdAction UNKNOWN_ACTION = new CallerIdAction();
	
	ActionType mActionType = ActionType.Action_Unknown;
	Intent mIntent = null;
	
	public static CallerIdAction intentToAction(Intent intent) {
		if (intent == null)
			return UNKNOWN_ACTION;
		
		String action = intent.getAction();
		
		CallerIdAction callerIdAction = new CallerIdAction();
		callerIdAction.mIntent = intent;
		
		if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			callerIdAction.mActionType = ActionType.Action_Sleep;
		} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
			callerIdAction.mActionType = ActionType.Action_Wake;
		} else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			
			if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
				callerIdAction.mActionType = ActionType.Action_Ring;
			} else {
				callerIdAction.mActionType = ActionType.Action_Offhook;
			}
		}
		
		return callerIdAction;
	}
}
