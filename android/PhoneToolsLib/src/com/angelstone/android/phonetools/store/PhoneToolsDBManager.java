package com.angelstone.android.phonetools.store;

public class PhoneToolsDBManager {
	private static BlackListManager mBlackListManager;
	private static SettingsManager mSettingsManager;
	private static EventLogManager mEventLogManager;

	public static void initialize(String authority) {
		mBlackListManager = new BlackListManager(authority);
		mSettingsManager = new SettingsManager(authority);
		mEventLogManager = new EventLogManager(authority);
	}

	public static BlackListManager getBlackListManager() {
		return mBlackListManager;
	}

	public static SettingsManager getSettingsManager() {
		return mSettingsManager;
	}

	public static EventLogManager getEventLogManager() {
		return mEventLogManager;
	}
}
