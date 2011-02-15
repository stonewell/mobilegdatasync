package com.angelstone.android.callerid.state;

public class NormalState extends CallerIdState {

	public void action(CallerIdAction action) {
		switch (action) {
		case Action_Ring:
		case Action_Wake:
		default:
			break;
		}
	}
}
