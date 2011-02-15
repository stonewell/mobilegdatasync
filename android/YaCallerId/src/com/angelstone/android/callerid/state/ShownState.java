package com.angelstone.android.callerid.state;

public class ShownState extends CallerIdState {

	@Override
	public void action(CallerIdContext context, CallerIdAction action) {
		switch (action) {
		case Action_Sleep:
		case Action_Offhook:
		case Action_Hide:
			context.moveToState(new NormalState());
			break;
		default:
			break;
		}
	}
}
