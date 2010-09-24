package com.angelstone.android.smsblocker.store;

public class PhoneNumberDisposition {
	public static final int SMS_UNKNOWN = 0;
	public static final int SMS_ACCEPT = 1;
	public static final int SMS_REJECT = 2;
	public static final int SMC_REJECT_ERASE = 3;

	public PhoneNumberDisposition() {
		m_SmsAction = SMS_ACCEPT;
	}

	public int m_SmsAction;
}
