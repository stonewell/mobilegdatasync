package com.angelstone.android.callerid.service;

import android.app.IntentService;
import android.content.Intent;

import com.angelstone.android.callerid.state.CallerIdAction;
import com.angelstone.android.callerid.state.CallerIdContext;

public class CallerIdService extends IntentService {

	public CallerIdService() {
		super("AS.CallFireWall_Service_Handler");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null)
			return;
		
		System.out.println("CallerId get intent:" + intent.getAction());
		
		CallerIdContext callerIdContext = new CallerIdContext();
		callerIdContext.init(this);
		callerIdContext.loadFromPreference();
		
		callerIdContext.action(CallerIdAction.intentToAction(intent));
		callerIdContext.saveToPreference();
	}
}
