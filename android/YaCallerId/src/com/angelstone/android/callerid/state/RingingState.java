package com.angelstone.android.callerid.state;

public class RingingState extends CallerIdState {

	@Override
	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action) {
		case Action_Offhook:
			context.moveToState(new NormalState());
			break;
		case Action_Wake:
			context.moveToState(new ShownState());
		default:
			break;
		}
	}
}
