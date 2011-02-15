package com.angelstone.android.callerid.state;

public class NormalState extends CallerIdState {

	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action) {
		case Action_Ring:
			context.moveToState(new RingingState());
			break;
		case Action_Wake:
			context.moveToState(new WakeupState());
		default:
			break;
		}
	}
}
