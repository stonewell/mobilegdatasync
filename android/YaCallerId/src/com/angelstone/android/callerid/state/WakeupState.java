package com.angelstone.android.callerid.state;

public class WakeupState extends CallerIdState {

	@Override
	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action) {
		case Action_Ring:
			context.moveToState(new ShownState());
			break;
		case Action_Sleep:
			context.moveToState(new NormalState());
		default:
			break;
		}
	}

}
