package com.angelstone.android.callerid.state;

import android.content.Intent;

public enum CallerIdAction {
	Action_Ring,
	Action_Offhook,
	Action_Sleep,
	Action_Wake,
	Action_Hide;
	
	public static CallerIdAction intentToAction(Intent intent) {
		return Action_Ring;
	}
}
