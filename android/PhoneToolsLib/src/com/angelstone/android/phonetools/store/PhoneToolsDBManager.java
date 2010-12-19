package com.angelstone.android.phonetools.store;

public class PhoneToolsDBManager {
	private static BlackListManager mBlackListManager;
	private static SettingsManager mSettingsManager;
	private static EventLogManager mEventLogManager;
	private static boolean mInitialized = false;

	public static synchronized void initialize(String authority) {
		
		if (mInitialized)
			return;
		
		mBlackListManager = new BlackListManager(authority);
		mSettingsManager = new SettingsManager(authority);
		mEventLogManager = new EventLogManager(authority);
		
		mInitialized = true;
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
