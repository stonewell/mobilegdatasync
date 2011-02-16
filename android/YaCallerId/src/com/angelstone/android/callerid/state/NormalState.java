package com.angelstone.android.callerid.state;

public class NormalState extends CallerIdState {

	NormalState() {
		super(0);
	}
	
	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action.mActionType) {
		case Action_Ring:
			context.moveToState(action, new RingingState());
			break;
		case Action_Wake:
			context.moveToState(action, new WakeupState());
		default:
			break;
		}
	}
}
