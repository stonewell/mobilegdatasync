package com.angelstone.android.callerid.state;

public class WakeupState extends CallerIdState {

	WakeupState() {
		super(3);
	}
	
	@Override
	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action.mActionType) {
		case Action_Ring:
			context.moveToState(action, new ShownState());
			break;
		case Action_Sleep:
			context.moveToState(action, new NormalState());
		default:
			break;
		}
	}

}
