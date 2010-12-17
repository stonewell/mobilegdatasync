package com.angelstone.android.phonetools.store;

public class PhoneNumberDisposition {
	public static final int UNKNOWN = 0;
	public static final int ACCEPT = 1;
	public static final int REJECT = 2;
	public static final int REJECT_ERASE = 3;

	public PhoneNumberDisposition() {
		mAction = ACCEPT;
	}

	public int mAction;
}
